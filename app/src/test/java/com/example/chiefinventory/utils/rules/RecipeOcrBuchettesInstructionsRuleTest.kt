package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrBuchettesInstructionsRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse buchettes de melon recipe should merge instruction lines broken by trailing connector et`() {
        val ocrText = """
            Büchettes de melon au jambon italien et au porto
            INGRÉDIENTS
            1 melon Galia
            sel et poivre
            • Pelez le melon, épépinez-le et
            coupez la chair en fines
            lamelles.
            • Coupez les tranches de jambon.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        // Validation : "Pelez le melon, épépinez-le et" + "coupez la chair en fines lamelles." doivent être recollés proprement !
        assertThat(inst).contains("Pelez le melon, épépinez-le et coupez la chair en fines lamelles.")
        assertThat(inst).doesNotContain("épépinez-le et.")
        assertThat(inst).doesNotContain("et.\n")
    }
}
