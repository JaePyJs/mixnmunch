package com.jmbar.mixandmunch.domain.model

/**
 * Domain model for a recipe with all necessary information for display
 */
data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val ingredients: List<RecipeIngredient>,
    val instructions: String?,
    val category: String?,
    val area: String?,
    val tags: List<String>,
    val source: RecipeSource,
    val matchInfo: MatchInfo
)

data class RecipeIngredient(
    val name: String,
    val measurement: String
)

sealed class RecipeSource {
    object TheMealDB : RecipeSource()
    data class AI(val safetyNotes: List<String>) : RecipeSource()
}

data class MatchInfo(
    val matchedIngredients: List<String>,
    val missingIngredients: List<String>,
    val isExactMatch: Boolean,
    val score: Double,
    val isFilipino: Boolean
) {
    val isPartialMatch: Boolean get() = !isExactMatch
}

/**
 * Lightweight recipe summary for results display
 */
data class RecipeSummary(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val source: RecipeSource,
    val matchInfo: MatchInfo,
    val estimatedMinutes: Int? = null,
    val difficulty: String? = null
)

/**
 * Result wrapper for handling different UI states
 */
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val throwable: Throwable? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

/**
 * Search result container
 */
data class RecipeSearchResult(
    val recipes: List<RecipeSummary>,
    val searchedIngredients: List<String>,
    val suggestions: List<String> = emptyList(), // Ingredients to remove if no results
    val hasAiResults: Boolean = false
)