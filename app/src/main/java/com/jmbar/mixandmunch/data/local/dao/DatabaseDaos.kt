package com.jmbar.mixandmunch.data.local.dao

import androidx.room.*
import com.jmbar.mixandmunch.data.local.entity.FilterCacheEntity
import com.jmbar.mixandmunch.data.local.entity.MealDetailsEntity
import com.jmbar.mixandmunch.data.local.entity.SavedRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterCacheDao {
    
    @Query("SELECT * FROM filter_cache WHERE ingredient = :ingredient")
    suspend fun getFilterCache(ingredient: String): FilterCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterCache(cache: FilterCacheEntity)
    
    @Query("DELETE FROM filter_cache WHERE timestamp + ttl < :currentTime")
    suspend fun deleteExpiredCache(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM filter_cache")
    suspend fun clearAll()
}

@Dao 
interface MealDetailsDao {
    
    @Query("SELECT * FROM meal_details WHERE mealId = :mealId")
    suspend fun getMealDetails(mealId: String): MealDetailsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDetails(details: MealDetailsEntity)
    
    @Query("DELETE FROM meal_details WHERE timestamp + ttl < :currentTime")
    suspend fun deleteExpiredDetails(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM meal_details")
    suspend fun clearAll()
}

@Dao
interface SavedRecipeDao {
    
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>>
    
    @Query("SELECT * FROM saved_recipes WHERE recipeId = :recipeId")
    suspend fun getSavedRecipe(recipeId: String): SavedRecipeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: SavedRecipeEntity)
    
    @Query("DELETE FROM saved_recipes WHERE recipeId = :recipeId")
    suspend fun deleteSavedRecipe(recipeId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE recipeId = :recipeId)")
    suspend fun isRecipeSaved(recipeId: String): Boolean
    
    @Query("DELETE FROM saved_recipes")
    suspend fun clearAll()
}