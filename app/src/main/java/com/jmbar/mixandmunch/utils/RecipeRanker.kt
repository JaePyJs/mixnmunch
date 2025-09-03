package com.jmbar.mixandmunch.utils

import com.jmbar.mixandmunch.data.remote.dto.MealDetailDto
import com.jmbar.mixandmunch.domain.model.MatchInfo
import com.jmbar.mixandmunch.domain.model.RecipeSummary
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recipe ranking and scoring logic as specified in project brief:
 * Score = 3× matchedCount + 2× title keyword match + 2× Filipino boost + 1× image bonus − 1× large missing-ingredient penalty
 */
@Singleton
class RecipeRanker @Inject constructor() {
    
    // Filipino keywords for boosting as specified
    private val filipinoKeywords = setOf(
        "filipino", "adobo", "sinigang", "ginisa", "tinola", 
        "inihaw", "bistek", "menudo", "afritada", "kaldereta", 
        "kare kare", "pinakbet"
    )
    
    /**
     * Ranks and scores meals based on ingredient matches and Filipino relevance
     */
    fun rankMeals(
        meals: List<MealDetailDto>,
        mealIdToMatchCount: Map<String, Int>,
        searchedIngredients: List<String>
    ): List<Pair<MealDetailDto, MatchInfo>> {
        
        return meals.map { meal ->
            val matchedCount = mealIdToMatchCount[meal.idMeal] ?: 0
            val matchInfo = calculateMatchInfo(meal, matchedCount, searchedIngredients)
            Pair(meal, matchInfo)
        }
        .sortedByDescending { it.second.score }
    }
    
    private fun calculateMatchInfo(
        meal: MealDetailDto,
        matchedCount: Int,
        searchedIngredients: List<String>
    ): MatchInfo {
        
        // Extract meal ingredients for comparison
        val mealIngredients = meal.getIngredients().map { it.first.lowercase().trim() }
        
        // Find matched and missing ingredients
        val matchedIngredients = searchedIngredients.filter { searchIngredient ->
            mealIngredients.any { mealIngredient ->
                mealIngredient.contains(searchIngredient.lowercase()) ||
                searchIngredient.lowercase().contains(mealIngredient)
            }
        }
        
        val missingIngredients = searchedIngredients - matchedIngredients.toSet()
        val isExactMatch = missingIngredients.isEmpty() && matchedIngredients.size == searchedIngredients.size
        
        // Calculate score using the specified formula
        val score = calculateScore(meal, matchedCount, searchedIngredients, missingIngredients)
        
        // Check if recipe is Filipino
        val isFilipino = isFilipinoDish(meal)
        
        return MatchInfo(
            matchedIngredients = matchedIngredients,
            missingIngredients = missingIngredients,
            isExactMatch = isExactMatch,
            score = score,
            isFilipino = isFilipino
        )
    }
    
    private fun calculateScore(
        meal: MealDetailDto,
        matchedCount: Int,
        searchedIngredients: List<String>,
        missingIngredients: List<String>
    ): Double {
        var score = 0.0
        
        // 3× matchedCount
        score += 3.0 * matchedCount
        
        // 2× title keyword match
        val titleMatchBonus = searchedIngredients.count { ingredient ->
            meal.strMeal.lowercase().contains(ingredient.lowercase())
        }
        score += 2.0 * titleMatchBonus
        
        // 2× Filipino boost
        if (isFilipinoDish(meal)) {
            score += 2.0
        }
        
        // 1× image bonus
        if (!meal.strMealThumb.isNullOrBlank()) {
            score += 1.0
        }
        
        // -1× large missing-ingredient penalty (if missing > 3 ingredients)
        if (missingIngredients.size > 3) {
            score -= 1.0
        }
        
        return score
    }
    
    private fun isFilipinoDish(meal: MealDetailDto): Boolean {
        val searchText = "${meal.strMeal} ${meal.strArea} ${meal.strCategory} ${meal.strTags}".lowercase()
        
        return filipinoKeywords.any { keyword ->
            searchText.contains(keyword)
        } || meal.strArea?.lowercase() == "filipino"
    }
    
    /**
     * Suggests which ingredient to remove if no results found
     * Returns the least common ingredient based on frequency
     */
    fun suggestIngredientToRemove(
        ingredientFrequencies: Map<String, Int>
    ): String? {
        return ingredientFrequencies.minByOrNull { it.value }?.key
    }
    
    /**
     * Determines if we should use union fallback (partial matches)
     * when intersection is empty
     */
    fun shouldUseUnionFallback(
        ingredientToMealIds: Map<String, List<String>>
    ): Boolean {
        // If no ingredient has any results, or intersection is empty but union exists
        val allMealIds = ingredientToMealIds.values.flatten().toSet()
        val intersection = ingredientToMealIds.values.reduce { acc, list ->
            acc.intersect(list.toSet()).toList()
        }
        
        return intersection.isEmpty() && allMealIds.isNotEmpty()
    }
}