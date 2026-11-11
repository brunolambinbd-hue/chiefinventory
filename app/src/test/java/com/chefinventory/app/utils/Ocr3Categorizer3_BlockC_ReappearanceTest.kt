package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Une ligne démarrant par un connecteur ne doit pas être qualifiée de réapparition")
            .that(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("de farine", false, false, false, true, seen))
            .isFalse()

        // 2. match = true, closed = true -> true
        assertWithMessage("Un mot-clé d'ingrédient déjà vu dans une liste fermée doit être qualifié de réapparition")
            .that(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("ajouter la farine", true, false, false, false, seen))
            .isTrue()

        // 3. match = true, closed = false, noQty = true -> true
        assertWithMessage("Un mot-clé d'ingrédient sans quantité dans une liste ouverte doit être qualifié de réapparition")
            .that(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("farine fluide", false, false, false, false, seen))
            .isTrue()

        // 4. match = true, closed = false, hasHardQuantity = true -> false
        assertWithMessage("Un mot-clé avec une quantité stricte ne doit pas être qualifié de réapparition")
            .that(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("200 g de farine", false, true, false, false, seen))
            .isFalse()

        // 5. match = true, closed = false, hasWeight = true -> false
        assertWithMessage("Un mot-clé avec un poids entre parenthèses ne doit pas être qualifié de réapparition")
            .that(Ocr3Categorizer3_BlockC_Reappearance.evaluateIsReappearance("(50 g) de farine", false, false, true, false, seen))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsIsolatedModifier_DirectCall() {
        val mods = listOf("haché")
        assertWithMessage("Le modificateur 'haché' seul doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("haché", mods))
            .isTrue()

        assertWithMessage("Le modificateur 'haché fin' doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("haché fin", mods))
            .isTrue()

        assertWithMessage("Le modificateur 'hachéd'oignons' doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachéd'oignons", mods))
            .isTrue()

        assertWithMessage("Le modificateur 'hachéde persil' doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachéde persil", mods))
            .isTrue()

        assertWithMessage("Le modificateur 'hachédu sel' doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachédu sel", mods))
            .isTrue()

        assertWithMessage("Le modificateur 'hachédes poivrons' doit être reconnu comme modificateur isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("hachédes poivrons", mods))
            .isTrue()

        assertWithMessage("Un modificateur non présent dans la liste ne doit pas être reconnu comme isolé")
            .that(Ocr3Categorizer3_BlockC_Reappearance.isIsolatedModifier("moulu", mods))
            .isFalse()
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

        assertWithMessage("Les copies de BlockCResult avec des valeurs identiques doivent être égales")
            .that(res1)
            .isEqualTo(res2)

        assertWithMessage("Les objets BlockCResult avec une propriété différente ne doivent pas être égaux")
            .that(res1)
            .isNotEqualTo(res3)

        assertWithMessage("Le hashcode de la copie de BlockCResult doit correspondre à l'original")
            .that(res1.hashCode())
            .isEqualTo(res2.hashCode())

        assertWithMessage("La méthode toString de BlockCResult doit contenir le nom de la classe")
            .that(res1.toString())
            .contains("BlockCResult")

        val (lower, action, bullet, weight, hardQ, reappear, instTrig, looksIng, sec, closed) = res1
        assertWithMessage("Le composant lowerLine déconstruit doit correspondre")
            .that(lower)
            .isEqualTo("200 g de farine")

        assertWithMessage("Le composant containsAction déconstruit doit être faux")
            .that(action)
            .isFalse()

        assertWithMessage("Le composant startsWithBullet déconstruit doit être faux")
            .that(bullet)
            .isFalse()

        assertWithMessage("Le composant hasWeight déconstruit doit être faux")
            .that(weight)
            .isFalse()

        assertWithMessage("Le composant hasHardQuantity déconstruit doit être vrai")
            .that(hardQ)
            .isTrue()

        assertWithMessage("Le composant isReappearance déconstruit doit être faux")
            .that(reappear)
            .isFalse()

        assertWithMessage("Le composant hasInstructionTrigger déconstruit doit être faux")
            .that(instTrig)
            .isFalse()

        assertWithMessage("Le composant looksLikeIngredient déconstruit doit être vrai")
            .that(looksIng)
            .isTrue()

        assertWithMessage("Le composant updatedSection déconstruit doit correspondre à SECTION_INGREDIENTS")
            .that(sec)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        assertWithMessage("Le composant updatedIngredientListClosed déconstruit doit être faux")
            .that(closed)
            .isFalse()
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
        assertWithMessage("L'en-tête 'Préparation' doit faire basculer la section courante vers SECTION_INSTRUCTIONS")
            .that(resInstrHeader.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)

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
        assertWithMessage("L'en-tête 'Ingrédients' doit faire basculer la section courante vers SECTION_INGREDIENTS")
            .that(resIngrHeader.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        assertWithMessage("L'en-tête 'Ingrédients' doit ré-ouvrir la liste des ingrédients")
            .that(resIngrHeader.updatedIngredientListClosed)
            .isFalse()

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
        assertWithMessage("L'en-tête 'composition' doit faire basculer la section courante vers SECTION_INGREDIENTS")
            .that(resCompositionHeader.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        assertWithMessage("L'en-tête 'composition' doit ré-ouvrir la liste des ingrédients")
            .that(resCompositionHeader.updatedIngredientListClosed)
            .isFalse()

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
        assertWithMessage("Le déclencheur d'instruction 'étape' doit basculer vers SECTION_INSTRUCTIONS")
            .that(resTrigHeader.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
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
        assertWithMessage("Une ligne brute d'ingrédient sans verbe d'action ne doit pas contenir d'action")
            .that(resNullRegex.containsAction)
            .isFalse()

        assertWithMessage("Une ligne brute sans puce initiale ne doit pas démarrer par une puce")
            .that(resNullRegex.startsWithBullet)
            .isFalse()

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
        assertWithMessage("Une ligne d'ingrédient débutant par un tiret '-' doit être détectée comme puce")
            .that(resDashBullet.startsWithBullet)
            .isTrue()

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
        assertWithMessage("Le mot 'farine' déjà vu dans la liste fermée doit être qualifié de réapparition")
            .that(resReappear.isReappearance)
            .isTrue()

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
        assertWithMessage("Le modificateur 'haché' seul doit ressembler à un ingrédient")
            .that(resMod1.looksLikeIngredient)
            .isTrue()

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
        assertWithMessage("Le modificateur 'hachéd'oignons' doit ressembler à un ingrédient")
            .that(resMod2.looksLikeIngredient)
            .isTrue()

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
        assertWithMessage("Le modificateur 'hachéde persil' doit ressembler à un ingrédient")
            .that(resMod3.looksLikeIngredient)
            .isTrue()

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
        assertWithMessage("Le modificateur 'hachédu sel' doit ressembler à un ingrédient")
            .that(resMod4.looksLikeIngredient)
            .isTrue()

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
        assertWithMessage("Le modificateur 'hachédes poivrons' doit ressembler à un ingrédient")
            .that(resMod5.looksLikeIngredient)
            .isTrue()

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
        assertWithMessage("Le sous-en-tête 'Pour la pâte' doit faire basculer la section vers SECTION_INGREDIENTS")
            .that(resSubHeader.updatedSection)
            .isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
    }
}
