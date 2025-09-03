package com.jmbar.mixandmunch.domain.repository

import com.jmbar.mixandmunch.domain.model.RecipeSummary

/**
 * Repository contract for searching recipes by ingredients
 */
interface RecipeRepository {
    /**
     * Searches recipes by ingredients
     * 
     * @param rawInputs Raw ingredient strings from user input
     * @param maxNormalized Maximum number of normalized ingredients to use (default 6)
     * @param maxResults Maximum number of results to return (default 30)
     * @return List of recipe summaries sorted by match count descending, then name ascending
     */
    suspend fun searchByIngredients(
        rawInputs: List<String>, 
        maxNormalized: Int = 6, 
        maxResults: Int = 30
    ): List<RecipeSummary>
}