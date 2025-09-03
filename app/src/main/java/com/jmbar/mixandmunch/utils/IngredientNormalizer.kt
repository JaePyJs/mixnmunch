package com.jmbar.mixandmunch.utils

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ingredient normalizer that handles Filipino→English mapping, typo correction,
 * singularization, and cleaning as specified in the project brief.
 */
@Singleton
class IngredientNormalizer @Inject constructor() {
    
    // Filipino→English base map as specified
    private val filipinoToEnglishMap = mapOf(
        "sibuyas" to "onion",
        "bawang" to "garlic", 
        "kamatis" to "tomato",
        "patatas" to "potato",
        "talong" to "eggplant",
        "manok" to "chicken",
        "baboy" to "pork",
        "baka" to "beef",
        "isda" to "fish",
        "asukal" to "sugar",
        "toyo" to "soy sauce",
        "suka" to "vinegar",
        "sitaw" to "string beans",
        "kangkong" to "water spinach",
        "pechay" to "bok choy",
        "kalabasa" to "squash",
        "ampalaya" to "bitter gourd",
        "labanos" to "radish",
        "sili" to "chili",
        "tokwa" to "tofu",
        "tahong" to "mussels",
        "malunggay" to "moringa"
    )
    
    // Common digit-to-letter typos (0→o, 1→l, 3→e, 4→a, 5→s, 7→t)
    private val digitToLetterMap = mapOf(
        '0' to 'o',
        '1' to 'l', 
        '3' to 'e',
        '4' to 'a',
        '5' to 's',
        '7' to 't'
    )
    
    // Brand names and vague terms to drop
    private val brandNamesAndVagueTerms = setOf(
        "knorr", "magic sarap", "seasoning", "spice", "powder", "mix"
    )
    
    // Basic singularization rules
    private val singularizationRules = mapOf(
        "tomatoes" to "tomato",
        "potatoes" to "potato", 
        "onions" to "onion",
        "eggs" to "egg",
        "peppers" to "pepper",
        "beans" to "bean",
        "leaves" to "leaf",
        "cloves" to "clove"
    )
    
    /**
     * Normalizes a list of ingredient strings according to the specified pipeline:
     * 1. Lowercase/trim
     * 2. Strip punctuation  
     * 3. Fix digit-to-letter typos
     * 4. Filipino→English mapping
     * 5. Basic singularization
     * 6. Dedupe
     * 7. Drop brands and vague terms
     * 8. Cap to max 6 ingredients
     */
    fun normalize(ingredients: List<String>): List<String> {
        return ingredients
            .asSequence()
            .map { it.lowercase().trim() }
            .filter { it.isNotBlank() }
            .map { stripPunctuation(it) }
            .map { fixDigitToLetterTypos(it) }
            .map { filipinoToEnglish(it) }
            .map { singularize(it) }
            .distinct()
            .filterNot { it in brandNamesAndVagueTerms }
            .filter { it.isNotBlank() }
            .take(6) // Cap to max 6 ingredients per query
            .toList()
    }
    
    private fun stripPunctuation(text: String): String {
        return text.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim()
    }
    
    private fun fixDigitToLetterTypos(text: String): String {
        return text.map { char ->
            digitToLetterMap[char] ?: char
        }.joinToString("")
    }
    
    private fun filipinoToEnglish(text: String): String {
        return filipinoToEnglishMap[text] ?: text
    }
    
    private fun singularize(text: String): String {
        return singularizationRules[text] ?: text
    }
    
    /**
     * Returns a readable representation of the normalization process
     * for showing users what the app understood
     */
    fun getNormalizationSteps(original: String): NormalizationStep {
        val trimmed = original.lowercase().trim()
        val noPunctuation = stripPunctuation(trimmed)
        val fixedTypos = fixDigitToLetterTypos(noPunctuation)
        val translated = filipinoToEnglish(fixedTypos)
        val singularized = singularize(translated)
        
        return NormalizationStep(
            original = original,
            final = singularized,
            wasTranslated = translated != fixedTypos,
            wasSingularized = singularized != translated,
            wasFiltered = singularized in brandNamesAndVagueTerms
        )
    }
}

data class NormalizationStep(
    val original: String,
    val final: String,
    val wasTranslated: Boolean,
    val wasSingularized: Boolean,
    val wasFiltered: Boolean
)