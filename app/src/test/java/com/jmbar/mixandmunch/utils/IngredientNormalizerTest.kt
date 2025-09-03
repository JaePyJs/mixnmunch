package com.jmbar.mixandmunch.utils

import org.junit.Test
import org.junit.Assert.*

class IngredientNormalizerTest {

    @Test
    fun `test Filipino to English mapping`() {
        val result = IngredientNormalizer.normalize(listOf("sibuyas", "kamatis", "egg"))
        assertEquals(listOf("onion", "tomato", "egg"), result)
    }

    @Test
    fun `test brand and vague terms removal`() {
        val result = IngredientNormalizer.normalize(listOf("Knorr", "Magic Sarap", "chickens"))
        assertEquals(listOf("chicken"), result)
    }

    @Test
    fun `test digit to letter typo fixes and Filipino mapping`() {
        val result = IngredientNormalizer.normalize(listOf("t0mat0", "onions", "bawang"))
        assertEquals(listOf("tomato", "onion", "garlic"), result)
    }

    @Test
    fun `test complex normalization with sitaw and kangkong`() {
        val result = IngredientNormalizer.normalize(listOf("sitaw", "kangkong", "seasoning", "pepper", "peppers"))
        assertEquals(listOf("string beans", "water spinach", "pepper"), result)
    }

    @Test
    fun `test max limit enforcement`() {
        val inputs = listOf("onion", "tomato", "garlic", "potato", "carrot", "celery", "pepper", "salt")
        val result = IngredientNormalizer.normalize(inputs, max = 6)
        assertEquals(6, result.size)
        assertEquals(listOf("onion", "tomato", "garlic", "potato", "carrot", "celery"), result)
    }

    @Test
    fun `test custom max parameter`() {
        val inputs = listOf("onion", "tomato", "garlic")
        val result = IngredientNormalizer.normalize(inputs, max = 2)
        assertEquals(2, result.size)
        assertEquals(listOf("onion", "tomato"), result)
    }

    @Test
    fun `test punctuation removal and space collapsing`() {
        val result = IngredientNormalizer.normalize(listOf("onion,,,", "tom@to!", "gar  lic"))
        assertEquals(listOf("onion", "tomato", "garlic"), result)
    }

    @Test
    fun `test deduplication preserves insertion order`() {
        val result = IngredientNormalizer.normalize(listOf("onion", "tomato", "onion", "garlic", "tomato"))
        assertEquals(listOf("onion", "tomato", "garlic"), result)
    }

    @Test
    fun `test irregular plural handling`() {
        val result = IngredientNormalizer.normalize(listOf("tomatoes", "potatoes"))
        assertEquals(listOf("tomato", "potato"), result)
    }

    @Test
    fun `test generic singularization for single words`() {
        val result = IngredientNormalizer.normalize(listOf("onions", "peppers", "carrots"))
        assertEquals(listOf("onion", "pepper", "carrot"), result)
    }

    @Test
    fun `test multi-word phrases left as-is`() {
        val result = IngredientNormalizer.normalize(listOf("string beans", "soy sauce"))
        assertEquals(listOf("string beans", "soy sauce"), result)
    }

    @Test
    fun `test short words not singularized`() {
        val result = IngredientNormalizer.normalize(listOf("peas", "egg"))
        assertEquals(listOf("pea", "egg"), result)
    }

    @Test
    fun `test case insensitive brand removal`() {
        val result = IngredientNormalizer.normalize(listOf("KNORR", "magic sarap", "Ajinomoto", "Del Monte"))
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `test all brand and vague terms removed`() {
        val brands = listOf("knorr", "maggi", "magic sarap", "nor", "del monte", "mama sita", 
                           "ajinomoto", "mccormick", "seasoning", "mix", "flavor", "flavour", "taste enhancer")
        val result = IngredientNormalizer.normalize(brands)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `test digit typo correction`() {
        val result = IngredientNormalizer.normalize(listOf("t0m4t0", "0n10n", "g4r1ic"))
        assertEquals(listOf("tomato", "onion", "garlic"), result)
    }

    @Test
    fun `test empty and blank inputs filtered`() {
        val result = IngredientNormalizer.normalize(listOf("", "  ", "onion", "   ", "tomato"))
        assertEquals(listOf("onion", "tomato"), result)
    }

    @Test
    fun `test convenience overload defaults to max 6`() {
        val inputs = (1..10).map { "ingredient$it" }
        val result = IngredientNormalizer.normalize(inputs)
        assertEquals(6, result.size)
    }

    // Acceptance checks from the brief
    @Test
    fun `acceptance check 1 - sibuyas kamatis egg`() {
        val result = IngredientNormalizer.normalize(listOf("sibuyas", "kamatis", "egg"))
        assertEquals(listOf("onion", "tomato", "egg"), result)
    }

    @Test
    fun `acceptance check 2 - Knorr Magic Sarap chickens`() {
        val result = IngredientNormalizer.normalize(listOf("Knorr", "Magic Sarap", "chickens"))
        assertEquals(listOf("chicken"), result)
    }

    @Test
    fun `acceptance check 3 - t0mat0 onions bawang`() {
        val result = IngredientNormalizer.normalize(listOf("t0mat0", "onions", "bawang"))
        assertEquals(listOf("tomato", "onion", "garlic"), result)
    }

    @Test
    fun `acceptance check 4 - sitaw kangkong seasoning pepper peppers`() {
        val result = IngredientNormalizer.normalize(listOf("sitaw", "kangkong", "seasoning", "pepper", "peppers"))
        assertEquals(listOf("string beans", "water spinach", "pepper"), result)
    }

    @Test
    fun `acceptance check 5 - more than 6 inputs returns at most 6`() {
        val inputs = listOf("onion", "tomato", "garlic", "potato", "carrot", "celery", "pepper", "salt", "sugar")
        val result = IngredientNormalizer.normalize(inputs)
        assertTrue("Result should have at most 6 items", result.size <= 6)
        assertEquals(6, result.size)
    }
}