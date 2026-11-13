package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Étape 2.5) vérifiant que le terme de coût/budget "raisonnable" ou "Coût : raisonnable"
 * est extrait à 100% dans priceRating avec le code ID '2' (PRICE_MEDIUM) et purgé du texte.
 */
class Ocr3PriceReasonableMetadataTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessPriceBlock_ReasonableKeyword_ExtractsPriceMediumCodeAndPurgesText() {
        val rawSections = RawSections()
        val remaining = RecipeMetadataParser2_BlockB_Price.processPriceBlock("Coût : raisonnable", vocabulary, rawSections)

        assertWithMessage("Le terme 'raisonnable' doit alimenter detectedPriceRating avec le code '2'")
            .that(rawSections.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_MEDIUM)

        assertWithMessage("La ligne 'Coût : raisonnable' doit être totalement purgée de son mot-clé")
            .that(remaining)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_FullRecipeWithReasonablePrice_PopulatesPriceRatingFieldWithMediumCode() {
        val rawOcrText = """
            Soupe à l'oignon
            Coût : raisonnable
            INGRÉDIENTS
            4 oignons
            50 g de beurre
            INSTRUCTIONS
            • Faites revenir les oignons.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("Le champ priceRating doit valoir le code '2'")
            .that(parsed.priceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_MEDIUM)

        assertWithMessage("Le mot-clé 'raisonnable' ne doit pas fuiter dans les instructions")
            .that(parsed.instructions.orEmpty())
            .doesNotContain("raisonnable")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessPriceBlock_LowKeywords_ExtractsPriceLowCodeAndPurgesText() {
        val rawSections = RawSections()
        val remaining = RecipeMetadataParser2_BlockB_Price.processPriceBlock("Budget : bon marché", vocabulary, rawSections)

        assertWithMessage("Le terme 'bon marché' doit alimenter detectedPriceRating avec le code '1'")
            .that(rawSections.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_LOW)

        assertWithMessage("La ligne 'Budget : bon marché' doit être totalement purgée de son mot-clé")
            .that(remaining)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessPriceBlock_HighKeywords_ExtractsPriceHighCodeAndPurgesText() {
        val rawSections = RawSections()
        val remaining = RecipeMetadataParser2_BlockB_Price.processPriceBlock("Prix : gastronomique", vocabulary, rawSections)

        assertWithMessage("Le terme 'gastronomique' doit alimenter detectedPriceRating avec le code '3'")
            .that(rawSections.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_HIGH)

        assertWithMessage("La ligne 'Prix : gastronomique' doit être totalement purgée de son mot-clé")
            .that(remaining)
            .isEmpty()
    }
}
