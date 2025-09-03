package com.jmbar.mixandmunch.domain.usecase

import com.jmbar.mixandmunch.domain.model.RecipeSummary
import com.jmbar.mixandmunch.domain.repository.RecipeRepository

/**
 * Use case for searching recipes by ingredients.
 * Platform-agnostic and delegates to repository.
 */
class SearchRecipesUseCase(private val repository: RecipeRepository) {
    
    /**
     * Searches for recipes based on raw ingredient inputs
     * 
     * @param rawInputs Raw ingredient strings from user
     * @param maxResults Maximum number of results to return (default 30)
     * @return List of recipe summaries ranked by match count
     */
    suspend operator fun invoke(rawInputs: List<String>, maxResults: Int = 30): List<RecipeSummary> {
        return repository.searchByIngredients(rawInputs, maxResults = maxResults)
    }
}