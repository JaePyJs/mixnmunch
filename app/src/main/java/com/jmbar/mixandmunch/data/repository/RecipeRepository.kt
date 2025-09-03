package com.jmbar.mixandmunch.data.repository

import com.jmbar.mixandmunch.domain.model.Recipe
import com.jmbar.mixandmunch.domain.model.RecipeSearchResult
import com.jmbar.mixandmunch.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    
    suspend fun searchRecipes(ingredients: List<String>): Resource<RecipeSearchResult>
    
    suspend fun getRecipeDetails(recipeId: String): Resource<Recipe>
    
    suspend fun saveRecipe(recipe: Recipe)
    
    suspend fun deleteSavedRecipe(recipeId: String)
    
    suspend fun isRecipeSaved(recipeId: String): Boolean
    
    fun getSavedRecipes(): Flow<List<Recipe>>
    
    suspend fun clearCache()
}