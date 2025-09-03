package com.jmbar.mixandmunch.data.local.dao

import androidx.room.*
import com.jmbar.mixandmunch.data.local.entity.FilterCacheEntity
import com.jmbar.mixandmunch.data.local.entity.MealDetailsEntity
import com.jmbar.mixandmunch.data.local.entity.SavedRecipeEntity
import com.jmbar.mixandmunch.data.local.entity.DemoRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterCacheDao {
    
    @Query("SELECT * FROM filter_cache WHERE ingredient = :ingredient LIMIT 1")
    suspend fun getFilterCache(ingredient: String): FilterCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterCache(cache: FilterCacheEntity)
    
    @Query("DELETE FROM filter_cache WHERE timestamp < :expiredBefore")
    suspend fun clearExpiredCache(expiredBefore: Long)
    
    @Query("DELETE FROM filter_cache")
    suspend fun clearAll()
}

@Dao
interface MealDetailsDao {
    
    @Query("SELECT * FROM meal_details_cache WHERE mealId = :mealId LIMIT 1")
    suspend fun getMealDetails(mealId: String): MealDetailsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDetails(details: MealDetailsEntity)
    
    @Query("DELETE FROM meal_details_cache WHERE timestamp < :expiredBefore")
    suspend fun clearExpiredCache(expiredBefore: Long)
    
    @Query("DELETE FROM meal_details_cache")
    suspend fun clearAll()
}

@Dao
interface SavedRecipeDao {
    
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE recipeId = :recipeId)")
    suspend fun isRecipeSaved(recipeId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: SavedRecipeEntity)
    
    @Query("DELETE FROM saved_recipes WHERE recipeId = :recipeId")
    suspend fun deleteSavedRecipe(recipeId: String)
    
    @Query("SELECT COUNT(*) FROM saved_recipes")
    suspend fun getSavedRecipeCount(): Int
}

@Dao
interface DemoRecipeDao {
    
    @Query("SELECT * FROM demo_recipes WHERE ingredients LIKE '%' || :ingredient || '%' LIMIT :limit")
    suspend fun getDemoRecipesByIngredient(ingredient: String, limit: Int = 5): List<DemoRecipeEntity>
    
    @Query("SELECT * FROM demo_recipes WHERE isFilipino = 1 LIMIT :limit")
    suspend fun getFilipinoRecipes(limit: Int = 10): List<DemoRecipeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemoRecipes(recipes: List<DemoRecipeEntity>)
    
    @Query("SELECT COUNT(*) FROM demo_recipes")
    suspend fun getDemoRecipeCount(): Int
}