package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la validation du traitement des en-têtes de vin corrompus ("VIN CONSEILLĖ")
 * et à la prévention de leur fuite dans le champ source.
 */
class OcrWineCorruptedHeaderTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testParse_CorruptedWineHeader_CapturesWineAndDoesNotLeakToSource() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            Salade océane
            INGRÉDIENTS
            250 g de crevettes
            sel et poivre
            • Mélangez le yaourt et la mayonnaise.
            VIN CONSEILLĖ
            Touraine,
            Sauvignon blanc, 1995, 75 cl
            TOCRAINE
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .ignoringCase()
            .contains("Touraine")

        assertWithMessage("L'en-tête de vin corrompu 'VIN CONSEILLĖ' ne doit pas fuiter dans le champ source")
            .that(result.source.orEmpty())
            .doesNotContain("VIN CONSEILLĖ")

        assertWithMessage("Le champ source ne doit pas contenir 'VIN CONSEILLÉ'")
            .that(result.source.orEmpty())
            .doesNotContain("VIN CONSEILLÉ")
    }

    @Test
    fun testParse_UnaccentedWineHeader_CapturesWineAndDoesNotLeakToSource() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            Salade océane
            INGRÉDIENTS
            250 g de crevettes
            sel et poivre
            • Mélangez le yaourt et la mayonnaise.
            VIN CONSEILLE
            Touraine,
            Sauvignon blanc, 1995, 75 cl
            TOCRAINE
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .ignoringCase()
            .contains("Touraine")

        assertWithMessage("L'en-tête de vin sans accent 'VIN CONSEILLE' ne doit pas fuiter dans le champ source")
            .that(result.source.orEmpty())
            .doesNotContain("VIN CONSEILLE")

        assertWithMessage("Le champ source ne doit pas contenir 'VIN CONSEILLÉ'")
            .that(result.source.orEmpty())
            .doesNotContain("VIN CONSEILLÉ")
    }

    @Test
    fun testParse_DiacriticAccentedWineHeaders_CapturesWineAndDoesNotLeakToSource() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val headersToTest = listOf("VIN CONSEILLÈ", "VIN CONSEILLË", "VIN CONSEILLÊ")

        headersToTest.forEach { header ->
            val rawOcrText = """
                Salade océane
                INGRÉDIENTS
                250 g de crevettes
                sel et poivre
                • Mélangez le yaourt et la mayonnaise.
                $header
                Touraine,
                Sauvignon blanc, 1995, 75 cl
                TOCRAINE
            """.trimIndent()

            val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

            assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
                .that(result.wine)
                .ignoringCase()
                .contains("Touraine")

            assertWithMessage("L'en-tête de vin '$header' ne doit pas fuiter dans le champ source")
                .that(result.source.orEmpty())
                .doesNotContain(header)
        }
    }
}
