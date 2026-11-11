package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement, le scoring et l'extraction
 * du titre principal des recettes par [Ocr3CategorizerTitleExtractor].
 */
class Ocr3CategorizerTitleExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveOcrTitleExtractorBranchCoverage() {
        // 1. Empty stepActionKeywords and no-ingredient lines (L59, L63)
        val emptyActionsVocab = vocabulary.copy(stepActionKeywords = emptyList())
        val noIngLines = listOf("Salade grecque aux olives", "Mélanger et servir")
        val resNoIng = Ocr3CategorizerTitleExtractor.extractTitle(noIngLines, emptyActionsVocab)
        assertWithMessage("Le titre 'Salade grecque aux olives' doit être extrait en l'absence d'ingrédients")
            .that(resNoIng.title)
            .contains("Salade grecque aux olives")

        // 2. Attached title after "sel et poivre" with connector " de" (L80)
        val attachedTitleLines = listOf(
            "1 oignon sel et poivre Tarte de",
            "pommes fraîches"
        )
        val resAttached = Ocr3CategorizerTitleExtractor.extractTitle(attachedTitleLines, vocabulary)
        assertWithMessage("Le titre accolé 'Tarte de pommes fraîches' après 'sel et poivre' doit être extrait")
            .that(resAttached.title)
            .contains("Tarte de pommes fraîches")

        // 3. Orphan noun completions with "petite", "grande", "façon" (L165-174)
        val fasonLines = listOf("Poissons façon", "meunière", "200 g de poisson")
        val resFason = Ocr3CategorizerTitleExtractor.extractTitle(fasonLines, vocabulary)
        assertWithMessage("Le titre 'Poissons façon' doit être complété par 'meunière'")
            .that(resFason.title)
            .contains("meunière")

        // 4. Score penalty for ending period (L366-367)
        val periodLines = listOf("Tarte aux pommes.", "Tarte aux pommes fraîches")
        val resPeriod = Ocr3CategorizerTitleExtractor.extractTitle(periodLines, vocabulary)
        assertWithMessage("Le titre sans point final doit l'emporter sur le titre se terminant par un point")
            .that(resPeriod.title)
            .isEqualTo("Tarte aux pommes fraîches")

        // 5. Disqualifying guards: "portions", trailing stop words, digit limits (L472, L478, L480)
        val disqLines = listOf(
            "Pour 4 portions",
            "Tarte de",
            "Recette 123456",
            "Tarte Tatin"
        )
        val resDisq = Ocr3CategorizerTitleExtractor.extractTitle(disqLines, vocabulary)
        assertWithMessage("Les candidats invalides doivent être disqualifiés au profit de 'Tarte Tatin'")
            .that(resDisq.title)
            .isEqualTo("Tarte Tatin")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOrphanNounCompletion_FeminineAndMasculineVariations() {
        // 1. Feminine adjective + orphan noun
        val linesFeminine = listOf(
            "Tarte aux pommes et sa grande",
            "salade",
            "200 g de farine"
        )
        val resultFem = Ocr3CategorizerTitleExtractor.extractTitle(linesFeminine, vocabulary)
        assertWithMessage("L'adjectif féminin 'grande' doit récupérer le nom 'salade'")
            .that(resultFem.title)
            .contains("salade")

        // 2. Disqualified lines with parentheses or bullets
        val linesDisqualified = listOf(
            "Salade (4 personnes)",
            "• Salade grecque",
            "- Soupe à l'oignon",
            "Tarte aux cerises"
        )
        val resultDisq = Ocr3CategorizerTitleExtractor.extractTitle(linesDisqualified, vocabulary)
        assertWithMessage("Les lignes avec parenthèses et puces doivent être disqualifiées pour extraire 'Tarte aux cerises'")
            .that(resultDisq.title)
            .isEqualTo("Tarte aux cerises")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindFirstIngredientWord_DirectAndDefaultParameterOverloads() {
        // 1. Default parameter overload (vocabulary = null)
        val word1 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("250 g de farine"))
        assertWithMessage("Le premier mot d'ingrédient doit être 'farine'")
            .that(word1)
            .isEqualTo("farine")

        // 2. Explicit vocabulary overload
        val word2 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("1/2 l d'eau"), vocabulary)
        assertWithMessage("Le premier mot d'ingrédient de '1/2 l d'eau' doit être 'eau'")
            .that(word2)
            .isEqualTo("eau")

