package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerTitleExtractor1_BlockA_MixedLines].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerTitleExtractor1_BlockA_MixedLinesTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasEndingConnector_AllVariants() {
        assertWithMessage("Le titre partiel 'Salade et' se terminant par 'et' doit être reconnu comme ayant un connecteur de fin")
            .that(Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.hasEndingConnector("Salade et"))
            .isTrue()

        assertWithMessage("Le titre partiel 'Salade d'' se terminant par 'd'' doit être reconnu comme ayant un connecteur de fin")
            .that(Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.hasEndingConnector("Salade d'"))
            .isTrue()

        assertWithMessage("Le titre partiel 'Salade de' se terminant par 'de' doit être reconnu comme ayant un connecteur de fin")
            .that(Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.hasEndingConnector("Salade de"))
            .isTrue()

        assertWithMessage("Le titre partiel 'Salade aux' se terminant par 'aux' doit être reconnu comme ayant un connecteur de fin")
            .that(Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.hasEndingConnector("Salade aux"))
            .isTrue()

        assertWithMessage("Le titre complet 'Salade verte' sans connecteur de fin doit retourner faux")
            .that(Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.hasEndingConnector("Salade verte"))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSelPoivreLine_WithMixedLineAndConnector() {
        val candidates = mutableListOf<ScoredTitleCandidate>()
        val lines = listOf(
            "1. Ajouter du sel et poivre Salade d'avocat et",
            "aux crevettes"
        )
        val processed = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = lines[0],
            lineIndex = 0,
            lines = lines,
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("salade", "avocat", "crevettes"),
            containsActionRegex = null,
            firstIngredientWord = "avocat",
            candidates = candidates
        )

        assertWithMessage("La ligne mixte avec 'sel et poivre' et titre accolé doit être traitée avec succès")
            .that(processed)
            .isTrue()

        assertWithMessage("Un candidat titre doit être extrait et ajouté à la liste des candidats")
            .that(candidates)
            .isNotEmpty()

        assertWithMessage("Le candidat titre fusionné sur 2 lignes 'Salade d'avocat et aux crevettes' doit être extrait")
            .that(candidates[0].candidateText)
            .contains("Salade d'avocat et aux crevettes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSelPoivreLine_NonSelPoivreShortOrBlankSecondPart() {
        val candidates = mutableListOf<ScoredTitleCandidate>()

        // 1. Line without sel et poivre
        val res1 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = "Salade verte",
            lineIndex = 0,
            lines = listOf("Salade verte"),
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("salade"),
            containsActionRegex = null,
            firstIngredientWord = null,
            candidates = candidates
        )
        assertWithMessage("Une ligne sans 'sel et poivre' ne doit pas être traitée par le bloc mixte")
            .that(res1)
            .isFalse()

        // 2. Line with sel et poivre but short (< 12)
        val res2 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = "sel et poivre",
            lineIndex = 0,
            lines = listOf("sel et poivre"),
            vocabulary = vocabulary,
            recipeIngredientWords = emptySet(),
            containsActionRegex = null,
            firstIngredientWord = null,
            candidates = candidates
        )
        assertWithMessage("La ligne courte 'sel et poivre' (< 12 caractères) ne doit pas être traitée par le bloc mixte")
            .that(res2)
            .isFalse()

        // 3. Line with sel et poivre but blank second part
        val res3 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = "Ajouter le sel et poivre   ",
            lineIndex = 0,
            lines = listOf("Ajouter le sel et poivre   "),
            vocabulary = vocabulary,
            recipeIngredientWords = emptySet(),
            containsActionRegex = null,
            firstIngredientWord = null,
            candidates = candidates
        )
        assertWithMessage("Une ligne mixte avec une seconde partie vide ne doit pas être traitée")
            .that(res3)
            .isFalse()

        // 4. Line with sel et poivre where potential title has no ending connector and no next line
        val res4 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = "1. Ajouter du sel et poivre Salade de tomates",
            lineIndex = 0,
            lines = listOf("1. Ajouter du sel et poivre Salade de tomates"),
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("salade", "tomates"),
            containsActionRegex = null,
            firstIngredientWord = "tomates",
            candidates = candidates
        )
        assertWithMessage("La ligne mixte avec titre complet 'Salade de tomates' doit être traitée avec succès")
            .that(res4)
            .isTrue()

        assertWithMessage("Le titre extrait doit correspondre à 'Salade de tomates'")
            .that(candidates.last().candidateText)
            .isEqualTo("Salade de tomates")

        // 5. Line with sel et poivre ending with connector ("et") but with no next line (disqualified by hanging connector "et")
        val res5 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = "1. Ajouter du sel et poivre Salade d'avocat et",
            lineIndex = 0,
            lines = listOf("1. Ajouter du sel et poivre Salade d'avocat et"),
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("salade", "avocat"),
            containsActionRegex = null,
            firstIngredientWord = "avocat",
            candidates = candidates
        )
        assertWithMessage("Un titre se terminant par un connecteur suspendu 'et' sans ligne suivante doit être ignoré")
            .that(res5)
            .isFalse()

        // 6. Line with sel et poivre, potential title without ending connector, but next line IS a connector
        val linesMulti = listOf("1. Ajouter du sel et poivre Salade de tomates", "au basilic")
        val res6 = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
            workingLine = linesMulti[0],
            lineIndex = 0,
            lines = linesMulti,
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("salade", "tomates", "basilic"),
            containsActionRegex = null,
            firstIngredientWord = "tomates",
            candidates = candidates
        )
        assertWithMessage("Un titre suivi d'une ligne démarrant par le connecteur 'au basilic' doit fusionner les 2 lignes")
            .that(res6)
            .isTrue()

        assertWithMessage("Le titre fusionné doit contenir 'Salade de tomates au basilic'")
            .that(candidates.last().candidateText)
            .contains("Salade de tomates au basilic")
    }
}
