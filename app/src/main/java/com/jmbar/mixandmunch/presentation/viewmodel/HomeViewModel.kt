package com.jmbar.mixandmunch.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmbar.mixandmunch.domain.model.Resource
import com.jmbar.mixandmunch.domain.model.RecipeSearchResult
import com.jmbar.mixandmunch.domain.usecase.SearchRecipesUseCase
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val ingredientInput: String = "",
    val normalizedIngredients: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val searchResult: Resource<RecipeSearchResult>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val ingredientNormalizer: IngredientNormalizer
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    fun onIngredientInputChanged(input: String) {
        uiState = uiState.copy(ingredientInput = input)
        
        // Show normalized ingredients in real-time
        if (input.isNotBlank()) {
            val ingredients = input.split(",", "\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val normalized = ingredientNormalizer.normalize(ingredients)
            uiState = uiState.copy(normalizedIngredients = normalized)
        } else {
            uiState = uiState.copy(normalizedIngredients = emptyList())
        }
    }

    fun onSearchClicked() {
        if (uiState.normalizedIngredients.isEmpty()) {
            uiState = uiState.copy(errorMessage = "Please enter at least one ingredient")
            return
        }

        uiState = uiState.copy(
            isSearching = true,
            errorMessage = null,
            searchResult = null
        )

        viewModelScope.launch {
            try {
                val result = searchRecipesUseCase(uiState.normalizedIngredients)
                uiState = uiState.copy(
                    isSearching = false,
                    searchResult = result
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSearching = false,
                    errorMessage = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun onRemoveIngredient(ingredient: String) {
        val currentIngredients = uiState.ingredientInput
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !ingredientNormalizer.normalize(listOf(it)).contains(ingredient) }
            .joinToString(", ")
        
        onIngredientInputChanged(currentIngredients)
    }

    fun onClearAll() {
        uiState = HomeUiState()
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}