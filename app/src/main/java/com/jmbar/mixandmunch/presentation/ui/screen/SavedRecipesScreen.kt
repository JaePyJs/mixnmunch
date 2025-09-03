package com.jmbar.mixandmunch.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmbar.mixandmunch.domain.model.Recipe
import com.jmbar.mixandmunch.presentation.viewmodel.SavedRecipesViewModel
import com.jmbar.mixandmunch.presentation.ui.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(
    onNavigateToRecipeDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedRecipesViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Saved Recipes",
                    fontWeight = FontWeight.Medium
                )
            }
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading saved recipes...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                uiState.savedRecipes.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.BookmarkBorder,
                                    contentDescription = "No saved recipes",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "No saved recipes yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "When you find recipes you like, save them here for offline access",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                else -> {
                    // Recipe List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "${uiState.savedRecipes.size} saved ${if (uiState.savedRecipes.size == 1) "recipe" else "recipes"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        items(uiState.savedRecipes, key = { it.id }) { recipe ->
                            SwipeToDeleteRecipeCard(
                                recipe = recipe,
                                onClick = { onNavigateToRecipeDetail(recipe.id) },
                                onDelete = { recipeToDelete = recipe }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    recipeToDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = {
                Text("Delete Recipe")
            },
            text = {
                Text("Are you sure you want to remove \"${recipe.title}\" from your saved recipes?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteRecipe(recipe.id)
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { recipeToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error Snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar
            viewModel.clearError()
        }
    }
}

@Composable
private fun SwipeToDeleteRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // For now, we'll add a simple delete button
    // TODO: Implement proper swipe-to-delete gesture
    
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Box {
            RecipeCard(
                recipeSummary = recipe.toRecipeSummary(),
                onClick = onClick
            )
            
            // Delete button overlay
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete recipe",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper extension to convert Recipe to RecipeSummary for display
private fun Recipe.toRecipeSummary() = com.jmbar.mixandmunch.domain.model.RecipeSummary(
    id = id,
    title = title,
    imageUrl = imageUrl,
    source = source,
    matchInfo = matchInfo
)