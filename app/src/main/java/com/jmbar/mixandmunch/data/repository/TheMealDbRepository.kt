package com.jmbar.mixandmunch.data.repository

import com.jmbar.mixandmunch.data.remote.themealdb.TheMealDbService
import com.jmbar.mixandmunch.domain.model.RecipeSummary
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * TheMealDB implementation of RecipeRepository for Step 2
 */
class TheMealDbRepository(
    private val service: TheMealDbService
) : RecipeRepository {

    override suspend fun searchByIngredients(
        rawInputs: List<String>,
        maxNormalized: Int,
        maxResults: Int
    ): List<RecipeSummary> {
        try {
            // Step 1: Normalize ingredients using the pure function
            val normalizedIngredients = IngredientNormalizer.normalize(rawInputs, maxNormalized)
            
            if (normalizedIngredients.isEmpty()) {
                return emptyList()
            }

            // Step 2: Query TheMealDB for each normalized ingredient
            val mealIdToMatchCount = mutableMapOf<String, Int>()
            val mealIdToDetails = mutableMapOf<String, Pair<String, String?>>() // id to (name, thumb)

            coroutineScope {
                val deferredResults = normalizedIngredients.map { ingredient ->
                    async {
                        try {
                            val response = service.filterByIngredient(ingredient)
                            response.meals?.forEach { meal ->
                                // Count matches for this meal
                                mealIdToMatchCount[meal.idMeal] = mealIdToMatchCount.getOrDefault(meal.idMeal, 0) + 1
                                // Store meal details (name and thumbnail)
                                mealIdToDetails[meal.idMeal] = Pair(meal.strMeal, meal.strMealThumb)
                            }
                        } catch (e: Exception) {
                            // Network error: skip this ingredient, continue with others
                        }
                    }
                }
                deferredResults.awaitAll()
            }

            // Step 3: Create RecipeSummary objects and sort by matchedCount desc, then name asc
            val recipeSummaries = mealIdToMatchCount.map { (mealId, matchCount) ->
                val (name, thumb) = mealIdToDetails[mealId] ?: Pair("Unknown Recipe", null)
                RecipeSummary(
                    id = mealId,
                    name = name,
                    thumb = thumb,
                    matchedCount = matchCount
                )
            }

            return recipeSummaries
                .sortedWith(compareByDescending<RecipeSummary> { it.matchedCount }.thenBy { it.name })
                .take(maxResults)

        } catch (e: Exception) {
            // If all calls fail, return empty list (no exceptions thrown to callers)
            return emptyList()
        }
    }
}