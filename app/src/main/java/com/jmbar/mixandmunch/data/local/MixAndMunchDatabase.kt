package com.jmbar.mixandmunch.data.local

import androidx.room.*
import com.jmbar.mixandmunch.data.local.dao.FilterCacheDao
import com.jmbar.mixandmunch.data.local.dao.MealDetailsDao
import com.jmbar.mixandmunch.data.local.dao.SavedRecipeDao
import com.jmbar.mixandmunch.data.local.entity.FilterCacheEntity
import com.jmbar.mixandmunch.data.local.entity.MealDetailsEntity
import com.jmbar.mixandmunch.data.local.entity.SavedRecipeEntity

@Database(
    entities = [
        FilterCacheEntity::class,
        MealDetailsEntity::class,
        SavedRecipeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MixAndMunchDatabase : RoomDatabase() {
    
    abstract fun filterCacheDao(): FilterCacheDao
    abstract fun mealDetailsDao(): MealDetailsDao
    abstract fun savedRecipeDao(): SavedRecipeDao
    
    companion object {
        const val DATABASE_NAME = "mix_and_munch_db"
    }
}