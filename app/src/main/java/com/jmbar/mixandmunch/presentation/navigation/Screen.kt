package com.jmbar.mixandmunch.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Results : Screen("results", "Results", Icons.Default.Home)
    object RecipeDetails : Screen("recipe_details/{recipeId}", "Recipe", Icons.Default.Home) {
        fun createRoute(recipeId: String) = "recipe_details/$recipeId"
    }
    object Saved : Screen("saved", "Saved", Icons.Default.Favorite)
    object About : Screen("about", "About", Icons.Default.Info)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Saved,
    Screen.About
)