package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant les fonctionnalités de présentation finale de [Ocr5Beautifier].
 */
class Ocr5BeautifierTest {

    /**
     * Vérifie la suppression des numéros d'étapes et puces de numérotation.
     */
    @Test
    fun beautifyInstructions_removesStepNumbersAndPunctuation() {
        val input = listOf("1. Couper les oignons", "2) Ajouter le sel", "3- Mélanger", "10 minutes au four")
        val expected = listOf("Couper les oignons.", "Ajouter le sel.", "Mélanger.", "10 minutes au four.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Les numéros d'étapes ('1.', '2)', '3-') doivent être supprimés et les consignes majusculées et ponctuées")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    /**
     * Vérifie la suppression des chiffres isolés en début de consigne devant les majuscules.
     */
    @Test
    fun beautifyInstructions_removesIsolatedDigitsBeforeUppercaseWords() {
        val input = listOf("1 Plongez les légumes")
        val expected = listOf("Plongez les légumes.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Le chiffre isolé '1' devant le mot majusculé 'Plongez' doit être retiré")
            .that(result)
            .containsExactlyElementsIn(expected)
    }

    /**
     * Vérifie le retrait des puces typographiques en début de ligne (`•`, `-`, `*`).
     */
    @Test
    fun beautifyInstructions_removesBullets() {
        val input = listOf("• Faire cuire", "- Saler", "* Poivrer")
        val expected = listOf("Faire cuire.", "Saler.", "Poivrer.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Les puces '•', '-', '*' doivent être retirées en début de ligne")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    /**
     * Vérifie la majuscule initiale forcée sur chaque nom d'ingrédient.
     */
    @Test
    fun beautifyIngredients_capitalizesFirstLetter() {
        val input = listOf("sel", "poivre", "2 carottes")
        val expected = listOf("Sel", "Poivre", "2 carottes")

        val result = Ocr5Beautifier.beautifyIngredients(input)
        assertWithMessage("Chaque ingrédient en minuscules doit recevoir une majuscule initiale")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    @Test
    fun testSanitizeLine_ExhaustiveBranchCoverage() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        assertWithMessage("Ocr5Beautifier doit générer une représentation textuelle valide")
            .that(Ocr5Beautifier.toString())
            .contains("Ocr5Beautifier")

        // 1. Non-empty spellingRepairs (Lines 108-110)
        val sanitizedSpelling = Ocr5Beautifier.sanitizeLine(
            line = "hoile",
            spellingRepairs = listOf("hoile | huile"),
            vocab = vocabulary
        )
        assertWithMessage("spellingRepairs doit corriger 'hoile' en 'huile'").that(sanitizedSpelling).isEqualTo("huile")

        // 2. ocrNoiseExclusions match & empty string filtering (Line 127)
        val customNoiseVocab = vocabulary.copy(
            ocrNoiseExclusions = listOf("   ", "noise_marker"),
            excludedOcrKeywords = listOf("   ", "publicité")
        )
        val sanitizedNoise = Ocr5Beautifier.sanitizeLine(
            line = "noise_marker 200 g de farine",
            headerTrashWords = setOf("ingrédients"),
            vocab = customNoiseVocab
        )
        assertWithMessage("ocrNoiseExclusions doit rejeter la ligne comme bruit").that(sanitizedNoise).isNull()

        // 3. isRecipeHeaderTrash and headerTrashWords branches (Line 132)
        // a) hasActionVerb = true -> isRecipeHeaderTrash is false
        val sanitizedAction = Ocr5Beautifier.sanitizeLine(
            line = "Ingrédients : Mélanger la farine",
            headerTrashWords = setOf("ingrédients"),
            hasActionVerb = true,
            vocab = customNoiseVocab
        )
        assertWithMessage("Ligne avec verbe d'action ne doit pas être rejetée comme en-tête").that(sanitizedAction).isNotNull()

        // b) hasActionVerb = false and empty headerTrashWords
        val sanitizedNoHeaders = Ocr5Beautifier.sanitizeLine(
            line = "200 g de farine",
            headerTrashWords = emptySet(),
            hasActionVerb = false,
            vocab = customNoiseVocab
        )
        assertWithMessage("Sans en-têtes rejetés, la ligne est conservée").that(sanitizedNoHeaders).isEqualTo("200 g de farine")

        // c) hasActionVerb = false with matching excludedOcrKeywords
        val sanitizedExcluded = Ocr5Beautifier.sanitizeLine(
            line = "publicité",
            headerTrashWords = setOf("ingrédients"),
            hasActionVerb = false,
            vocab = customNoiseVocab
        )
        assertWithMessage("Mot exclu de l'OCR doit rejeter la ligne").that(sanitizedExcluded).isNull()
    }
}
