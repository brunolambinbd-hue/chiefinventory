package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la détection et la purge précoce des en-têtes de catégorie
 * de recettes ("ENTRÉES ET AMUSE-BOUCHE", "DESSERTS", "PLATS PRINCIPAUX").
 */
class Ocr2CleanerCategoryHeaderExtractorTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWithVocabulary_DetectsAndPurgesExplicitCategoryHeaders() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawLines = listOf(
            "ENTRÉES ET AMUSE-BOUCHE",
            "1 avocat",
            "200 g de crevettes"
        )

        val result = Ocr2Cleaner.cleanWithVocabulary(rawLines, vocabulary)

        assertWithMessage("La catégorie précoce 'Entrée' doit être détectée depuis l'en-tête 'ENTRÉES ET AMUSE-BOUCHE'")
            .that(result.earlyDetectedCategory)
            .isEqualTo("Entrée")

        assertWithMessage("La ligne d'en-tête de catégorie 'ENTRÉES ET AMUSE-BOUCHE' doit être purgée du texte")
            .that(result.cleanedLines)
            .containsExactly(
                "1 avocat",
                "200 g de crevettes"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_FullRecipeWithExplicitCategoryHeader_PopulatesCategoryField() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            ENTRÉES ET AMUSE-BOUCHE
            Salade de crevettes
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            • Mélangez les crevettes avec la mayonnaise.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("La catégorie de recette extraite doit valoir 'Entrée'")
            .that(parsed.category)
            .isEqualTo("Entrée")

        assertWithMessage("L'en-tête 'ENTRÉES ET AMUSE-BOUCHE' ne doit pas fuiter dans le titre")
            .that(parsed.title)
            .isEqualTo("Salade de crevettes")

        assertWithMessage("L'en-tête 'ENTRÉES ET AMUSE-BOUCHE' ne doit pas fuiter dans les ingrédients")
            .that(parsed.ingredients)
            .doesNotContain("ENTRÉES ET AMUSE-BOUCHE")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWithVocabulary_DetectsAndPurgesCategoryHeaderWithTrailingNoise() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawLines = listOf(
            "ENTRÉES ET AMUSE-BOUCHE O",
            "1 avocat",
            "200 g de crevettes"
        )

        val result = Ocr2Cleaner.cleanWithVocabulary(rawLines, vocabulary)

        assertWithMessage("La catégorie 'Entrée' doit être détectée même avec un bruit 'O' en fin de ligne")
            .that(result.earlyDetectedCategory)
            .isEqualTo("Entrée")

        assertWithMessage("La ligne avec bruit 'ENTRÉES ET AMUSE-BOUCHE O' doit être complètement purgée")
            .that(result.cleanedLines)
            .containsExactly(
                "1 avocat",
                "200 g de crevettes"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_FullRecipeWithCategoryHeaderWithTrailingNoise_PopulatesCategoryAndCleansInstructions() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            Salade de crevettes
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            • Mélangez les crevettes avec la mayonnaise.
            ENTRÉES ET AMUSE-BOUCHE O
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("La catégorie 'Entrée' doit être détectée même depuis une ligne avec bruit de fin de ligne")
            .that(parsed.category)
            .isEqualTo("Entrée")

        assertWithMessage("L'en-tête de catégorie avec bruit 'ENTRÉES ET AMUSE-BOUCHE O' ne doit pas fuiter dans les instructions")
            .that(parsed.instructions.orEmpty())
            .doesNotContain("ENTRÉES ET AMUSE-BOUCHE")
    }
}