        // 3. Empty list -> returns null
        val word3 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(emptyList(), vocabulary)
        assertWithMessage("Une liste vide doit retourner null pour le premier ingrédient")
            .that(word3)
            .isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsMultiLineTitleConnector_ExhaustiveOverloads() {
        // 1. Default parameter overload (vocabulary = null)
        assertWithMessage("Le connecteur 'au jambon' doit être reconnu par défaut")
            .that(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("au jambon"))
            .isTrue()

        assertWithMessage("Le texte 'salade verte' ne doit pas être qualifié de connecteur multiligne")
            .that(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("salade verte"))
            .isFalse()

        // 2. Explicit vocabulary overload
        assertWithMessage("Le connecteur 'aux crevettes' doit être reconnu avec le vocabulaire explicit")
            .that(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("aux crevettes", vocabulary))
            .isTrue()

        assertWithMessage("Le texte 'soupe à l'oignon' ne doit pas être qualifié de connecteur multiligne")
            .that(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("soupe à l'oignon", vocabulary))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_AttachedTitleAfterSelEtPoivreMultiLine() {
        val lines = listOf(
            "2 gousses d'ail sel et poivre Salade d'avocat et",
            "princesses aux crevettes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertWithMessage("Le titre multiligne accolé à 'sel et poivre' doit être reconstruit")
            .that(result.title)
            .contains("Salade d'avocat et princesses aux crevettes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_TitleWithEndingPeriodScorePenalty() {
        val lines = listOf(
            "Salade de crevettes.",
            "200 g de crevettes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertWithMessage("Un titre se terminant par un point doit être disqualifié par pénalité de score")
            .that(result.title)
            .isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_EmptyMultilineConnectorsFallback() {
        val emptyConnectorsVocab = vocabulary.copy(titleMultilineConnectors = emptyList())
        val lines = listOf(
            "Salade de fruits",
            "au sirop d'érable",
            "100 g de pommes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, emptyConnectorsVocab)

        assertWithMessage("Un dictionnaire de connecteurs vide doit utiliser les connecteurs de secours")
            .that(result.title)
            .isEqualTo("Salade de fruits au sirop d'érable")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_EmptyLinesAndNoValidCandidates() {
        // 1. List with empty strings
        val linesWithEmpty = listOf("", "Salade de tomates", "")
        val resEmpty = Ocr3CategorizerTitleExtractor.extractTitle(linesWithEmpty, vocabulary)
        assertWithMessage("Les lignes vides doivent être ignorées pour extraire 'Salade de tomates'")
            .that(resEmpty.title)
            .isEqualTo("Salade de tomates")

        // 2. Candidates with score <= 0
        val linesLowScore = listOf("10. Etape 1")
        val resLowScore = Ocr3CategorizerTitleExtractor.extractTitle(linesLowScore, vocabulary)
        assertWithMessage("Les candidats avec un score <= 0 doivent retourner un titre null")
            .that(resLowScore.title)
            .isNull()

        // 3. Completely empty list
        val resNoCandidates = Ocr3CategorizerTitleExtractor.extractTitle(emptyList(), vocabulary)
        assertWithMessage("Une liste totalement vide doit retourner un titre null")
            .that(resNoCandidates.title)
            .isNull()
    }

    /**
     * Vérifie l'extraction complète d'un titre multi-lignes relié par une préposition et réparé orthographiquement.
     */
    @Test
    @CoveredRequirement("6.1.1.9")
    fun `extractTitle should extract two line title connected by au and post process spelling repairs`() {
        val lines = listOf(
            "INGRÉDIENTS",
            "1 melon Galia",
            "8 tranches de jambon",
            "sel et poivre",
            "Büchettes de melon",
            "au jambon italien et au porto",
            "• Pelez le melon et servez."
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertWithMessage("Le titre sur 2 lignes avec lettrine 'Büchettes' doit être réparé en 'Bûchettes de melon au jambon italien et au porto'")
            .that(result.title)
            .isEqualTo("Bûchettes de melon au jambon italien et au porto")
    }

    /**
     * Vérifie que les lignes de consignes d'instructions contenant des verbes d'action ou se terminant par des connecteurs suspendus (ex: "Maintenez... puis") sont disqualifiées.
     */
    @Test
    @CoveredRequirement("6.1.1.9")
    fun `extractTitle should disqualify lines containing action verbs or ending with hanging connectors like puis`() {
        val lines = listOf(
            "Maintenez une petite ébullition pendant 3 mn puis",
            "Fondue à la bière",
            "1. Verser la bière et mélanger."
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertWithMessage("La consigne avec verbe d'action/connecteur suspendu doit être disqualifiée pour extraire 'Fondue à la bière'")
            .that(result.title)
            .isEqualTo("Fondue à la bière")
    }
}
