package com.jmbar.mixandmunch.data.repository

import com.jmbar.mixandmunch.data.local.dao.FilterCacheDao
import com.jmbar.mixandmunch.data.local.dao.MealDetailsDao
import com.jmbar.mixandmunch.data.local.dao.SavedRecipeDao
import com.jmbar.mixandmunch.data.local.entity.FilterCacheEntity
import com.jmbar.mixandmunch.data.local.entity.MealDetailsEntity
import com.jmbar.mixandmunch.data.local.entity.SavedRecipeEntity
import com.jmbar.mixandmunch.data.remote.api.TheMealDbApi
import com.jmbar.mixandmunch.data.remote.dto.MealDetailDto
import com.jmbar.mixandmunch.domain.model.*
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import com.jmbar.mixandmunch.utils.RecipeRanker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val api: TheMealDbApi,
    private val filterCacheDao: FilterCacheDao,
    private val mealDetailsDao: MealDetailsDao,
    private val savedRecipeDao: SavedRecipeDao,
    private val ingredientNormalizer: IngredientNormalizer,
    private val recipeRanker: RecipeRanker,
    private val json: Json
) : RecipeRepository {

    override suspend fun searchRecipes(ingredients: List<String>): Resource<RecipeSearchResult> {
        try {
            // Normalize ingredients first
            val normalizedIngredients = ingredientNormalizer.normalize(ingredients)
            
            if (normalizedIngredients.isEmpty()) {
                return Resource.Error("Please enter at least one ingredient")
            }

            // Get meal IDs for each ingredient with caching
            val ingredientToMealIds = mutableMapOf<String, List<String>>()
            var totalApiCalls = 0

            for (ingredient in normalizedIngredients) {
                if (totalApiCalls >= 6) break // Limit to 6 filter calls as specified

                val mealIds = getCachedOrFetchMealIds(ingredient)
                if (mealIds != null) {
                    ingredientToMealIds[ingredient] = mealIds
                    if (mealIds.isNotEmpty()) totalApiCalls++
                }
            }

            // Find intersection of meal IDs (recipes with all ingredients)
            val allMealIds = ingredientToMealIds.values.flatten().distinct()
            var candidateMealIds = if (ingredientToMealIds.isNotEmpty()) {
                ingredientToMealIds.values.reduce { acc, list -> 
                    acc.intersect(list.toSet()).toList() 
                }
            } else {
                emptyList()
            }

            // Use union fallback if intersection is empty
            val isPartialMatch = candidateMealIds.isEmpty() && allMealIds.isNotEmpty()
            if (isPartialMatch) {
                candidateMealIds = allMealIds.take(10) // Limit to top 10 for performance
            }

            if (candidateMealIds.isEmpty()) {
                // Suggest ingredient to remove
                val ingredientFrequencies = ingredientToMealIds.mapValues { it.value.size }
                val suggestion = recipeRanker.suggestIngredientToRemove(ingredientFrequencies)
                
                return Resource.Success(
                    RecipeSearchResult(
                        recipes = emptyList(),
                        searchedIngredients = normalizedIngredients,
                        suggestions = listOfNotNull(suggestion)
                    )
                )
            }

            // Fetch meal details (limit to 10 lookups as specified)
            val meals = fetchMealDetails(candidateMealIds.take(10))
            
            // Create match count map
            val mealIdToMatchCount = candidateMealIds.associateWith { mealId ->
                ingredientToMealIds.count { (_, ids) -> mealId in ids }
            }

            // Rank and score meals
            val rankedMeals = recipeRanker.rankMeals(meals, mealIdToMatchCount, normalizedIngredients)
            
            // Convert to recipe summaries and take top 5 sourced recipes
            val recipeSummaries = rankedMeals.take(5).map { (meal, matchInfo) ->
                meal.toRecipeSummary(matchInfo)
            }

            return Resource.Success(
                RecipeSearchResult(
                    recipes = recipeSummaries,
                    searchedIngredients = normalizedIngredients,
                    hasAiResults = false // Will be true when AI is implemented
                )
            )

        } catch (e: Exception) {
            return Resource.Error("Search failed: ${e.message}", e)
        }
    }

    override suspend fun getRecipeDetails(recipeId: String): Resource<Recipe> {
        try {
            // Try to get from cache first
            val cachedDetails = mealDetailsDao.getMealDetails(recipeId)
            if (cachedDetails != null && !cachedDetails.isExpired()) {
                val mealDetail = json.decodeFromString<MealDetailDto>(cachedDetails.detailsJson)
                return Resource.Success(mealDetail.toRecipe())
            }

            // Fetch from API
            val response = api.getMealById(recipeId)
            if (response.isSuccessful && response.body()?.meals?.isNotEmpty() == true) {
                val meal = response.body()!!.meals!!.first()
                
                // Cache the result
                val cacheEntity = MealDetailsEntity(
                    mealId = recipeId,
                    detailsJson = json.encodeToString(meal),
                    timestamp = System.currentTimeMillis()
                )
                mealDetailsDao.insertMealDetails(cacheEntity)
                
                return Resource.Success(meal.toRecipe())
            } else {
                return Resource.Error("Recipe not found")
            }
        } catch (e: Exception) {
            return Resource.Error("Failed to load recipe: ${e.message}", e)
        }
    }

    override suspend fun saveRecipe(recipe: Recipe) {
        val entity = SavedRecipeEntity(
            recipeId = recipe.id,
            recipeJson = json.encodeToString(recipe),
            savedAt = System.currentTimeMillis()
        )
        savedRecipeDao.saveRecipe(entity)
    }

    override suspend fun deleteSavedRecipe(recipeId: String) {
        savedRecipeDao.deleteSavedRecipe(recipeId)
    }

    override suspend fun isRecipeSaved(recipeId: String): Boolean {
        return savedRecipeDao.isRecipeSaved(recipeId)
    }

    override fun getSavedRecipes(): Flow<List<Recipe>> {
        return savedRecipeDao.getAllSavedRecipes().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    json.decodeFromString<Recipe>(entity.recipeJson)
                } catch (e: Exception) {
                    null // Skip corrupted entries
                }
            }
        }
    }

    override suspend fun clearCache() {
        filterCacheDao.clearAll()
        mealDetailsDao.clearAll()
    }

    private suspend fun getCachedOrFetchMealIds(ingredient: String): List<String>? {
        // Check cache first
        val cached = filterCacheDao.getFilterCache(ingredient)
        if (cached != null && !cached.isExpired()) {
            return json.decodeFromString<List<String>>(cached.mealIds)
        }

        // Fetch from API
        try {
            val response = api.getMealsByIngredient(ingredient)
            if (response.isSuccessful) {
                val mealIds = response.body()?.meals?.map { it.idMeal } ?: emptyList()
                
                // Cache the result
                val cacheEntity = FilterCacheEntity(
                    ingredient = ingredient,
                    mealIds = json.encodeToString(mealIds),
                    timestamp = System.currentTimeMillis()
                )
                filterCacheDao.insertFilterCache(cacheEntity)
                
                return mealIds
            }
        } catch (e: Exception) {
            // Return cached result even if expired on network error
            return cached?.let { json.decodeFromString<List<String>>(it.mealIds) }
        }
        
        return null
    }

    private suspend fun fetchMealDetails(mealIds: List<String>): List<MealDetailDto> {
        val meals = mutableListOf<MealDetailDto>()
        
        for (mealId in mealIds) {
            try {
                // Check cache first
                val cached = mealDetailsDao.getMealDetails(mealId)
                if (cached != null && !cached.isExpired()) {
                    val meal = json.decodeFromString<MealDetailDto>(cached.detailsJson)
                    meals.add(meal)
                    continue
                }

                // Fetch from API
                val response = api.getMealById(mealId)
                if (response.isSuccessful && response.body()?.meals?.isNotEmpty() == true) {
                    val meal = response.body()!!.meals!!.first()
                    meals.add(meal)
                    
                    // Cache the result
                    val cacheEntity = MealDetailsEntity(
                        mealId = mealId,
                        detailsJson = json.encodeToString(meal),
                        timestamp = System.currentTimeMillis()
                    )
                    mealDetailsDao.insertMealDetails(cacheEntity)
                }
            } catch (e: Exception) {
                // Continue with next meal on error
                continue
            }
        }
        
        return meals
    }
}

// Extension functions for mapping DTOs to domain models
private fun MealDetailDto.toRecipe(): Recipe {
    val ingredients = getIngredients().map { (name, measurement) ->
        RecipeIngredient(name, measurement)
    }
    
    return Recipe(
        id = idMeal,
        title = strMeal,
        imageUrl = strMealThumb,
        ingredients = ingredients,
        instructions = strInstructions,
        category = strCategory,
        area = strArea,
        tags = strTags?.split(",")?.map { it.trim() } ?: emptyList(),
        source = RecipeSource.TheMealDB,
        matchInfo = MatchInfo(
            matchedIngredients = emptyList(), // Will be set during search
            missingIngredients = emptyList(),
            isExactMatch = false,
            score = 0.0,
            isFilipino = strArea?.lowercase() == "filipino"
        )
    )
}

private fun MealDetailDto.toRecipeSummary(matchInfo: MatchInfo): RecipeSummary {
    return RecipeSummary(
        id = idMeal,
        title = strMeal,
        imageUrl = strMealThumb,
        source = RecipeSource.TheMealDB,
        matchInfo = matchInfo
    )
}