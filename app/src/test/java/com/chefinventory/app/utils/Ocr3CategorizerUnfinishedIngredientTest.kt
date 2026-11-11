package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la fusion d'ingrédients inachevés par [Ocr3CategorizerUnfinishedIngredient].
 */
class Ocr3CategorizerUnfinishedIngredientTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_ModifierEndsWithSuffixBranch() {
        val rawList = mutableListOf("200 g de frais")
        val keywords = mutableSetOf<String>()

        val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "haché",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )

        assertWithMessage("Le modificateur 'haché' se terminant par un suffixe doit être fusionné")
            .that(merged)
            .isTrue()

        assertWithMessage("La ligne d'ingrédient fusionnée doit correspondre à '200 g de frais haché'")
            .that(rawList)
            .containsExactly("200 g de frais haché")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_PluralModifierAndBlankParsedNameAfterMerge() {
        // 1. Plural modifier "2 petites" matching lastLower.endsWith(" $mod")
        val rawList1 = mutableListOf("2 petites")
        val keywords1 = mutableSetOf<String>()

        val merged1 = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "feuilles de chicon",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList1,
            seenIngredientKeywords = keywords1
        )

        assertWithMessage("La quantité au pluriel '2 petites' doit fusionner avec 'feuilles de chicon'")
            .that(merged1)
            .isTrue()

        assertWithMessage("La ligne d'ingrédient fusionnée doit être '2 petites feuilles de chicon'")
            .that(rawList1)
            .containsExactly("2 petites feuilles de chicon")

        // 2. Quantity merge with blank line producing blank parsed name ("1" + "   " -> "1   ")
        val rawList2 = mutableListOf("1")
        val keywords2 = mutableSetOf<String>()

        val merged2 = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "   ",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList2,
            seenIngredientKeywords = keywords2
        )

        assertWithMessage("La fusion avec une ligne blanche doit retourner vrai sans ajouter de mots-clés")
            .that(merged2)
            .isTrue()

        assertWithMessage("Aucun mot-clé ne doit être ajouté pour un nom d'ingrédient blanc")
            .that(keywords2)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_QuantityUnitOnly_MergesNextLine() {
        val rawList = mutableListOf("1 cuillère à soupe")
        val keywords = mutableSetOf<String>()

        val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "de farine de blé",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )

        assertWithMessage("L'unité seule '1 cuillère à soupe' doit fusionner avec la suite 'de farine de blé'")
            .that(merged)
            .isTrue()

        assertWithMessage("La ligne fusionnée doit correspondre à '1 cuillère à soupe de farine de blé'")
            .that(rawList)
            .containsExactly("1 cuillère à soupe de farine de blé")

        assertWithMessage("Le mot-clé 'farine' doit être enregistré")
            .that(keywords)
            .contains("farine")

        assertWithMessage("Le mot-clé 'blé' doit être enregistré")
            .that(keywords)
            .contains("blé")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_ExhaustiveUnitsAndModifiers() {
        val testUnits = listOf("1 g", "2 kg", "25 cl", "100 ml", "1 l", "1 cuillère à soupe", "2 c. à café", "1 gousse", "gousse")

        testUnits.forEach { unfinishedLine ->
            val rawList = mutableListOf(unfinishedLine)
            val keywords = mutableSetOf<String>()

            val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
                workingLine = "d'ail haché",
                currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
                containsAction = false,
                startsWithBullet = false,
                vocabulary = vocabulary,
                rawIngredientsList = rawList,
                seenIngredientKeywords = keywords
            )

            assertWithMessage("L'unité inachevée '$unfinishedLine' doit fusionner avec 'd'ail haché'")
                .that(merged)
                .isTrue()

            assertWithMessage("La ligne d'ingrédient fusionnée pour '$unfinishedLine' doit contenir 'd'ail haché'")
                .that(rawList.last())
                .contains("d'ail haché")
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_ModifierNameOnly_MergesNextLine() {
        val rawList = mutableListOf("1 petite")
        val keywords = mutableSetOf<String>()

        val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "salade frisée",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )

        assertWithMessage("L'adjectif seul '1 petite' doit fusionner avec la suite 'salade frisée'")
            .that(merged)
            .isTrue()

        assertWithMessage("La ligne fusionnée doit correspondre à '1 petite salade frisée'")
            .that(rawList)
            .containsExactly("1 petite salade frisée")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_AlreadyCompleteIngredient_DoesNotMerge() {
        val rawList = mutableListOf("200 g de farine")
        val keywords = mutableSetOf("farine")

        val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "100 g de beurre",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )

        assertWithMessage("Un ingrédient déjà complet '200 g de farine' ne doit pas fusionner avec le suivant")
            .that(merged)
            .isFalse()

        assertWithMessage("La liste des ingrédients bruts doit rester inchangée")
            .that(rawList)
            .containsExactly("200 g de farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_GuardsAndEarlyExits() {
        val rawList = mutableListOf("1 cuillère à soupe")
        val keywords = mutableSetOf<String>()

        // 1. Current section is not INGREDIENTS
        val merged1 = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "de farine",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )
        assertWithMessage("Une section non-ingrédient doit disqualifier la fusion")
            .that(merged1)
            .isFalse()

        // 2. Contains action verb or bullet
        val merged2 = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "Mélanger de la farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = true,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )
        assertWithMessage("Une ligne contenant un verbe d'action ne doit pas être fusionnée")
            .that(merged2)
            .isFalse()

        // 3. Raw list is empty
        val merged3 = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = mutableListOf(),
            seenIngredientKeywords = keywords
        )
        assertWithMessage("Une liste d'ingrédients bruts vide doit disqualifier la fusion")
            .that(merged3)
            .isFalse()
    }
}
