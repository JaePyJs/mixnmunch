package com.jmbar.mixandmunch.data.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Recipe Generator interface - designed for local Ollama integration
 * This is the foundation for Phase 2 AI implementation
 */
interface AIRecipeGenerator {
    suspend fun generateRecipe(ingredients: List<String>, preferences: AIPreferences = AIPreferences()): Result<AIRecipe>
    suspend fun isAvailable(): Boolean
}

@Serializable
data class AIPreferences(
    val cuisine: String = "Filipino",
    val difficulty: String = "medium",
    val cookingTime: String = "30-60 minutes",
    val servings: Int = 4,
    val dietaryRestrictions: List<String> = emptyList(),
    val safetyMode: Boolean = true
)

@Serializable
data class AIRecipe(
    val title: String,
    val ingredients: List<AIIngredient>,
    val instructions: List<String>,
    val cookingTime: String,
    val servings: Int,
    val difficulty: String,
    val safetyNotes: List<String>,
    val nutritionTips: List<String> = emptyList(),
    val culturalContext: String? = null
)

@Serializable
data class AIIngredient(
    val name: String,
    val amount: String,
    val substitutes: List<String> = emptyList()
)

/**
 * Local Ollama AI implementation (for Phase 2)
 * Connects to local Ollama instance running on PC
 */
