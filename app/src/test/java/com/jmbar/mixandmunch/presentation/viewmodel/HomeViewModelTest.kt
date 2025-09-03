package com.jmbar.mixandmunch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jmbar.mixandmunch.domain.model.*
import com.jmbar.mixandmunch.domain.usecase.SearchRecipesUseCase
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockSearchRecipesUseCase = mockk<SearchRecipesUseCase>()
    private val mockIngredientNormalizer = mockk<IngredientNormalizer>()
    
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockIngredientNormalizer.normalize(any<List<String>>()) } returns listOf("onion", "garlic")
        viewModel = HomeViewModel(mockSearchRecipesUseCase, mockIngredientNormalizer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ingredient input change updates state correctly`() {
        // Given
        val input = "sibuyas, bawang"
        
        // When
        viewModel.onIngredientInputChanged(input)
        
        // Then
        assertEquals(input, viewModel.uiState.ingredientInput)
        assertEquals(listOf("onion", "garlic"), viewModel.uiState.normalizedIngredients)
        verify { mockIngredientNormalizer.normalize(listOf("sibuyas", "bawang")) }
    }

    @Test
    fun `search with empty ingredients shows error`() {
        // Given
        every { mockIngredientNormalizer.normalize(any<List<String>>()) } returns emptyList()
        
        // When
        viewModel.onSearchClicked()
        
        // Then
        assertEquals("Please enter at least one ingredient", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isSearching)
        verify { mockIngredientNormalizer.normalize(any<List<String>>()) }
    }

    @Test
    fun `successful search updates result state`() = runTest {
        // Given
        val searchResult = RecipeSearchResult(
            recipes = listOf(createMockRecipeSummary()),
            searchedIngredients = listOf("onion", "garlic")
        )
        coEvery { mockSearchRecipesUseCase(any()) } returns Resource.Success(searchResult)
        
        viewModel.onIngredientInputChanged("sibuyas, bawang")
        
        // When
        viewModel.onSearchClicked()
        
        // Then
        assertEquals(Resource.Success(searchResult), viewModel.uiState.searchResult)
        assertFalse(viewModel.uiState.isSearching)
        assertNull(viewModel.uiState.errorMessage)
        coVerify { mockSearchRecipesUseCase(listOf("onion", "garlic")) }
    }

    @Test
    fun `failed search shows error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockSearchRecipesUseCase(any()) } returns Resource.Error(errorMessage)
        
        viewModel.onIngredientInputChanged("sibuyas, bawang")
        
        // When
        viewModel.onSearchClicked()
        
        // Then
        assertEquals("Search failed: $errorMessage", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isSearching)
        assertNull(viewModel.uiState.searchResult)
    }

    @Test
    fun `clear all resets state`() {
        // Given
        viewModel.onIngredientInputChanged("sibuyas, bawang")
        
        // When
        viewModel.onClearAll()
        
        // Then
        assertEquals(HomeUiState(), viewModel.uiState)
    }

    @Test
    fun `remove ingredient updates input correctly`() {
        // Given
        every { mockIngredientNormalizer.normalize(listOf("sibuyas")) } returns listOf("onion")
        every { mockIngredientNormalizer.normalize(listOf("sibuyas", "bawang")) } returns listOf("onion", "garlic")
        
        viewModel.onIngredientInputChanged("sibuyas, bawang")
        
        // When
        viewModel.onRemoveIngredient("garlic")
        
        // Then
        assertEquals("sibuyas", viewModel.uiState.ingredientInput)
        assertEquals(listOf("onion"), viewModel.uiState.normalizedIngredients)
    }

    @Test
    fun `search sets loading state initially`() {
        // Given
        val slot = slot<List<String>>()
        coEvery { mockSearchRecipesUseCase(capture(slot)) } coAnswers {
            // Simulate delay
            kotlinx.coroutines.delay(100)
            Resource.Success(RecipeSearchResult(emptyList(), slot.captured))
        }
        
        viewModel.onIngredientInputChanged("sibuyas")
        
        // When
        viewModel.onSearchClicked()
        
        // Then - check loading state is set initially
        assertTrue(viewModel.uiState.isSearching)
    }

    private fun createMockRecipeSummary() = RecipeSummary(
        id = "1",
        title = "Test Recipe",
        imageUrl = "https://example.com/image.jpg",
        source = RecipeSource.TheMealDB,
        matchInfo = MatchInfo(
            matchedIngredients = listOf("onion"),
            missingIngredients = emptyList(),
            isExactMatch = true,
            score = 5.0,
            isFilipino = true
        )
    )
}