package com.jmbar.mixandmunch.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmbar.mixandmunch.domain.model.Resource
import com.jmbar.mixandmunch.presentation.viewmodel.HomeViewModel
import com.jmbar.mixandmunch.presentation.ui.components.IngredientChip
import com.jmbar.mixandmunch.presentation.ui.components.RecipeCard
import com.jmbar.mixandmunch.presentation.ui.components.ErrorSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToResults: (List<String>) -> Unit,
    onNavigateToRecipeDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Mix & Munch",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Filipino Recipe Finder",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Ingredient Input Section
        Text(
            text = "What ingredients do you have?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Enter ingredients in Filipino or English (up to 6)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Input Field
        OutlinedTextField(
            value = uiState.ingredientInput,
            onValueChange = viewModel::onIngredientInputChanged,
            label = { Text("sibuyas, bawang, kamatis...") },
            placeholder = { Text("Enter ingredients separated by commas") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            trailingIcon = {
                Row {
                    if (uiState.ingredientInput.isNotEmpty()) {
                        IconButton(onClick = viewModel::onClearAll) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(
                        onClick = viewModel::onSearchClicked,
                        enabled = !uiState.isSearching && uiState.normalizedIngredients.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Normalized Ingredients Display
        if (uiState.normalizedIngredients.isNotEmpty()) {
            Text(
                text = "Normalized ingredients:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.normalizedIngredients) { ingredient ->
                    IngredientChip(
                        ingredient = ingredient,
                        onRemove = { viewModel.onRemoveIngredient(ingredient) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Search Button
        Button(
            onClick = viewModel::onSearchClicked,
            enabled = !uiState.isSearching && uiState.normalizedIngredients.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isSearching) "Searching..." else "Find Recipes",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        when (val result = uiState.searchResult) {
            is Resource.Success -> {
                if (result.data.recipes.isEmpty()) {
                    // No results found
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No recipes found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try removing some ingredients or use different ones",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            
                            if (result.data.suggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Suggestion: Remove '${result.data.suggestions.first()}'",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // Show results
                    Text(
                        text = "Found ${result.data.recipes.size} recipes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(result.data.recipes) { recipe ->
                            RecipeCard(
                                recipeSummary = recipe,
                                onClick = { onNavigateToRecipeDetail(recipe.id) }
                            )
                        }
                    }
                }
            }
            
            is Resource.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            null -> {
                // Initial state - show example ingredients
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Try these Filipino ingredients:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val examples = listOf(
                            "sibuyas, bawang, kamatis",
                            "manok, patatas, sitaw", 
                            "baboy, toyo, suka"
                        )
                        
                        examples.forEach { example ->
                            TextButton(
                                onClick = { viewModel.onIngredientInputChanged(example) }
                            ) {
                                Text(
                                    text = example,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Error Snackbar
    uiState.errorMessage?.let { error ->
        ErrorSnackbar(
            message = error,
            onDismiss = viewModel::clearError
        )
    }
}