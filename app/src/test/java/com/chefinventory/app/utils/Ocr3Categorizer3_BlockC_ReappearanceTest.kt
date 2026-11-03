package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer3_BlockC_Reappearance].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer3_BlockC_ReappearanceTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateIsReappearance_DirectCallAllBranches() {
        val seen = setOf("farine")

        // 1. startsWithConnector = true -> false
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("de farine", false, false, false, true, seen)).isFalse()

        // 2. match = true, closed = true -> true
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("ajouter la farine", true, false, false, false, seen)).isTrue()

        // 3. match = true, closed = false, noQty = true -> true
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("farine fluide", false, false, false, false, seen)).isTrue()

        // 4. match = true, closed = false, hasHardQuantity = true -> false
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("200 g de farine", false, true, false, false, seen)).isFalse()

        // 5. match = true, closed = false, hasWeight = true -> false
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("(50 g) de farine", false, false, true, false, seen)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsIsolatedModifier_DirectCall() {
        val mods = listOf("haché")
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("haché", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("haché fin", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachéd'oignons", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachéde persil", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachédu sel", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachédes poivrons", mods)).isTrue()
        assertThat(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("moulu", mods)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBlockCResultDataClassGeneratedMethods() {
        val res1 = BlockCResult(
            lowerLine = "200 g de farine",
            containsAction = false,
            startsWithBullet = false,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            updatedSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            updatedIngredientListClosed = false
        )
        val res2 = res1.copy()
        val res3 = res1.copy(isReappearance = true)

        assertThat(res1).isEqualTo(res2)
        assertThat(res1).isNotEqualTo(res3)
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode())
        assertThat(res1.toString()).contains("BlockCResult")

        val (lower, action, bullet, weight, hardQ, reappear, instTrig, looksIng, sec, closed) = res1
        assertThat(lower).isEqualTo("200 g de farine")
        assertThat(action).isFalse()
        assertThat(bullet).isFalse()
        assertThat(weight).isFalse()
        assertThat(hardQ).isTrue()
        assertThat(reappear).isFalse()
        assertThat(instTrig).isFalse()
        assertThat(looksIng).isTrue()
        assertThat(sec).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        assertThat(closed).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockC_ReappearanceAndHeaders_InstructionAndIngredientHeaders() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()
        val ctx = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)

        // 1. Instruction Header ("Préparation")
        val resInstrHeader = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "Préparation",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resInstrHeader.updatedSection).isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)

        // 2. Ingredient Header ("Ingrédients")
        val resIngrHeader = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "Ingrédients",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resIngrHeader.updatedSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        assertThat(resIngrHeader.updatedIngredientListClosed).isFalse()

        // 3. Composition Header ("composition")
        val resCompositionHeader = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "composition",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resCompositionHeader.updatedSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        assertThat(resCompositionHeader.updatedIngredientListClosed).isFalse()

        // 4. Instruction Trigger Header ("étape")
        val customVocabTrig = vocabulary.copy(instructionSwitchKeywords = listOf("étape"))
        val resTrigHeader = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "étape",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocabTrig,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resTrigHeader.updatedSection).isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockC_ReappearanceAndHeaders_ModifiersReappearanceAndSubHeaders() {
        val results = RawSections()
        val seenKeywords = mutableSetOf("farine")
        val ctx = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)

        // Null containsActionRegex & Plain text without bullets
        val resNullRegex = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = null,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resNullRegex.containsAction).isFalse()
        assertThat(resNullRegex.startsWithBullet).isFalse()

        // Dash bullet line ("- 200 g de farine")
        val resDashBullet = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "- 200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = null,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resDashBullet.startsWithBullet).isTrue()

        // Reappearance matching keyword "farine"
        val resReappear = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "Ajouter la farine",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resReappear.isReappearance).isTrue()

        // Isolated modifiers with prefixes ("haché", "d'oignons", "de persil", "du sel", "des poivrons")
        val customVocab = vocabulary.copy(
            ingredientPreparationModifiers = listOf("haché"),
            ingredientSubHeaders = listOf("Pour la pâte")
        )

        val resMod1 = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "haché",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMod1.looksLikeIngredient).isTrue()

        val resMod2 = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "hachéd'oignons",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMod2.looksLikeIngredient).isTrue()

        val resMod3 = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "hachéde persil",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMod3.looksLikeIngredient).isTrue()

        val resMod4 = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "hachédu sel",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMod4.looksLikeIngredient).isTrue()

        val resMod5 = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "hachédes poivrons",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMod5.looksLikeIngredient).isTrue()

        val resSubHeader = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
            workingLine = "Pour la pâte",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            startsWithConnector = false,
            containsActionRegex = ctx.containsActionRegex,
            hardQtyRegex = ctx.hardQtyRegex,
            qtyRegex = ctx.qtyRegex,
            vocabulary = customVocab,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resSubHeader.updatedSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
    }
}
