package com.jmbar.mixandmunch.data.remote.themealdb

import com.jmbar.mixandmunch.data.remote.themealdb.dto.FilterResponse
import com.jmbar.mixandmunch.data.remote.themealdb.dto.LookupResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for TheMealDB API
 */
interface TheMealDbService {
    
    /**
     * Filter meals by ingredient
     * GET filter.php?i={ingredient}
     * Returns minimal meals (idMeal, strMeal, strMealThumb) or {"meals": null} if none
     */
    @GET("api/json/v1/1/filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): FilterResponse
    
    /**
     * Lookup meal details by ID
     * GET lookup.php?i={id}
     * Returns full meal details
     */
    @GET("api/json/v1/1/lookup.php")
    suspend fun lookupById(@Query("i") id: String): LookupResponse
}