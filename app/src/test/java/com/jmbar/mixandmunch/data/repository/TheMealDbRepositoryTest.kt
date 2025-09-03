package com.jmbar.mixandmunch.data.repository

import com.jmbar.mixandmunch.data.remote.themealdb.TheMealDbService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class TheMealDbRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: TheMealDbService
    private lateinit var repository: TheMealDbRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        
        val json = Json { ignoreUnknownKeys = true }
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TheMealDbService::class.java)
            
        repository = TheMealDbRepository(service)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `single ingredient flow returns meals with matchedCount 1`() = runTest {
        // Given
        val responseJson = """
        {
            "meals": [
                {
                    "idMeal": "52874",
                    "strMeal": "Beef and Mustard Pie",
                    "strMealThumb": "https://www.themealdb.com/images/media/meals/sytuqu1511553755.jpg"
                },
                {
                    "idMeal": "52878",
                    "strMeal": "Beef and Oyster pie",
                    "strMealThumb": "https://www.themealdb.com/images/media/meals/wrssvt1511556563.jpg"
                }
            ]
        }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = repository.searchByIngredients(listOf("tomato"))

        // Then
        assertEquals(2, result.size)
        assertEquals("52874", result[0].id)
        assertEquals("Beef and Mustard Pie", result[0].name)
        assertEquals(1, result[0].matchedCount)
        assertEquals("52878", result[1].id)
        assertEquals("Beef and Oyster pie", result[1].name)
        assertEquals(1, result[1].matchedCount)
    }

    @Test
    fun `multiple ingredients union and rank by matchedCount`() = runTest {
        // Given - ingredient A returns meals {1,2,3}, ingredient B returns {2,3,4}
        val responseA = """
        {
            "meals": [
                {"idMeal": "1", "strMeal": "Recipe 1", "strMealThumb": "thumb1.jpg"},
                {"idMeal": "2", "strMeal": "Recipe 2", "strMealThumb": "thumb2.jpg"},
                {"idMeal": "3", "strMeal": "Recipe 3", "strMealThumb": "thumb3.jpg"}
            ]
        }
        """.trimIndent()

        val responseB = """
        {
            "meals": [
                {"idMeal": "2", "strMeal": "Recipe 2", "strMealThumb": "thumb2.jpg"},
                {"idMeal": "3", "strMeal": "Recipe 3", "strMealThumb": "thumb3.jpg"},
                {"idMeal": "4", "strMeal": "Recipe 4", "strMealThumb": "thumb4.jpg"}
            ]
        }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseA))
        mockWebServer.enqueue(MockResponse().setBody(responseB))

        // When
        val result = repository.searchByIngredients(listOf("tomato", "onion"))

        // Then
        assertEquals(4, result.size)
        
        // Check that meals 2,3 have matchedCount=2 and rank above meals 1,4 with matchedCount=1
        val meal2 = result.find { it.id == "2" }
        val meal3 = result.find { it.id == "3" }
        val meal1 = result.find { it.id == "1" }
        val meal4 = result.find { it.id == "4" }

        assertNotNull(meal2)
        assertNotNull(meal3)
        assertNotNull(meal1)
        assertNotNull(meal4)

        assertEquals(2, meal2!!.matchedCount)
        assertEquals(2, meal3!!.matchedCount)
        assertEquals(1, meal1!!.matchedCount)
        assertEquals(1, meal4!!.matchedCount)

        // Verify ranking: matchedCount=2 should come first
        assertTrue("Meal 2 should rank higher than Meal 1", 
            result.indexOf(meal2) < result.indexOf(meal1))
        assertTrue("Meal 3 should rank higher than Meal 4", 
            result.indexOf(meal3) < result.indexOf(meal4))
    }

    @Test
    fun `normalizer integration - brands dropped and typos fixed`() = runTest {
        // Given - only valid ingredients get queried
        val responseA = """
        {
            "meals": [
                {"idMeal": "1", "strMeal": "Tomato Soup", "strMealThumb": "thumb1.jpg"}
            ]
        }
        """.trimIndent()

        val responseB = """
        {
            "meals": [
                {"idMeal": "2", "strMeal": "Garlic Bread", "strMealThumb": "thumb2.jpg"}
            ]
        }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseA)) // for "tomato"
        mockWebServer.enqueue(MockResponse().setBody(responseB)) // for "garlic"

        // When - input has brand "Knorr", typo "t0mat0", and Filipino "bawang"
        val result = repository.searchByIngredients(listOf("Knorr", "t0mat0", "bawang"))

        // Then - should only query for "tomato" and "garlic" (Knorr is dropped)
        assertEquals(2, result.size)
        
        val tomato = result.find { it.name.contains("Tomato") }
        val garlic = result.find { it.name.contains("Garlic") }
        
        assertNotNull("Should have tomato result", tomato)
        assertNotNull("Should have garlic result", garlic)
        assertEquals(1, tomato!!.matchedCount)
        assertEquals(1, garlic!!.matchedCount)

        // Verify that only 2 API calls were made (not 3)
        assertEquals(2, mockWebServer.requestCount)
    }

    @Test
    fun `null response handling - one ingredient null, another valid`() = runTest {
        // Given
        val nullResponse = """{"meals": null}"""
        val validResponse = """
        {
            "meals": [
                {"idMeal": "1", "strMeal": "Valid Recipe", "strMealThumb": "thumb1.jpg"}
            ]
        }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(nullResponse))   // first ingredient
        mockWebServer.enqueue(MockResponse().setBody(validResponse)) // second ingredient

        // When
        val result = repository.searchByIngredients(listOf("unknown", "tomato"))

        // Then - should return the valid recipe despite one null response
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Valid Recipe", result[0].name)
        assertEquals(1, result[0].matchedCount)
    }

    @Test
    fun `cap behavior - normalize cap respected and repository maxResults enforced`() = runTest {
        // Given - more than 6 normalized ingredients should be capped to 6
        val sevenIngredients = listOf("onion", "tomato", "garlic", "potato", "carrot", "celery", "pepper")
        
        // Return empty for all to test cap behavior
        val emptyResponse = """{"meals": []}"""
        repeat(6) { // Should only make 6 calls due to normalize cap
            mockWebServer.enqueue(MockResponse().setBody(emptyResponse))
        }

        // When
        val result = repository.searchByIngredients(sevenIngredients)

        // Then - should only make 6 API calls (normalize cap)
        assertEquals(6, mockWebServer.requestCount)
        assertEquals(0, result.size) // empty results for this test
    }

    @Test
    fun `empty input returns empty result`() = runTest {
        // When
        val result = repository.searchByIngredients(emptyList())

        // Then
        assertEquals(0, result.size)
        assertEquals(0, mockWebServer.requestCount) // No API calls made
    }

    @Test
    fun `network error handling - continues with other ingredients`() = runTest {
        // Given - first call fails, second succeeds
        mockWebServer.enqueue(MockResponse().setResponseCode(500)) // network error
        mockWebServer.enqueue(MockResponse().setBody("""
        {
            "meals": [
                {"idMeal": "1", "strMeal": "Valid Recipe", "strMealThumb": "thumb1.jpg"}
            ]
        }
        """.trimIndent()))

        // When
        val result = repository.searchByIngredients(listOf("bad_ingredient", "tomato"))

        // Then - should return results from successful call
        assertEquals(1, result.size)
        assertEquals("Valid Recipe", result[0].name)
    }

    @Test
    fun `maxResults parameter enforced`() = runTest {
        // Given - response with multiple meals
        val responseJson = """
        {
            "meals": [
                {"idMeal": "1", "strMeal": "Recipe 1", "strMealThumb": "thumb1.jpg"},
                {"idMeal": "2", "strMeal": "Recipe 2", "strMealThumb": "thumb2.jpg"},
                {"idMeal": "3", "strMeal": "Recipe 3", "strMealThumb": "thumb3.jpg"}
            ]
        }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When - limit to 2 results
        val result = repository.searchByIngredients(listOf("tomato"), maxResults = 2)

        // Then
        assertEquals(2, result.size)
    }
}