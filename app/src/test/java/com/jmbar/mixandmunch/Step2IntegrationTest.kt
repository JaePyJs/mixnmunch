package com.jmbar.mixandmunch

import com.jmbar.mixandmunch.data.remote.themealdb.TheMealDbService
import com.jmbar.mixandmunch.data.remote.themealdb.dto.FilterResponse
import com.jmbar.mixandmunch.data.remote.themealdb.dto.MealFilterDto
import com.jmbar.mixandmunch.data.repository.TheMealDbRepository
import com.jmbar.mixandmunch.domain.usecase.SearchRecipesUseCase
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests to verify the complete Step 2 flow
 */
class Step2IntegrationTest {

    @Test
    fun `test complete flow from raw inputs to recipe summaries`() = runTest {
        // Given - Mock service that returns test data
        val mockService = object : TheMealDbService {
            override suspend fun filterByIngredient(ingredient: String): FilterResponse {
                return when (ingredient) {
                    "tomato" -> FilterResponse(
                        meals = listOf(
                            MealFilterDto("1", "Tomato Soup", "thumb1.jpg"),
                            MealFilterDto("2", "Tomato Pasta", "thumb2.jpg")
                        )
                    )
                    "onion" -> FilterResponse(
                        meals = listOf(
                            MealFilterDto("2", "Tomato Pasta", "thumb2.jpg"), // overlapping with tomato
                            MealFilterDto("3", "Onion Rings", "thumb3.jpg")
                        )
                    )
                    else -> FilterResponse(meals = null)
                }
            }

            override suspend fun lookupById(id: String) = throw NotImplementedError("Not needed for Step 2")
        }

        val repository = TheMealDbRepository(mockService)
        val useCase = SearchRecipesUseCase(repository)

        // When - Search with Filipino/typo inputs that should normalize to "tomato" and "onion"
        val result = useCase(listOf("t0mat0", "sibuyas"), maxResults = 10)

        // Then
        assertEquals(3, result.size)
        
        // Verify ranking: "Tomato Pasta" should have matchedCount=2 and rank first
        val tomatoPasta = result.find { it.name == "Tomato Pasta" }
        assertNotNull(tomatoPasta)
        assertEquals(2, tomatoPasta!!.matchedCount)
        assertEquals("2", tomatoPasta.id)
        
        // Verify other results have matchedCount=1
        val tomatoSoup = result.find { it.name == "Tomato Soup" }
        val onionRings = result.find { it.name == "Onion Rings" }
        assertNotNull(tomatoSoup)
        assertNotNull(onionRings)
        assertEquals(1, tomatoSoup!!.matchedCount)
        assertEquals(1, onionRings!!.matchedCount)
        
        // Verify ranking order: matchedCount=2 should come before matchedCount=1
        assertTrue("Tomato Pasta should rank first", result.indexOf(tomatoPasta) == 0)
    }

    @Test
    fun `test normalizer integration with brands and typos`() {
        // Test that the normalizer properly handles the specified pipeline
        val inputs = listOf("Knorr", "t0mat0", "sibuyas", "magic sarap")
        val normalized = IngredientNormalizer.normalize(inputs)
        
        // Should only have "tomato" and "onion" (brands filtered out)
        assertEquals(listOf("tomato", "onion"), normalized)
    }

    @Test
    fun `test empty and error handling`() = runTest {
        val mockService = object : TheMealDbService {
            override suspend fun filterByIngredient(ingredient: String): FilterResponse {
                return FilterResponse(meals = null) // No results
            }
            override suspend fun lookupById(id: String) = throw NotImplementedError()
        }

        val repository = TheMealDbRepository(mockService)
        val result = repository.searchByIngredients(listOf("unknown"))
        
        assertEquals(emptyList(), result)
    }
}