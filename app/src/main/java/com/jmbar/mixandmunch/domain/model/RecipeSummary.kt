package com.jmbar.mixandmunch.domain.model

/**
 * Summary of a recipe with basic information and match count for ranking
 */
data class RecipeSummary(
    val id: String,
    val name: String,
    val thumb: String?,
    val matchedCount: Int
)