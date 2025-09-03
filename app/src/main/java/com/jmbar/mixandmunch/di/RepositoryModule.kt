package com.jmbar.mixandmunch.di

import com.jmbar.mixandmunch.data.remote.themealdb.TheMealDbService
import com.jmbar.mixandmunch.data.repository.TheMealDbRepository
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideRecipeRepository(service: TheMealDbService): RecipeRepository {
        return TheMealDbRepository(service)
    }
}