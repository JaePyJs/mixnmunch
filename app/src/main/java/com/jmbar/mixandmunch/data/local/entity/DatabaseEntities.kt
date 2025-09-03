package com.jmbar.mixandmunch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache for ingredient filter results with 24h TTL
 */
@Entity(tableName = "filter_cache")
data class FilterCacheEntity(
    @PrimaryKey val ingredient: String,
    val mealIds: String, // JSON array of meal IDs
    val timestamp: Long,
    val ttl: Long = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > (timestamp + ttl)
}

/**
 * Cache for meal details with 7d TTL
 */
@Entity(tableName = "meal_details")
data class MealDetailsEntity(
    @PrimaryKey val mealId: String,
    val detailsJson: String, // Full MealDetailDto as JSON
    val timestamp: Long,
    val ttl: Long = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > (timestamp + ttl)
}

/**
 * User saved recipes for offline access
 */
@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey val recipeId: String,
    val recipeJson: String, // Full Recipe domain model as JSON
    val savedAt: Long = System.currentTimeMillis()
)