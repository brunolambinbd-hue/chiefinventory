package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer5_BlockE_Dispatch].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer5_BlockE_DispatchTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsEndMarkerLine_AllVariants() {
        assertWithMessage("La ligne 'sel et poivre' doit être reconnue comme marqueur de fin")
            .that(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel et poivre"))
            .isTrue()

        assertWithMessage("La ligne 'sel, poivre' doit être reconnue comme marqueur de fin")
            .that(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel, poivre"))
            .isTrue()

        assertWithMessage("Le mot 'sel' seul doit être reconnu comme marqueur de fin")
            .that(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel"))
            .isTrue()

        assertWithMessage("Le mot 'poivre' seul doit être reconnu comme marqueur de fin")
            .that(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("poivre"))
            .isTrue()

        assertWithMessage("Le mot 'farine' ne doit pas être reconnu comme marqueur de fin")
            .that(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("farine"))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateShouldPullBack_AllBranches() {
        // 1. isReappearance = true -> false
        assertWithMessage("Une réapparition d'ingrédient ne doit pas provoquer de recul vers la section précédente")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = true, isHyphenatedInstruction = false, isEndMarkerLine = false,
                containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = true
            ))
            .isFalse()

        // 2. isHyphenatedInstruction = true -> false
        assertWithMessage("Une instruction avec césure ne doit pas provoquer de recul")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = true, isEndMarkerLine = false,
                containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = true
            ))
            .isFalse()

        // 3. isEndMarkerLine && !containsAction -> true
        assertWithMessage("Un marqueur de fin sans action doit provoquer le recul de classification vers les ingrédients")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = true,
                containsAction = false, closedState = false, isStrictWithQty = false, isCommonWithNoAction = false
            ))
            .isTrue()

        // 4. closedState = true with isCommonWithNoAction = true
        assertWithMessage("Un ingrédient connu sans action dans une liste fermée doit provoquer le recul")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = false,
                containsAction = false, closedState = true, isStrictWithQty = false, isCommonWithNoAction = true
            ))
            .isTrue()

        // 5. closedState = false with isStrictWithQty = true
        assertWithMessage("Un ingrédient avec quantité stricte doit provoquer le recul vers la section ingrédients")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = false,
                containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = false
            ))
            .isTrue()

        // 6. isEndMarkerLine = true with containsAction = true -> false
        assertWithMessage("Un marqueur de fin contenant une action ne doit pas provoquer de recul")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = true,
                containsAction = true, closedState = false, isStrictWithQty = false, isCommonWithNoAction = false
            ))
            .isFalse()

        // 7. closedState = true with isStrictWithQty = false and isCommonWithNoAction = false -> false
        assertWithMessage("Une ligne ordinaire dans une liste fermée sans quantité ni ingrédient connu ne doit pas provoquer de recul")
            .that(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
                isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = false,
                containsAction = false, closedState = true, isStrictWithQty = false, isCommonWithNoAction = false
            ))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_IngredientsAndInstructionsAndFallback() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Dispatch INGREDIENTS section ("200 g de farine")
        val closed1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "200 g de farine",
            lowerLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("L'ingrédient '200 g de farine' ne doit pas fermer la liste des ingrédients")
            .that(closed1)
            .isFalse()

        assertWithMessage("L'ingrédient doit être ajouté dans rawIngredientsList")
            .that(results.rawIngredientsList)
            .contains("200 g de farine")

        // 2. Dispatch INGREDIENTS section end-marker ("sel et poivre")
        val closed2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "sel et poivre",
            lowerLine = "sel et poivre",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Le marqueur de fin 'sel et poivre' doit fermer la liste des ingrédients")
            .that(closed2)
            .isTrue()

        // 3. Dispatch INGREDIENTS section with containsAction = true
        val closedActionIngr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "couper les oignons",
            lowerLine = "couper les oignons",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une ligne avec verbe d'action ne doit pas fermer immédiatement la liste")
            .that(closedActionIngr)
            .isFalse()

        // 4. Dispatch INSTRUCTIONS section ("Faire cuire au four")
        val closed3 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Faire cuire au four",
            lowerLine = "faire cuire au four",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une instruction de cuisson doit marquer la liste des ingrédients comme fermée")
            .that(closed3)
            .isTrue()

        assertWithMessage("L'instruction doit être ajoutée dans rawInstructionsList")
            .that(results.rawInstructionsList)
            .contains("Faire cuire au four")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_TechnicalDimensionHyphenatedInstructionAndFallback() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Technical dimension in SECTION_INGREDIENTS ("5 cm x 10 cm")
        val closedTech = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "5 cm x 10 cm",
            lowerLine = "5 cm x 10 cm",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une dimension technique ne doit pas fermer la liste des ingrédients")
            .that(closedTech)
            .isFalse()

        // 2. Hyphenated instruction in SECTION_INSTRUCTIONS ("verser -")
        val closedHyphen = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "verser -",
            lowerLine = "verser -",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une consigne d'instruction avec césure ne doit pas fermer la liste")
            .that(closedHyphen)
            .isFalse()

        assertWithMessage("La consigne tronquée doit être ajoutée à rawInstructionsList")
            .that(results.rawInstructionsList)
            .contains("verser -")

        // 3. Technical dimension in SECTION_INSTRUCTIONS ("5 cm x 10 cm")
        val closedTechInInstructions = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "5 cm x 10 cm",
            lowerLine = "5 cm x 10 cm",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une dimension technique en section d'instructions ne doit pas fermer la liste")
            .that(closedTechInInstructions)
            .isFalse()

        // 4. Fallback SECTION_NONE looksLikeIngredient = true
        val closedNoneIngr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "100 g de sucre",
            lowerLine = "100 g de sucre",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un ingrédient en section neutre (NONE) ne doit pas fermer la liste")
            .that(closedNoneIngr)
            .isFalse()

        assertWithMessage("L'ingrédient doit être ajouté à rawIngredientsList en fallback")
            .that(results.rawIngredientsList)
            .contains("100 g de sucre")

        // 5. Fallback SECTION_NONE startsWithConnector = true
        val closedConnectorNone = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "de farine",
            lowerLine = "de farine",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un connecteur en section neutre ne doit pas fermer la liste")
            .that(closedConnectorNone)
            .isFalse()

        // 6. Fallback SECTION_NONE looksLikeIngredient = false
        val closedNoneInstr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Remuer énergiquement",
            lowerLine = "remuer énergiquement",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une consigne en section neutre ne doit pas fermer la liste")
            .that(closedNoneInstr)
            .isFalse()

        assertWithMessage("La consigne d'instruction doit être ajoutée à rawInstructionsList en fallback")
            .that(results.rawInstructionsList)
            .contains("Remuer énergiquement")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_CommonIngredientWithActionInInstructions() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // Common ingredient with containsAction = true in SECTION_INSTRUCTIONS ("couper le beurre")
        val closedCommonAction = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "couper le beurre",
            lowerLine = "couper le beurre",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un ingrédient courant avec verbe d'action en section d'instructions doit maintenir la liste fermée")
            .that(closedCommonAction)
            .isTrue()

        assertWithMessage("La consigne d'action doit être conservée dans rawInstructionsList")
            .that(results.rawInstructionsList)
            .contains("couper le beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_YellowLinesYellowToGreen() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. hasInstructionTrigger = true with looksLikeIngredient = true in SECTION_INSTRUCTIONS ("pour la farine")
        val closedTrigger = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "pour la farine",
            lowerLine = "pour la farine",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un ingrédient ressemblant avec déclencheur d'instruction ne doit pas fermer la liste")
            .that(closedTrigger)
            .isFalse()

        // 2. startsWithBullet = true with isCommonIngredient = true in SECTION_INSTRUCTIONS ("• le beurre")
        val closedBulletBeurre = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "• le beurre",
            lowerLine = "• le beurre",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = true,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un ingrédient à puce en section d'instructions ne doit pas fermer la liste")
            .that(closedBulletBeurre)
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SeasoningFollowedByMoreAndUnclosedParenInPrevInstruction() {
        val customVocab = vocabulary.copy(
            logOcr3BlockEDispatchLockActivated = "Verrouillage activé"
        )
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Seasoning "Sel" followed by "de Guérande" on next line
        val closed1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Sel",
            lowerLine = "sel",
            nextLine = "de Guérande",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Du sel suivi de 'de Guérande' ne doit pas verrouiller la liste")
            .that(closed1)
            .isFalse()

        // 2. Unclosed parenthesis in previous instruction prevents pulling back ingredient
        results.rawInstructionsList.add("Préchauffer le four (à 180°C")
        val closed2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "farine 200 g",
            lowerLine = "farine 200 g",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = true,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une parenthèse non fermée dans l'instruction précédente doit empêcher l'aspiration d'ingrédient")
            .that(closed2)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_EmptyLogsAndKeywordAlreadySeen() {
        val blankLogVocab = vocabulary.copy(
            logOcr3BlockEDispatchLockActivated = ""
        )
        val results = RawSections()
        val seenKeywords = mutableSetOf("haricots")

        // 1. Salt marker with blank log string
        val closed1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Sel",
            lowerLine = "sel",
            nextLine = "",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = blankLogVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Le verrouillage avec journalisation vide doit s'exécuter sans erreur")
            .that(closed1)
            .isTrue()

        // 2. isKeywordAlreadySeen = true in SECTION_INSTRUCTIONS
        val closed2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "haricots verts",
            lowerLine = "haricots verts",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = true,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = blankLogVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Un mot-clé déjà vu avec liste fermée ne doit pas ré-aspirer la ligne")
            .that(closed2)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_AdditionalCoverageBranches() {
        val customVocab = vocabulary.copy(
            logOcr3BlockEDispatchLockActivated = "Verrou"
        )
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Seasoning "Sel" followed by "poivre" on next line
        val c1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Sel", lowerLine = "sel", nextLine = "poivre moulu",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = false, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("c1").that(c1).isFalse()

        // 2. Seasoning "Sel" followed by "sel" on next line
        val c2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Sel", lowerLine = "sel", nextLine = "sel de mer",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = false, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("c2").that(c2).isFalse()

        // 3. Header variants ("Ingrédients de la recette", "Ingrédients(pour 4)", "Ingrédients:")
        val h1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Ingrédients de la recette", lowerLine = "ingrédients de la recette",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = false, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = false,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("h1").that(h1).isFalse()

        val h2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Ingrédients(pour 4)", lowerLine = "ingrédients(pour 4)",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = false, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = false,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("h2").that(h2).isFalse()

        val h3 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Ingrédients:", lowerLine = "ingrédients:",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = false, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = false,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("h3").that(h3).isFalse()

        // 4. Technical dimension in SECTION_INSTRUCTIONS
        val t1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "10 cm x 20 cm", lowerLine = "10 cm x 20 cm",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("t1").that(t1).isFalse()

        // 5. PullBack in SECTION_INSTRUCTIONS with endMarkerLine = true
        val p1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "sel et poivre", lowerLine = "sel et poivre",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = false, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("p1").that(p1).isTrue()

        // 6. isKeywordAlreadySeen = true in SECTION_INSTRUCTIONS
        seenKeywords.add("beurre")
        val customVocabKeywords = vocabulary.copy(
            commonIngredientsNoQty = listOf("beurre")
        )
        val kw1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "beurre 100 g", lowerLine = "beurre 100 g",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS, isIngredientListClosed = true,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = true, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocabKeywords, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("kw1").that(kw1).isTrue()

        // 7. Unclosed parenthesis in prev instruction & SECTION_UNKNOWN (else branch)
        results.rawInstructionsList.add("Mélanger (doucement")
        val customHangingVocab = vocabulary.copy(hangingConnectors = listOf("d'"))
        val unclosedParenRes = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "farine 100 g", lowerLine = "farine 100 g",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = true, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customHangingVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Unclosed paren in prev instruction prevents pullback").that(unclosedParenRes).isFalse()

        // Else section dispatch (e.g. currentSection = 99)
        val elseSectionRes = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "farine 100 g", lowerLine = "farine 100 g",
            currentSection = 99, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = true, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = customVocab, results = results, seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Else section dispatch returns closedState").that(elseSectionRes).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAtomicHelpers_EvalSeasoning_HangingConnector_KeywordAlreadySeen() {
        // 1. evalSeasoningClosedState
        val s1 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Sel", "poivre", containsAction = true, closedState = false, vocabulary = vocabulary)
        assertWithMessage("s1").that(s1).isFalse()

        val s2 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Farine", "beurre", containsAction = false, closedState = false, vocabulary = vocabulary)
        assertWithMessage("s2").that(s2).isFalse()

        val s3 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Sel", "poivre", containsAction = false, closedState = false, vocabulary = vocabulary)
        assertWithMessage("s3").that(s3).isFalse()

        val customOriginsVocab = vocabulary.copy(saltVarietyOrigins = listOf("de Guérande"))
        val s4 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Sel", "de Guérande", containsAction = false, closedState = false, vocabulary = customOriginsVocab)
        assertWithMessage("s4").that(s4).isFalse()

        val customPepperVocab = vocabulary.copy(pepperDeOriginItems = listOf("de Sichuan"))
        val s5 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Poivre", "de Sichuan", containsAction = false, closedState = false, vocabulary = customPepperVocab)
        assertWithMessage("s5").that(s5).isFalse()

        val s6 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Sel et poivre", "Cuire au four", containsAction = false, closedState = false, vocabulary = vocabulary)
        assertWithMessage("s6").that(s6).isTrue()

        // 2. checkPrevInstEndsWithHangingConnector
        assertWithMessage("blank line").that(Ocr3Categorizer5_BlockE_Dispatch.checkPrevInstEndsWithHangingConnector("", listOf("d'"))).isFalse()
        assertWithMessage("hanging conn match").that(Ocr3Categorizer5_BlockE_Dispatch.checkPrevInstEndsWithHangingConnector("Mélanger d'", listOf("d'"))).isTrue()

        // 3. checkKeywordAlreadySeen
        assertWithMessage("closedState false").that(Ocr3Categorizer5_BlockE_Dispatch.checkKeywordAlreadySeen(false, "farine", listOf("farine"), setOf("farine"))).isFalse()
        assertWithMessage("ingredient len < 4").that(Ocr3Categorizer5_BlockE_Dispatch.checkKeywordAlreadySeen(true, "ail", listOf("ail"), setOf("ail"))).isFalse()
        assertWithMessage("keyword seen").that(Ocr3Categorizer5_BlockE_Dispatch.checkKeywordAlreadySeen(true, "farine", listOf("farine"), setOf("farine"))).isTrue()
        assertWithMessage("keyword not seen").that(Ocr3Categorizer5_BlockE_Dispatch.checkKeywordAlreadySeen(true, "farine", listOf("farine"), setOf("beurre"))).isFalse()

        // 4. evalSeasoningClosedState with empty nextLine and logLock
        val emptyLogVocab = vocabulary.copy(logOcr3BlockEDispatchLockActivated = "")
        val s7 = Ocr3Categorizer5_BlockE_Dispatch.evalSeasoningClosedState("Sel et poivre", "", containsAction = false, closedState = false, vocabulary = emptyLogVocab)
        assertWithMessage("s7").that(s7).isTrue()

        // 5. Else section dispatch with looksLikeIngredient = true (Line 240)
        val resElse = RawSections()
        val keywordsElse = mutableSetOf<String>()
        val elseIngrRes = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "farine 100 g", lowerLine = "farine 100 g",
            currentSection = 99, isIngredientListClosed = false,
            containsAction = false, startsWithBullet = false, startsWithConnector = false,
            hasWeight = true, hasHardQuantity = true, isReappearance = false,
            hasInstructionTrigger = false, looksLikeIngredient = true,
            vocabulary = vocabulary, results = resElse, seenIngredientKeywords = keywordsElse
        )
        assertWithMessage("Else section dispatch with looksLikeIngredient true").that(elseIngrRes).isFalse()
    }
}
