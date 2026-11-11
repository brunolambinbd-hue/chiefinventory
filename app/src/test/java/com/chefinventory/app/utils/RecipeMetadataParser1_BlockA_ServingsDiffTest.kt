package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [RecipeMetadataParser1_BlockA_ServingsDiff].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeMetadataParser1_BlockA_ServingsDiffTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_KcalDiffAndServings() {
        val results = RawSections()
        val rawLine = "250 kcal Facile Pour 4 personnes"
        val remaining = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(rawLine, rawLine, results)

        assertWithMessage("Les kilocalories '250' doivent être extraites dans detectedKcal")
            .that(results.detectedKcal)
            .isEqualTo("250")

        assertWithMessage("La difficulté 'facile' doit être extraite dans detectedDifficulty")
            .that(results.detectedDifficulty)
            .isEqualTo("facile")

        assertWithMessage("Le nombre de portions '4' doit être extrait dans detectedServings")
            .that(results.detectedServings)
            .isEqualTo("4")

        assertWithMessage("La chaîne restante nettoyée doit être vide après extraction")
            .that(remaining)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_BlankWorkingLineWithIngrOrInstrHeaderInLine() {
        val resultsIngr = RawSections()
        val remainingIngr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "Pour 4 personnes",
            line = "Pour 4 personnes - Ingrédients",
            results = resultsIngr
        )
        assertWithMessage("Le nombre de portions '4' doit être extrait")
            .that(resultsIngr.detectedServings)
            .isEqualTo("4")

        assertWithMessage("Le reste de la ligne doit conserver l'en-tête 'ingrédients'")
            .that(remainingIngr)
            .isEqualTo("ingrédients")

        val resultsInstr = RawSections()
        val remainingInstr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "Pour 4 personnes",
            line = "Pour 4 personnes - Préparation",
            results = resultsInstr
        )
        assertWithMessage("Le nombre de portions '4' doit être extrait")
            .that(resultsInstr.detectedServings)
            .isEqualTo("4")

        assertWithMessage("Le reste de la ligne doit conserver l'en-tête 'préparation'")
            .that(remainingInstr)
            .isEqualTo("préparation")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_AlreadySetAndUnaccentedHeaders() {
        val results = RawSections()
        results.detectedKcal = "300"
        results.detectedDifficulty = "moyen"
        results.detectedServings = "2"

        val line = "250 kcal Facile Pour 4 personnes"
        RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(line, line, results)

        assertWithMessage("Des kilocalories déjà renseignées '300' doivent être conservées")
            .that(results.detectedKcal)
            .isEqualTo("300")

        assertWithMessage("Une difficulté déjà renseignée 'moyen' doit être conservée")
            .that(results.detectedDifficulty)
            .isEqualTo("moyen")

        assertWithMessage("Un nombre de portions déjà renseigné '2' doit être conservé")
            .that(results.detectedServings)
            .isEqualTo("2")

        // Unaccented "ingredients" and "instructions"
        val resultsIngr = RawSections()
        val remainingIngr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "pour 4 personnes",
            line = "ingredients pour 4 personnes",
            results = resultsIngr
        )
        assertWithMessage("L'en-tête sans accent 'ingredients' doit être normalisé en 'ingrédients'")
            .that(remainingIngr)
            .isEqualTo("ingrédients")

        val resultsInstr = RawSections()
        val remainingInstr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "pour 4 personnes",
            line = "instructions pour 4 personnes",
            results = resultsInstr
        )
        assertWithMessage("L'en-tête 'instructions' doit être normalisé en 'préparation'")
            .that(remainingInstr)
            .isEqualTo("préparation")
    }
}
