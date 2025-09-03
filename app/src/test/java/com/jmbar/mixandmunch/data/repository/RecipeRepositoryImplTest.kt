package com.jmbar.mixandmunch.data.repository

import com.jmbar.mixandmunch.data.local.dao.FilterCacheDao
import com.jmbar.mixandmunch.data.local.dao.MealDetailsDao
import com.jmbar.mixandmunch.data.local.dao.SavedRecipeDao
import com.jmbar.mixandmunch.data.local.entity.FilterCacheEntity
import com.jmbar.mixandmunch.data.local.entity.MealDetailsEntity
import com.jmbar.mixandmunch.data.remote.api.TheMealDbApi
import com.jmbar.mixandmunch.data.remote.dto.*
import com.jmbar.mixandmunch.domain.model.Resource
import com.jmbar.mixandmunch.utils.IngredientNormalizer
import com.jmbar.mixandmunch.utils.RecipeRanker
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

class RecipeRepositoryImplTest {

    private val mockApi = mockk<TheMealDbApi>()
    private val mockFilterCacheDao = mockk<FilterCacheDao>()
    private val mockMealDetailsDao = mockk<MealDetailsDao>()
    private val mockSavedRecipeDao = mockk<SavedRecipeDao>()
    private val mockIngredientNormalizer = mockk<IngredientNormalizer>()
    private val mockRecipeRanker = mockk<RecipeRanker>()
    private val json = Json { ignoreUnknownKeys = true }
    
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setup() {
        repository = RecipeRepositoryImpl(
            api = mockApi,
            filterCacheDao = mockFilterCacheDao,
            mealDetailsDao = mockMealDetailsDao,
            savedRecipeDao = mockSavedRecipeDao,
            ingredientNormalizer = mockIngredientNormalizer,
            recipeRanker = mockRecipeRanker,
            json = json
        )
    }

    @Test
    fun `searchRecipes with empty normalized ingredients returns error`() = runTest {
        // Given
        every { mockIngredientNormalizer.normalize(any<List<String>>()) } returns emptyList()
        
        // When
        val result = repository.searchRecipes(listOf(""))
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Please enter at least one ingredient", (result as Resource.Error).message)
    }

    @Test
    fun `searchRecipes uses cached results when available and not expired`() = runTest {
        // Given
        val ingredients = listOf("onion", "garlic")
        every { mockIngredientNormalizer.normalize(any<List<String>>()) } returns ingredients
        
        val cachedEntity = FilterCacheEntity(
            ingredient = "onion",
            mealIds = "[\"1\", \"2\"]",
            timestamp = System.currentTimeMillis()
        )
        coEvery { mockFilterCacheDao.getFilterCache("onion") } returns cachedEntity
        coEvery { mockFilterCacheDao.getFilterCache("garlic") } returns null
        coEvery { mockApi.getMealsByIngredient("garlic") } returns Response.success(
            MealFilterResponse(listOf(MealFilterDto("Test Meal", "image.jpg", "2")))
        )
        
        val mockMeal = createMockMealDetail()
        coEvery { mockApi.getMealById(any()) } returns Response.success(
            MealDetailResponse(listOf(mockMeal))
        )
        
        every { mockRecipeRanker.rankMeals(any(), any(), any()) } returns listOf(
            Pair(mockMeal, createMockMatchInfo())
        )
        
        coEvery { mockFilterCacheDao.insertFilterCache(any()) } just Runs
        coEvery { mockMealDetailsDao.insertMealDetails(any()) } just Runs
        
        // When
        val result = repository.searchRecipes(listOf("sibuyas", "bawang"))
        
        // Then
        assertTrue(result is Resource.Success)
        verify { mockFilterCacheDao.getFilterCache("onion") }
        coVerify { mockApi.getMealsByIngredient("garlic") }
        coVerify(exactly = 0) { mockApi.getMealsByIngredient("onion") } // Should use cache
    }

