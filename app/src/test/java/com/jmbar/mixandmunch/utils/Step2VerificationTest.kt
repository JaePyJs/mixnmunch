package com.jmbar.mixandmunch.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Quick verification test for the implementation
 */
class Step2VerificationTest {

    @Test
    fun `test basic normalizer functionality`() {
        // Test the acceptance checks from Step 1
        val result1 = IngredientNormalizer.normalize(listOf("sibuyas", "kamatis", "egg"))
        assertEquals(listOf("onion", "tomato", "egg"), result1)

        val result2 = IngredientNormalizer.normalize(listOf("Knorr", "Magic Sarap", "chickens"))
        assertEquals(listOf("chicken"), result2)

        val result3 = IngredientNormalizer.normalize(listOf("t0mat0", "onions", "bawang"))
        assertEquals(listOf("tomato", "onion", "garlic"), result3)

        val result4 = IngredientNormalizer.normalize(listOf("sitaw", "kangkong", "seasoning", "pepper", "peppers"))
        assertEquals(listOf("string beans", "water spinach", "pepper"), result4)
    }

    @Test
    fun `test max parameter functionality`() {
        val manyInputs = listOf("a", "b", "c", "d", "e", "f", "g", "h")
        val result = IngredientNormalizer.normalize(manyInputs, max = 6)
        assertEquals(6, result.size)
    }
}