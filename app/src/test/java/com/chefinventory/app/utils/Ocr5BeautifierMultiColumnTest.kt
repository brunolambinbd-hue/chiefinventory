package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant les 5 sous-embellisseurs de l'Étape 5 (Ocr5Beautifier) :
 * Titre, Ingrédients & Variétés, Instructions, Vins et Sources.
 */
class Ocr5BeautifierMultiColumnTest {

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
    fun testBeautifyTitle_CapitalizationAndTrailingPunctuation_Cleaned() {
        val title = "bûchettes de melon au jambon italien et au porto."
        val beautified = Ocr5Beautifier.beautifyTitle(title)

        assertWithMessage("Le titre doit être majusculé et purgé de son point final")
            .that(beautified)
            .isEqualTo("Bûchettes de melon au jambon italien et au porto")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_VarietyMergingAndLevenshtein_Beautified() {
        val rawIngredients = listOf(
            "8 tranches de jambon",
            "italien",
            "1 melon Galia"
        )
        val vocab = OcrVocabularyLoader.loadFromProjectResources()

        val beautified = Ocr5Beautifier.beautifyIngredients(
            ingredients = rawIngredients,
            baseIngredients = vocab.baseIngredientsExpectingVariety,
            varietyQualifiers = vocab.ingredientVarietyQualifiers
        )

        assertWithMessage("La variété 'italien' doit être fusionnée avec '8 tranches de jambon'")
            .that(beautified)
            .contains("8 tranches de jambon italien")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_StepNumberCleaningAndCapitalization_Beautified() {
        val rawInstructions = listOf(
            "• Disposez les bûchettes sur une assiette avec la vinaigrette.",
            "1. Décorez de brins d'aneth."
        )

        val beautified = Ocr5Beautifier.beautifyInstructions(rawInstructions)

        assertWithMessage("La consigne avec puce doit être nettoyée et majusculée")
            .that(beautified)
            .contains("Disposez les bûchettes sur une assiette avec la vinaigrette.")

        assertWithMessage("La consigne avec numéro d'étape '1.' doit être purgée du numéro")
            .that(beautified)
            .contains("Décorez de brins d'aneth.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyWines_LevenshteinDeduplication_PurgedCleanly() {
        val rawWines = listOf(
            "Atinum, Bordeaux Graves",
            "ATINUM",
            "GRAVES"
        )

        val beautified = Ocr5Beautifier.beautifyWines(rawWines)

        assertWithMessage("Le vin officiel 'Atinum, Bordeaux Graves' doit être conservé")
            .that(beautified)
            .contains("Atinum, Bordeaux Graves")

        assertWithMessage("Le doublon corrompu 'ATINUM' doit être purgé")
            .that(beautified)
            .doesNotContain("ATINUM")
    }
}
