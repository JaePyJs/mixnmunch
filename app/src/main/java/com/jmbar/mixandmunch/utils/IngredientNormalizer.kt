package com.jmbar.mixandmunch.utils

/**
 * Pure, dependency-free ingredient normalizer that handles Filipino→English mapping, 
 * typo correction, singularization, and cleaning as specified in the project brief.
 */
object IngredientNormalizer {
    
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
    
    // Brand names and vague terms to drop (order-insensitive, lowercase)
    private val brandNamesAndVagueTerms = setOf(
        "knorr", "maggi", "magic sarap", "nor", "del monte", "mama sita", 
        "ajinomoto", "mccormick", "seasoning", "mix", "flavor", "flavour", 
        "taste enhancer"
    )
    
    // Basic singularization rules for irregular forms
    private val irregularSingularization = mapOf(
        "tomatoes" to "tomato",
        "potatoes" to "potato"
    )
    
    /**
     * Normalizes a list of ingredient strings according to the specified pipeline:
     * 1. Lowercase + trim
     * 2. Remove punctuation/symbols by replacing with spaces; collapse multiple spaces; trim again
     * 3. Convert digit-to-letter typos: 0→o, 1→l, 3→e, 4→a, 5→s, 7→t (within words)
     * 4. Filipino→English mapping (exact)
     * 5. Basic singularization
     * 6. Drop brand/vague terms
     * 7. Deduplicate (preserve insertion order)
     * 8. Cap to max items (default 6)
     * 
     * @param inputs Raw ingredient strings
     * @param max Maximum number of normalized ingredients to return (default 6)
     * @return Normalized and filtered ingredient list
     */
    fun normalize(inputs: List<String>, max: Int = 6): List<String> {
        return inputs
            .asSequence()
            // Step 1: lowercase + trim
            .map { it.lowercase().trim() }
            .filter { it.isNotBlank() }
            // Step 2: remove punctuation/symbols, collapse spaces, trim
            .map { removePunctuationAndCollapseSpaces(it) }
            .filter { it.isNotBlank() }
            // Step 3: convert digit-to-letter typos
            .map { fixDigitToLetterTypos(it) }
            // Step 4: Filipino→English mapping
            .map { filipinoToEnglish(it) }
            // Step 5: basic singularization
            .map { singularize(it) }
            // Step 6: drop brand/vague terms
            .filterNot { it in brandNamesAndVagueTerms }
            // Step 7: deduplicate (preserve insertion order)
            .distinct()
            // Step 8: cap to max items
            .take(max)
            .toList()
    }
    
    /**
     * Convenience overload that delegates to normalize(inputs, 6)
     */
    fun normalize(inputs: List<String>): List<String> = normalize(inputs, 6)
    
    private fun removePunctuationAndCollapseSpaces(text: String): String {
        return text
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ") // Replace punctuation with spaces
            .replace(Regex("\\s+"), " ") // Collapse multiple spaces
            .trim()
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
        // Handle irregular plurals first
        irregularSingularization[text]?.let { return it }
        
        // Generic rule: if single word and length > 3, strip trailing 's'
        // Leave multi-word phrases as-is
        if (!text.contains(" ") && text.length > 3 && text.endsWith("s")) {
            return text.dropLast(1)
        }
        
        return text
    }
}