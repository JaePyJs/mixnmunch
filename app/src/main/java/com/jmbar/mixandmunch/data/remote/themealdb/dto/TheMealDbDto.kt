package com.jmbar.mixandmunch.data.remote.themealdb.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for TheMealDB API responses
 */

@Serializable
data class FilterResponse(
    val meals: List<MealFilterDto>?
)

@Serializable
data class MealFilterDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)

@Serializable
data class LookupResponse(
    val meals: List<MealDetailDto>?
)

@Serializable
data class MealDetailDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String?,
    val strArea: String?
)