    @Test
    fun `searchRecipes handles API errors gracefully`() = runTest {
        // Given
        every { mockIngredientNormalizer.normalize(any<List<String>>()) } returns listOf("onion")
        coEvery { mockFilterCacheDao.getFilterCache(any()) } returns null
        coEvery { mockApi.getMealsByIngredient(any()) } throws Exception("Network error")
        
        // When
        val result = repository.searchRecipes(listOf("onion"))
        
        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("Search failed"))
    }

    @Test
    fun `getRecipeDetails uses cache when available and not expired`() = runTest {
        // Given
        val recipeId = "123"
        val mockMeal = createMockMealDetail()
        val cachedEntity = MealDetailsEntity(
            mealId = recipeId,
            detailsJson = json.encodeToString(MealDetailDto.serializer(), mockMeal),
            timestamp = System.currentTimeMillis()
        )
        
        coEvery { mockMealDetailsDao.getMealDetails(recipeId) } returns cachedEntity
        
        // When
        val result = repository.getRecipeDetails(recipeId)
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockMeal.strMeal, (result as Resource.Success).data.title)
        coVerify(exactly = 0) { mockApi.getMealById(any()) } // Should use cache
    }

    @Test
    fun `getRecipeDetails fetches from API when cache expired`() = runTest {
        // Given
        val recipeId = "123"
        val expiredEntity = MealDetailsEntity(
            mealId = recipeId,
            detailsJson = "",
            timestamp = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000) // 8 days old
        )
        val mockMeal = createMockMealDetail()
        
        coEvery { mockMealDetailsDao.getMealDetails(recipeId) } returns expiredEntity
        coEvery { mockApi.getMealById(recipeId) } returns Response.success(
            MealDetailResponse(listOf(mockMeal))
        )
        coEvery { mockMealDetailsDao.insertMealDetails(any()) } just Runs
        
        // When
        val result = repository.getRecipeDetails(recipeId)
        
        // Then
        assertTrue(result is Resource.Success)
        coVerify { mockApi.getMealById(recipeId) }
        coVerify { mockMealDetailsDao.insertMealDetails(any()) }
    }

    @Test
    fun `saveRecipe stores recipe in database`() = runTest {
        // Given
        val recipe = createMockRecipe()
        coEvery { mockSavedRecipeDao.saveRecipe(any()) } just Runs
        
        // When
        repository.saveRecipe(recipe)
        
        // Then
        coVerify { mockSavedRecipeDao.saveRecipe(any()) }
    }

    @Test
    fun `isRecipeSaved returns correct status`() = runTest {
        // Given
        val recipeId = "123"
        coEvery { mockSavedRecipeDao.isRecipeSaved(recipeId) } returns true
        
        // When
        val result = repository.isRecipeSaved(recipeId)
        
        // Then
        assertTrue(result)
        coVerify { mockSavedRecipeDao.isRecipeSaved(recipeId) }
    }

    private fun createMockMealDetail() = MealDetailDto(
        idMeal = "1",
        strMeal = "Test Adobo",
        strArea = "Filipino",
        strCategory = "Chicken",
        strInstructions = "Test instructions",
        strMealThumb = "image.jpg",
        strIngredient1 = "Chicken",
        strIngredient2 = "Soy Sauce",
        strMeasure1 = "1 kg",
        strMeasure2 = "1/2 cup"
    )

    private fun createMockMatchInfo() = com.jmbar.mixandmunch.domain.model.MatchInfo(
        matchedIngredients = listOf("chicken"),
        missingIngredients = emptyList(),
        isExactMatch = true,
        score = 5.0,
        isFilipino = true
    )

    private fun createMockRecipe() = com.jmbar.mixandmunch.domain.model.Recipe(
        id = "1",
        title = "Test Recipe",
        imageUrl = "image.jpg",
        ingredients = emptyList(),
        instructions = "Test instructions",
        category = "Chicken",
        area = "Filipino",
        tags = emptyList(),
        source = com.jmbar.mixandmunch.domain.model.RecipeSource.TheMealDB,
        matchInfo = createMockMatchInfo()
    )
}