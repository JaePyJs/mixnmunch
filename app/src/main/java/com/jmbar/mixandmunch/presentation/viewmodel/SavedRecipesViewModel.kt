package com.jmbar.mixandmunch.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmbar.mixandmunch.domain.model.Recipe
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedRecipesUiState(
    val savedRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SavedRecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    var uiState by mutableStateOf(SavedRecipesUiState())
        private set

    init {
        loadSavedRecipes()
    }

    private fun loadSavedRecipes() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                recipeRepository.getSavedRecipes().collectLatest { recipes ->
                    uiState = uiState.copy(
                        savedRecipes = recipes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Failed to load saved recipes: ${e.message}"
                )
            }
        }
    }

    fun onDeleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.deleteSavedRecipe(recipeId)
                // The UI will update automatically through the Flow
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Failed to delete recipe: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}