package com.jmbar.mixandmunch.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmbar.mixandmunch.domain.model.Recipe
import com.jmbar.mixandmunch.domain.model.Resource
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    var uiState by mutableStateOf(RecipeDetailUiState())
        private set

    fun loadRecipeDetails(recipeId: String) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // Load recipe details
                val result = recipeRepository.getRecipeDetails(recipeId)
                when (result) {
                    is Resource.Success -> {
                        uiState = uiState.copy(
                            recipe = result.data,
                            isLoading = false
                        )
                        // Check if saved
                        checkIfSaved(recipeId)
                    }
                    is Resource.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Failed to load recipe: ${e.message}"
                )
            }
        }
    }

    private suspend fun checkIfSaved(recipeId: String) {
        val isSaved = recipeRepository.isRecipeSaved(recipeId)
        uiState = uiState.copy(isSaved = isSaved)
    }

    fun onSaveToggle() {
        val recipe = uiState.recipe ?: return
        
        uiState = uiState.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                if (uiState.isSaved) {
                    recipeRepository.deleteSavedRecipe(recipe.id)
                    uiState = uiState.copy(isSaved = false, isSaving = false)
                } else {
                    recipeRepository.saveRecipe(recipe)
                    uiState = uiState.copy(isSaved = true, isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Failed to save recipe: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}