package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("La liste des réparations orthographiques XML ne doit pas être vide")
            .that(vocabulary.ocrSpellingRepairs)
            .isNotEmpty()

        assertWithMessage("La liste des réparations orthographiques doit contenir au moins 70 règles de production")
            .that(vocabulary.ocrSpellingRepairs.size)
            .isAtLeast(70)
    }

    @Test
    fun `pipeline should apply all production ocr_repairs XML on ingredient and instruction lines`() {
        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val expected = parts[1].trim().replace("\\'", "'")

                val normalized = Ocr1Corrector.normalize("200 g de $wrong", vocabulary.ocrSpellingRepairs)
                assertWithMessage("La réparation orthographique pour '$wrong' doit produire '$expected'")
                    .that(normalized)
                    .ignoringCase()
                    .contains(expected.lowercase())
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
        assertWithMessage("L'ingrédient 'goit' doit être réparé en 'goût'")
            .that(result.ingredients)
            .contains("poisson au goût de crabe")

        assertWithMessage("La quantité '4 1 oasts' doit être réparée en '4 toasts'")
            .that(result.ingredients)
            .contains("4 toasts")

        assertWithMessage("La liaison 'lail' doit être réparée en 'l'ail'")
            .that(result.ingredients)
            .contains("l'ail")

        assertWithMessage("Le mot 'pin cée' doit être réparé en 'pincée'")
            .that(result.ingredients)
            .contains("pincée")

        // Vérification sur les instructions finales
        assertWithMessage("La consigne '1 oasts' doit être réparée en 'toasts'")
            .that(result.instructions)
            .contains("Coupez les toasts en deux.")

        assertWithMessage("La consigne 'lais sez' doit être fusionnée en 'laissez'")
            .that(result.instructions)
            .ignoringCase()
            .contains("laissez")
    }
}
