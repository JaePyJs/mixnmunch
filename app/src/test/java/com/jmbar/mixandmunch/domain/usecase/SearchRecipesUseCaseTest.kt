package com.jmbar.mixandmunch.domain.usecase

import com.jmbar.mixandmunch.domain.model.RecipeSummary
import com.jmbar.mixandmunch.domain.repository.RecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SearchRecipesUseCaseTest {

    @Test
    fun `use case delegates to repository with correct parameters`() = runTest {
        // Given
        val expectedResults = listOf(
            RecipeSummary("1", "Recipe 1", "thumb1.jpg", 2),
            RecipeSummary("2", "Recipe 2", "thumb2.jpg", 1)
        )
        
        val fakeRepository = FakeRecipeRepository(expectedResults)
        val useCase = SearchRecipesUseCase(fakeRepository)
        
        // When
        val result = useCase(listOf("tomato", "onion"), maxResults = 10)
        
        // Then
        assertEquals(expectedResults, result)
        assertEquals(listOf("tomato", "onion"), fakeRepository.lastRawInputs)
        assertEquals(10, fakeRepository.lastMaxResults)
    }

    @Test
    fun `use case uses default maxResults when not specified`() = runTest {
        // Given
        val fakeRepository = FakeRecipeRepository(emptyList())
        val useCase = SearchRecipesUseCase(fakeRepository)
        
        // When
        useCase(listOf("tomato"))
        
        // Then
        assertEquals(30, fakeRepository.lastMaxResults) // default value
    }

    @Test
    fun `use case handles empty input`() = runTest {
        // Given
        val fakeRepository = FakeRecipeRepository(emptyList())
        val useCase = SearchRecipesUseCase(fakeRepository)
        
        // When
        val result = useCase(emptyList())
        
        // Then
        assertEquals(emptyList<RecipeSummary>(), result)
        assertEquals(emptyList<String>(), fakeRepository.lastRawInputs)
    }

    @Test
    fun `use case preserves repository order`() = runTest {
        // Given - repository returns results in specific order
        val orderedResults = listOf(
            RecipeSummary("high-match", "High Match Recipe", "thumb1.jpg", 3),
            RecipeSummary("medium-match", "Medium Match Recipe", "thumb2.jpg", 2),
            RecipeSummary("low-match", "Low Match Recipe", "thumb3.jpg", 1)
        )
        
        val fakeRepository = FakeRecipeRepository(orderedResults)
        val useCase = SearchRecipesUseCase(fakeRepository)
        
        // When
        val result = useCase(listOf("tomato", "onion", "garlic"))
        
        // Then - order should be preserved
        assertEquals(orderedResults, result)
        assertEquals("high-match", result[0].id)
        assertEquals("medium-match", result[1].id)
        assertEquals("low-match", result[2].id)
    }
}

/**
 * Fake implementation of RecipeRepository for testing
 */
private class FakeRecipeRepository(
    private val resultsToReturn: List<RecipeSummary>
) : RecipeRepository {
    
    var lastRawInputs: List<String> = emptyList()
    var lastMaxResults: Int = 0
    
    override suspend fun searchByIngredients(
        rawInputs: List<String>,
        maxNormalized: Int,
        maxResults: Int
    ): List<RecipeSummary> {
        lastRawInputs = rawInputs
        lastMaxResults = maxResults
        return resultsToReturn
    }
}