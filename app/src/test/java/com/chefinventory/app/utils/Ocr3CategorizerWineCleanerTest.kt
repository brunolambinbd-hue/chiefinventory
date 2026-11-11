package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerWineCleaner].
 */
class Ocr3CategorizerWineCleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr3CategorizerWineCleaner doit générer une représentation textuelle valide")
            .that(Ocr3CategorizerWineCleaner.toString())
            .contains("Ocr3CategorizerWineCleaner")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasWineVolume_AllCases() {
        assertWithMessage("La ligne 'Bouteille de 75 cl' doit être reconnue comme ayant un volume de vin")
            .that(Ocr3CategorizerWineCleaner.hasWineVolume("Bouteille de 75 cl"))
            .isTrue()

        assertWithMessage("Le volume collé '75cl' doit être reconnu")
            .that(Ocr3CategorizerWineCleaner.hasWineVolume("75cl"))
            .isTrue()

        assertWithMessage("Le volume décimal '37.5 cl' doit être reconnu")
            .that(Ocr3CategorizerWineCleaner.hasWineVolume("37.5 cl"))
            .isTrue()

        assertWithMessage("Le volume en litres '1.5 l' doit être reconnu")
            .that(Ocr3CategorizerWineCleaner.hasWineVolume("1.5 l"))
            .isTrue()

        assertWithMessage("Le mot 'Farine' sans volume doit retourner faux")
            .that(Ocr3CategorizerWineCleaner.hasWineVolume("Farine"))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWineLine_HeaderRemovalAndSpellingCorrections() {
        val input = "Notre vin conseillé : Un Bordeau 1996 75 cl"
        val cleaned = Ocr3CategorizerWineCleaner.cleanWineLine(input, vocabulary)
        assertWithMessage("L'en-tête 'Notre vin conseillé :' doit être nettoyé et la faute 'Bordeau' réparée en 'Bordeaux'")
            .that(cleaned)
            .isEqualTo("Un Bordeaux 1996 75 cl")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWineLine_WithEmptyRemovePatternInResources() {
        val emptyRemovePatternRes = Ocr3CategorizerWine.WineResources(
            appellations = vocabulary.wineAppellations,
            producers = vocabulary.wineProducers,
            keywords = vocabulary.wineKeywords,
            titleKeywords = vocabulary.wineTitleKeywords,
            removePattern = ""
        )
        val cleaned = Ocr3CategorizerWineCleaner.cleanWineLine("Un Bordeau 1996", emptyRemovePatternRes)
        assertWithMessage("Le nettoyage de vin avec un motif de suppression vide doit réparer 'Bordeau' sans erreur")
            .that(cleaned)
            .isEqualTo("Un Bordeaux 1996")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplySpellingCorrections_DirectCall() {
        val corrected = Ocr3CategorizerWineCleaner.applySpellingCorrections("Un Bordeau Atinum")
        assertWithMessage("Les corrections orthographiques doivent réparer 'Bordeau' en 'Bordeaux'")
            .that(corrected)
            .contains("Bordeaux")
    }
}
