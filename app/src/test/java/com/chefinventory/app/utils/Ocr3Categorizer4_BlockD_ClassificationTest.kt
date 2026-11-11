package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer4_BlockD_Classification].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer4_BlockD_ClassificationTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClassifySection_DirectCallAllBranches() {
        // 1. isInstructionSignal = true -> SECTION_INSTRUCTIONS
        val sec1 = Ocr3Categorizer4_BlockD_Classification.classifySection(
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isInstructionSignal = true,
            isIngredientListClosed = false,
            hasHardQuantity = false,
            hasWeight = false,
            looksLikeIngredient = false,
            startsWithConnector = false
        )
        assertWithMessage("Un signal d'instruction doit classifier vers SECTION_INSTRUCTIONS")
            .that(sec1)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)

        // 2. isClosedNoQtyNoWeight = true -> SECTION_INSTRUCTIONS
        val sec2 = Ocr3Categorizer4_BlockD_Classification.classifySection(
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isInstructionSignal = false,
            isIngredientListClosed = true,
            hasHardQuantity = false,
            hasWeight = false,
            looksLikeIngredient = false,
            startsWithConnector = false
        )
        assertWithMessage("Une liste fermée sans quantité ni poids doit classifier vers SECTION_INSTRUCTIONS")
            .that(sec2)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)

        // 3. SECTION_NONE with startsWithConnector = true -> SECTION_INGREDIENTS
        val sec3 = Ocr3Categorizer4_BlockD_Classification.classifySection(
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isInstructionSignal = false,
            isIngredientListClosed = false,
            hasHardQuantity = false,
            hasWeight = false,
            looksLikeIngredient = false,
            startsWithConnector = true
        )
        assertWithMessage("Une ligne démarrant par un connecteur doit classifier vers SECTION_INGREDIENTS")
            .that(sec3)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        // 4. SECTION_INGREDIENTS with looksLikeIngredient = false -> remains SECTION_INGREDIENTS
        val sec4 = Ocr3Categorizer4_BlockD_Classification.classifySection(
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isInstructionSignal = false,
            isIngredientListClosed = false,
            hasHardQuantity = false,
            hasWeight = false,
            looksLikeIngredient = false,
            startsWithConnector = false
        )
        assertWithMessage("La section INGREDIENTS doit rester inchangée si aucun signal inverse n'est détecté")
            .that(sec4)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInstructionSignal_DirectCallAllBranches() {
        // 1. hasUnclosedParenthesis = true -> false
        assertWithMessage("Une parenthèse non refermée doit disqualifier le signal d'instruction")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = true, hasInstructionTrigger = true, isReappearance = true,
                isStepNumber = true, containsAction = true, workingLineLength = 30, startsWithBullet = true
            ))
            .isFalse()

        // 2. hasInstructionTrigger = true -> true
        assertWithMessage("Un déclencheur d'instruction doit valider le signal d'instruction")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = false, hasInstructionTrigger = true, isReappearance = false,
                isStepNumber = false, containsAction = false, workingLineLength = 10, startsWithBullet = false
            ))
            .isTrue()

        // 3. isReappearance = true -> true
        assertWithMessage("Une réapparition d'ingrédient doit valider le signal d'instruction")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = false, hasInstructionTrigger = false, isReappearance = true,
                isStepNumber = false, containsAction = false, workingLineLength = 10, startsWithBullet = false
            ))
            .isTrue()

        // 4. isStepNumber = true -> true
        assertWithMessage("Un numéro d'étape doit valider le signal d'instruction")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = false, hasInstructionTrigger = false, isReappearance = false,
                isStepNumber = true, containsAction = false, workingLineLength = 10, startsWithBullet = false
            ))
            .isTrue()

        // 5. containsAction = true and workingLineLength > 25 -> true
        assertWithMessage("Un verbe d'action dans une ligne longue (> 25) doit valider le signal d'instruction")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = false, hasInstructionTrigger = false, isReappearance = false,
                isStepNumber = false, containsAction = true, workingLineLength = 30, startsWithBullet = false
            ))
            .isTrue()

        // 6. containsAction = false -> false
        assertWithMessage("L'absence d'action et de déclencheur doit retourner un signal d'instruction faux")
            .that(Ocr3Categorizer4_BlockD_Classification.isInstructionSignal(
                hasUnclosedParenthesis = false, hasInstructionTrigger = false, isReappearance = false,
                isStepNumber = false, containsAction = false, workingLineLength = 10, startsWithBullet = false
            ))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBlockDResultDataClassGeneratedMethods() {
        val res1 = BlockDResult(shouldContinue = false, workingLine = "Couper les pommes", updatedSection = Ocr3Categorizer.SECTION_INSTRUCTIONS)
        val res2 = res1.copy()
        val res3 = res1.copy(shouldContinue = true)

        assertWithMessage("Les objets BlockDResult identiques doivent être égaux")
            .that(res1)
            .isEqualTo(res2)

        assertWithMessage("Les objets BlockDResult avec une propriété différente ne doivent pas être égaux")
            .that(res1)
            .isNotEqualTo(res3)

        assertWithMessage("Le hashcode de la copie doit correspondre à l'original")
            .that(res1.hashCode())
            .isEqualTo(res2.hashCode())

        assertWithMessage("La méthode toString doit contenir le nom de la classe")
            .that(res1.toString())
            .contains("BlockDResult")

        val (cont, line, sec) = res1
        assertWithMessage("Le composant shouldContinue déconstruit doit être faux")
            .that(cont)
            .isFalse()

        assertWithMessage("Le composant workingLine déconstruit doit correspondre à 'Couper les pommes'")
            .that(line)
            .isEqualTo("Couper les pommes")

        assertWithMessage("Le composant updatedSection déconstruit doit correspondre à SECTION_INSTRUCTIONS")
            .that(sec)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockD_SectionClassification_NoiseAndInstructionSignal() {
        val results = RawSections()
        val wineState = Ocr3CategorizerWine.WineState()

        // 1. Noise line with no letters ("★ ★ ★")
        val resNoise = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "★ ★ ★",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une ligne de bruit sans lettres doit continuer le pipeline")
            .that(resNoise.shouldContinue)
            .isTrue()

        // 2. Closed wine section noise line
        wineState.isWineSectionClosed = true
        val resWineClosedNoise = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "L'abus d'alcool est dangereux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une ligne de bruit dans une section vin fermée doit continuer le pipeline")
            .that(resWineClosedNoise.shouldContinue)
            .isTrue()

        // 3. Instruction signal (step number "1. Couper")
        wineState.isWineSectionClosed = false
        val resStep = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "1. Couper les oignons",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Un numéro d'étape avec verbe d'action ne doit pas continuer sans classification")
            .that(resStep.shouldContinue)
            .isFalse()

        assertWithMessage("La section mise à jour doit basculer vers SECTION_INSTRUCTIONS")
            .that(resStep.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockD_SectionClassification_UnclosedParenthesisAndSectionIngredientsBranch() {
        val results = RawSections()
        val wineState = Ocr3CategorizerWine.WineState()

        // 1. Unclosed parenthesis in last ingredient line ("200 g de farine (type 55")
        results.rawIngredientsList.add("200 g de farine (type 55")
        val resUnclosed = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "1. Couper les oignons",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une parenthèse non refermée dans le dernier ingrédient doit bloquer la bascule")
            .that(resUnclosed.shouldContinue)
            .isFalse()

        // 2. Looks like ingredient in SECTION_NONE -> becomes SECTION_INGREDIENTS
        val resIngrBranch = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "200 g de farine",
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
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une ligne ressemblant à un ingrédient doit classifier vers SECTION_INGREDIENTS")
            .that(resIngrBranch.shouldContinue)
            .isFalse()

        assertWithMessage("La section mise à jour doit être SECTION_INGREDIENTS")
            .that(resIngrBranch.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        // 3. Closed wine section with containsAction = true (should NOT continue)
        wineState.isWineSectionClosed = true
        val resWineClosedWithAction = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "Chauffer le vin",
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
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une section vin fermée avec verbe d'action ne doit pas continuer sans traitement")
            .that(resWineClosedWithAction.shouldContinue)
            .isFalse()

        // 4. Closed wine section with startsWithBullet = true (should NOT continue)
        val resWineClosedWithBullet = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
            rawWorkingLine = "• château margaux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = true,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            wineState = wineState,
            vocabulary = vocabulary,
            results = results
        )
        assertWithMessage("Une section vin fermée démarrant par une puce ne doit pas continuer sans traitement")
            .that(resWineClosedWithBullet.shouldContinue)
            .isFalse()
    }
}
