package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrSurimiRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse surimi recipe should merge alternative ingredient line ending with ou and keep mayonnaise and salade de ble in ingredients`() {
        val ocrText = """
            175 Salade de surimi
            INGRÉDIENTS
            120 g de surimi (poisson au goût de crabe) ou
            de véritable crabe
            4 toasts
            2 gousses de l'ail
            1 pincée de sel
            2 c. à soupe de mayonnaise
            salade de blé
            • Hachez finement le surimi ou le crabe ainsi que les œufs durs.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // 1. "de véritable crabe" must be merged with "surimi ... ou" in ingredients
        assertThat(ing).ignoringCase().contains("surimi")
        assertThat(ing).ignoringCase().contains("véritable crabe")
        assertThat(ing).ignoringCase().contains("mayonnaise")
        assertThat(ing).ignoringCase().contains("salade de blé")

        // 2. Instructions must NOT contain ingredient names like mayonnaise or salade de blé
        assertThat(inst).ignoringCase().doesNotContain("mayonnaise")
        assertThat(inst).ignoringCase().doesNotContain("salade de blé")
        assertThat(inst).ignoringCase().doesNotContain("véritable crabe")
    }
}
