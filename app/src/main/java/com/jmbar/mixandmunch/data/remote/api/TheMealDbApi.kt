package com.jmbar.mixandmunch.data.remote.api

import com.jmbar.mixandmunch.data.remote.dto.MealDetailResponse
import com.jmbar.mixandmunch.data.remote.dto.MealFilterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TheMealDB API service with endpoints:
 * - filter.php?i={ingredient} - Get meals containing ingredient
 * - lookup.php?i={id} - Get meal details by ID
 */
interface TheMealDbApi {
    
    @GET("api/json/v1/1/filter.php")
    suspend fun getMealsByIngredient(
        @Query("i") ingredient: String
    ): Response<MealFilterResponse>
    
    @GET("api/json/v1/1/lookup.php")  
    suspend fun getMealById(
        @Query("i") mealId: String
    ): Response<MealDetailResponse>
    
    companion object {
        const val BASE_URL = "https://www.themealdb.com/"
    }
}