@Singleton
class OllamaAIRecipeGenerator @Inject constructor(
    private val httpClient: OkHttpClient,
    private val json: Json
) : AIRecipeGenerator {
    
    private val baseUrl = "http://192.168.1.100:11434" // Default Ollama URL - configurable
    private val model = "llama3.1:8b-instruct-q4_0" // Recommended model from project brief
    
    override suspend fun generateRecipe(
        ingredients: List<String>,
        preferences: AIPreferences
    ): Result<AIRecipe> {
        return try {
            if (!isAvailable()) {
                return Result.failure(Exception("AI service unavailable - using sourced recipes only"))
            }
            
            val prompt = buildPrompt(ingredients, preferences)
            val response = callOllama(prompt)
            
            parseAIResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isAvailable(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/version")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    private fun buildPrompt(ingredients: List<String>, preferences: AIPreferences): String {
        return buildString {
            appendLine("You are a Filipino cuisine expert. Create a recipe using these ingredients:")\n            appendLine("Ingredients: ${ingredients.joinToString(\", \")}")\n            appendLine("Cuisine: ${preferences.cuisine}")\n            appendLine("Difficulty: ${preferences.difficulty}")\n            appendLine("Cooking time: ${preferences.cookingTime}")\n            appendLine("Servings: ${preferences.servings}")\n            \n            if (preferences.dietaryRestrictions.isNotEmpty()) {\n                appendLine("Dietary restrictions: ${preferences.dietaryRestrictions.joinToString(\", \")}")\n            }\n            \n            appendLine()\n            appendLine("Requirements:")\n            appendLine("1. Create an authentic Filipino recipe")\n            appendLine("2. Use proper Filipino cooking techniques")\n            appendLine("3. Include safety notes for any potentially risky ingredients or cooking methods")\n            appendLine("4. Provide substitutions for hard-to-find ingredients")\n            appendLine("5. Include cultural context or history if relevant")\n            appendLine("6. Ensure proper food safety (cooking temperatures, handling)")\n            \n            if (preferences.safetyMode) {\n                appendLine("7. SAFETY CRITICAL: Include proper cooking temperatures for meat/poultry")\n                appendLine("8. SAFETY CRITICAL: Warn about any food safety concerns")\n            }\n            \n            appendLine()\n            appendLine("Respond in JSON format with this structure:")\n            appendLine("{")\n            appendLine("  \"title\": \"Recipe Name\",")\n            appendLine("  \"ingredients\": [{")\n            appendLine("    \"name\": \"ingredient\",")\n            appendLine("    \"amount\": \"quantity\",")\n            appendLine("    \"substitutes\": [\"alternative1\", \"alternative2\"]")\n            appendLine("  }],")\n            appendLine("  \"instructions\": [\"step1\", \"step2\"],")\n            appendLine("  \"cookingTime\": \"X minutes\",")\n            appendLine("  \"servings\": X,")\n            appendLine("  \"difficulty\": \"easy/medium/hard\",")\n            appendLine("  \"safetyNotes\": [\"safety note1\"],")\n            appendLine("  \"nutritionTips\": [\"tip1\"],")\n            appendLine("  \"culturalContext\": \"background info\"")\n            appendLine("}")\n        }\n    }\n    \n    private suspend fun callOllama(prompt: String): String {\n        val requestBody = json.encodeToString(\n            OllamaRequest.serializer(),\n            OllamaRequest(\n                model = model,\n                prompt = prompt,\n                stream = false,\n                options = OllamaOptions(\n                    temperature = 0.7,\n                    top_p = 0.9,\n                    max_tokens = 2048\n                )\n            )\n        )\n        \n        val request = Request.Builder()\n            .url("$baseUrl/api/generate")\n            .post(requestBody.toRequestBody("application/json".toMediaType()))\n            .build()\n        \n        val response = httpClient.newCall(request).execute()\n        \n        if (!response.isSuccessful) {\n            throw IOException("AI request failed: ${response.code}")\n        }\n        \n        val responseBody = response.body?.string() ?: throw IOException("Empty response")\n        val ollamaResponse = json.decodeFromString(OllamaResponse.serializer(), responseBody)\n        \n        return ollamaResponse.response\n    }\n    \n    private fun parseAIResponse(response: String): Result<AIRecipe> {\n        return try {\n            // Extract JSON from response (AI might include extra text)\n            val jsonStart = response.indexOf("{")\n            val jsonEnd = response.lastIndexOf("}") + 1\n            \n            if (jsonStart == -1 || jsonEnd <= jsonStart) {\n                throw Exception("No valid JSON found in AI response")\n            }\n            \n            val jsonString = response.substring(jsonStart, jsonEnd)\n            val aiRecipe = json.decodeFromString(AIRecipe.serializer(), jsonString)\n            \n            // Validate recipe has required safety information\n            val validatedRecipe = validateSafetyNotes(aiRecipe)\n            \n            Result.success(validatedRecipe)\n        } catch (e: Exception) {\n            Result.failure(Exception("Failed to parse AI response: ${e.message}"))\n        }\n    }\n    \n    private fun validateSafetyNotes(recipe: AIRecipe): AIRecipe {\n        val enhancedSafetyNotes = recipe.safetyNotes.toMutableList()\n        \n        // Add default safety notes if missing\n        val recipeText = "${recipe.title} ${recipe.ingredients.joinToString(" ") { it.name }} ${recipe.instructions.joinToString(" ")}".lowercase()\n        \n        if (recipeText.contains("chicken") || recipeText.contains("manok")) {\n            if (!enhancedSafetyNotes.any { it.contains("165°F") || it.contains("74°C") }) {\n                enhancedSafetyNotes.add("Cook chicken to internal temperature of 165°F (74°C)")\n            }\n        }\n        \n        if (recipeText.contains("pork") || recipeText.contains("baboy")) {\n            if (!enhancedSafetyNotes.any { it.contains("145°F") || it.contains("63°C") }) {\n                enhancedSafetyNotes.add("Cook pork to internal temperature of 145°F (63°C)")\n            }\n        }\n        \n        if (recipeText.contains("ground") || recipeText.contains("giniling")) {\n            if (!enhancedSafetyNotes.any { it.contains("160°F") || it.contains("71°C") }) {\n                enhancedSafetyNotes.add("Cook ground meat to internal temperature of 160°F (71°C)")\n            }\n        }\n        \n        return recipe.copy(safetyNotes = enhancedSafetyNotes)\n    }\n}\n\n@Serializable\nprivate data class OllamaRequest(\n    val model: String,\n    val prompt: String,\n    val stream: Boolean = false,\n    val options: OllamaOptions\n)\n\n@Serializable\nprivate data class OllamaOptions(\n    val temperature: Double,\n    val top_p: Double,\n    val max_tokens: Int\n)\n\n@Serializable\nprivate data class OllamaResponse(\n    val response: String,\n    val done: Boolean\n)\n\n/**\n * Fallback AI implementation for when local AI is unavailable\n * Returns pre-configured Filipino recipes based on ingredients\n */\n@Singleton\nclass FallbackAIRecipeGenerator @Inject constructor() : AIRecipeGenerator {\n    \n    override suspend fun generateRecipe(\n        ingredients: List<String>,\n        preferences: AIPreferences\n    ): Result<AIRecipe> {\n        // Return a curated Filipino recipe based on ingredients\n        val recipe = getFallbackRecipe(ingredients)\n        return Result.success(recipe)\n    }\n    \n    override suspend fun isAvailable(): Boolean = true\n    \n    private fun getFallbackRecipe(ingredients: List<String>): AIRecipe {\n        // Simple ingredient matching for common Filipino dishes\n        return when {\n            ingredients.any { it.contains("chicken") || it.contains("manok") } -> {\n                createAdoboRecipe()\n            }\n            ingredients.any { it.contains("pork") || it.contains("baboy") } -> {\n                createPorkAdoboRecipe()\n            }\n            ingredients.any { it.contains("vegetables") || ingredients.size >= 3 } -> {\n                createPinakbetRecipe()\n            }\n            else -> createSimpleStirFryRecipe(ingredients)\n        }\n    }\n    \n    private fun createAdoboRecipe(): AIRecipe {\n        return AIRecipe(\n            title = "Chicken Adobo (Filipino Style)\",\n            ingredients = listOf(\n                AIIngredient("Chicken pieces", "1 kg", listOf("pork belly", "tofu")),\n                AIIngredient("Soy sauce", \"1/2 cup\", listOf(\"tamari\", \"coconut aminos\")),\n                AIIngredient("White vinegar", \"1/4 cup\", listOf(\"apple cider vinegar\")),\n                AIIngredient(\"Garlic\", \"6 cloves\", emptyList()),\n                AIIngredient(\"Bay leaves\", \"3 pieces\", emptyList()),\n                AIIngredient(\"Black peppercorns\", \"1 tsp\", listOf(\"ground black pepper\"))\n            ),\n            instructions = listOf(\n                \"Combine chicken, soy sauce, vinegar, garlic, bay leaves, and peppercorns in a pot.\",\n                \"Marinate for at least 30 minutes.\",\n                \"Bring to a boil, then simmer covered for 20 minutes.\",\n                \"Remove cover and continue cooking until sauce reduces.\",\n                \"Cook chicken until internal temperature reaches 165°F (74°C).\",\n                \"Serve hot with steamed rice.\"\n            ),\n            cookingTime = \"45 minutes\",\n            servings = 4,\n            difficulty = \"easy\",\n            safetyNotes = listOf(\n                \"Cook chicken to internal temperature of 165°F (74°C)\",\n                \"Do not leave marinated chicken at room temperature for over 2 hours\"\n            ),\n            nutritionTips = listOf(\n                \"Rich in protein\",\n                \"Contains beneficial probiotics from vinegar fermentation\"\n            ),\n            culturalContext = \"Adobo is considered the national dish of the Philippines, with Spanish and indigenous influences.\"\n        )\n    }\n    \n    // Additional fallback recipes would be implemented here...\n    private fun createPorkAdoboRecipe(): AIRecipe = createAdoboRecipe().copy(\n        title = \"Pork Adobo (Filipino Style)\",\n        safetyNotes = listOf(\n            \"Cook pork to internal temperature of 145°F (63°C)\",\n            \"Let pork rest for 3 minutes after cooking\"\n        )\n    )\n    \n    private fun createPinakbetRecipe(): AIRecipe {\n        // Implementation for Pinakbet recipe\n        return createAdoboRecipe().copy(title = \"Simple Pinakbet\")\n    }\n    \n    private fun createSimpleStirFryRecipe(ingredients: List<String>): AIRecipe {\n        return AIRecipe(\n            title = \"Filipino-Style Stir Fry\",\n            ingredients = ingredients.map { \n                AIIngredient(it, \"as needed\", emptyList()) \n            },\n            instructions = listOf(\n                \"Heat oil in a large pan or wok.\",\n                \"Add garlic and onions, sauté until fragrant.\",\n                \"Add main ingredients and cook thoroughly.\",\n                \"Season with soy sauce and pepper.\",\n                \"Ensure all ingredients are properly cooked.\"\n            ),\n            cookingTime = \"20 minutes\",\n            servings = 2,\n            difficulty = \"easy\",\n            safetyNotes = listOf(\n                \"Ensure all meat is cooked to safe temperatures\",\n                \"Wash vegetables thoroughly before cooking\"\n            ),\n            culturalContext = \"A simple Filipino home-style dish using available ingredients.\"\n        )\n    }\n}", "original_text": ""}]