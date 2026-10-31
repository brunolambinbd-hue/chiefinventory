package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
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
    fun testTryMergeUnfinishedQuantityLine_QuantityUnitOnly_MergesNextLine() {
        val rawList = mutableListOf("1 cuillère à soupe")
        val keywords = mutableSetOf<String>()

        val merged = Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
            workingLine = "de moutarde",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            vocabulary = vocabulary,
            rawIngredientsList = rawList,
            seenIngredientKeywords = keywords
        )

        assertThat(merged).isTrue()
        assertThat(rawList).containsExactly("1 cuillère à soupe de moutarde")
        assertThat(keywords).contains("moutarde")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTryMergeUnfinishedQuantityLine_ExhaustiveUnitsAndModifiers() {
        val testUnits = listOf("1 g", "2 kg", "25 cl", "100 ml", "1 l", "1 cuillère à soupe", "2 c. à café", "1 gousse")

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

            assertThat(merged).isTrue()
            assertThat(rawList.last()).contains("d'ail haché")
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

        assertThat(merged).isTrue()
        assertThat(rawList).containsExactly("1 petite salade frisée")
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

        assertThat(merged).isFalse()
        assertThat(rawList).containsExactly("200 g de farine")
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
        assertThat(merged1).isFalse()

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
        assertThat(merged2).isFalse()

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
        assertThat(merged3).isFalse()
    }
}
