package com.jmbar.mixandmunch.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jmbar.mixandmunch.data.local.MixAndMunchDatabase
import com.jmbar.mixandmunch.data.remote.api.TheMealDbApi
import com.jmbar.mixandmunch.data.remote.themealdb.TheMealDbService
import com.jmbar.mixandmunch.data.repository.RecipeRepositoryImpl
import com.jmbar.mixandmunch.data.repository.TheMealDbRepository
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import com.jmbar.mixandmunch.domain.usecase.SearchRecipesUseCase
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import com.jmbar.mixandmunch.utils.RecipeRanker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    @Provides
    @Singleton  
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS) // 5s connect timeout as specified
            .readTimeout(10, TimeUnit.SECONDS)   // 10s read timeout as specified
            .retryOnConnectionFailure(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC // BASIC logging as specified
                }
            )
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TheMealDbApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTheMealDbApi(retrofit: Retrofit): TheMealDbApi {
        return retrofit.create(TheMealDbApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTheMealDbService(retrofit: Retrofit): TheMealDbService {
        return retrofit.create(TheMealDbService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MixAndMunchDatabase {
        return Room.databaseBuilder(
            context,
            MixAndMunchDatabase::class.java,
            MixAndMunchDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideFilterCacheDao(database: MixAndMunchDatabase) = database.filterCacheDao()
    
    @Provides
    fun provideMealDetailsDao(database: MixAndMunchDatabase) = database.mealDetailsDao()
    
    @Provides
    fun provideSavedRecipeDao(database: MixAndMunchDatabase) = database.savedRecipeDao()
}

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {
    
    @Provides
    @Singleton
    fun provideIngredientNormalizer(): IngredientNormalizer = IngredientNormalizer
    
    @Provides
    @Singleton
    fun provideRecipeRanker(): RecipeRanker = RecipeRanker()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository
}