package com.jmbar.mixandmunch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache entity for ingredient filter results with TTL
 */
@Entity(tableName = "filter_cache")
data class FilterCacheEntity(
    @PrimaryKey
    val ingredient: String,
    val mealIds: String, // JSON serialized List<String>
    val timestamp: Long
) {
    fun isExpired(ttlHours: Long = 24): Boolean {
        val ageHours = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
        return ageHours > ttlHours
    }
}

/**
 * Cache entity for meal details with TTL
 */
@Entity(tableName = "meal_details_cache")
data class MealDetailsEntity(
    @PrimaryKey
    val mealId: String,
    val detailsJson: String, // JSON serialized MealDetailDto
    val timestamp: Long
) {
    fun isExpired(ttlDays: Long = 7): Boolean {
        val ageDays = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
        return ageDays > ttlDays
    }
}

/**
 * Entity for user-saved recipes (permanent storage)
 */
@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey
    val recipeId: String,
    val recipeJson: String, // JSON serialized Recipe
    val savedAt: Long
)

/**
 * Demo pack entity for offline fallback recipes
 */
@Entity(tableName = "demo_recipes")
data class DemoRecipeEntity(
    @PrimaryKey
    val recipeId: String,
    val title: String,
    val ingredients: String, // JSON serialized List<String>
    val recipeJson: String, // JSON serialized Recipe
    val isFilipino: Boolean,
    val createdAt: Long
)