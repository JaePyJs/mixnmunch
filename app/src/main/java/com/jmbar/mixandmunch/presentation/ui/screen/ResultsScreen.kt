package com.jmbar.mixandmunch.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jmbar.mixandmunch.domain.model.RecipeSearchResult
import com.jmbar.mixandmunch.presentation.ui.components.IngredientChip
import com.jmbar.mixandmunch.presentation.ui.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    searchResult: RecipeSearchResult,
    onNavigateBack: () -> Unit,
    onNavigateToRecipeDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Search Results",
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Searched Ingredients
            Text(
                text = "Searched for:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResult.searchedIngredients) { ingredient ->
                    IngredientChip(
                        ingredient = ingredient,
                        onRemove = { } // Read-only in results
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (searchResult.recipes.isEmpty()) {
                // No results state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recipes found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Try different ingredients or remove some to get broader results",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (searchResult.suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "💡 Suggestion: Try removing '${searchResult.suggestions.first()}'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                // Results header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${searchResult.recipes.size} recipes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (searchResult.hasAiResults) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    "Includes AI recipes",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recipe List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(searchResult.recipes) { recipe ->
                        RecipeCard(
                            recipeSummary = recipe,
                            onClick = { onNavigateToRecipeDetail(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}