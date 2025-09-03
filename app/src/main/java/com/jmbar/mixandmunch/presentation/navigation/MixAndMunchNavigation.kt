package com.jmbar.mixandmunch.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.jmbar.mixandmunch.presentation.ui.screen.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixAndMunchNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine if we should show bottom bar
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToResults = { ingredients ->
                        // For now, we'll handle results in the home screen itself
                        // In the future, you could navigate to a separate results screen
                    },
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(Screen.RecipeDetails.createRoute(recipeId))
                    }
                )
            }
            
            composable(
                Screen.RecipeDetails.route,
                arguments = listOf(navArgument(\"recipeId\") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString(\"recipeId\") ?: \"\"
                RecipeDetailsScreen(
                    recipeId = recipeId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Saved.route) {
                SavedRecipesScreen(
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(Screen.RecipeDetails.createRoute(recipeId))
                    }
                )
            }
            
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}