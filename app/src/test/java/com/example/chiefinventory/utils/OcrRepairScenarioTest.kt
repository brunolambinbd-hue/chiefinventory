package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrRepairScenarioTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        // Charge le VRAI vocabulaire de production depuis res/values/ocr_repairs.xml
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `pipeline should verify that all ocr_repairs from production XML are loaded`() {
        assertThat(vocabulary.ocrSpellingRepairs).isNotEmpty()
        assertThat(vocabulary.ocrSpellingRepairs.size).isAtLeast(70)
    }

    @Test
    fun `pipeline should apply all production ocr_repairs XML on ingredient and instruction lines`() {
        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val expected = parts[1].trim().replace("\\'", "'")

                val normalized = Ocr1Normalizer.normalize("200 g de $wrong", vocabulary.ocrSpellingRepairs)
                assertThat(normalized).ignoringCase().contains(expected.lowercase())
            }
        }
    }

    @Test
    fun `pipeline should handle complete recipe scan containing multiple OCR typos`() {
        val rawOcrWithTypos = """
            INGRÉDIENTS
            120 g de surimi (poisson au goit de crabe)
            4 1 oasts
            2 gousses de lail
            1 pin cée de sel
            • Coupez les 1 oasts en deux.
            • Metiez de lail et lais sez mijoter avec la potle.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrWithTypos, vocabulary)

        // Vérification sur les ingrédients finaux (Niveau B - Intégration)
        assertThat(result.ingredients).contains("poisson au goût de crabe")
        assertThat(result.ingredients).contains("4 toasts")
        assertThat(result.ingredients).contains("l'ail")
        assertThat(result.ingredients).contains("pincée")

        // Vérification sur les instructions finales
        assertThat(result.instructions).contains("Coupez les toasts en deux.")
        assertThat(result.instructions).ignoringCase().contains("laissez")
    }
}
