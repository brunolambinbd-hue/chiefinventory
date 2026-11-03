package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer1_BlockA_Metadata].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer1_BlockA_MetadataTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBlockAResultDataClassGeneratedMethods() {
        val res1 = BlockAResult(
            shouldContinue = true,
            workingLine = "200 g de farine",
            lowerLine = "200 g de farine",
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false
        )
        val res2 = res1.copy()
        val res3 = res1.copy(shouldContinue = false)

        assertThat(res1).isEqualTo(res2)
        assertThat(res1).isNotEqualTo(res3)
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode())
        assertThat(res1.toString()).contains("BlockAResult")

        val (cont, line, lower, action, bullet, conn) = res1
        assertThat(cont).isTrue()
        assertThat(line).isEqualTo("200 g de farine")
        assertThat(lower).isEqualTo("200 g de farine")
        assertThat(action).isFalse()
        assertThat(bullet).isFalse()
        assertThat(conn).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockA_MetadataAndHangingLine_TechnicalDimensionAndPureMetadata() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Technical dimension (5 cm x 10 cm)
        val resTech = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "5 cm x 10 cm",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_NONE,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resTech.shouldContinue).isTrue()
        assertThat(results.rawInstructionsList).contains("5 cm x 10 cm")

        // 2. Pure metadata line
        val resMeta = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "4 personnes",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_NONE,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resMeta.shouldContinue).isTrue()
        assertThat(results.detectedServings).isEqualTo("4")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockA_MetadataAndHangingLine_HangingLineContinuation() {
        val results = RawSections()
        results.rawIngredientsList.add("200 g de")
        val seenKeywords = mutableSetOf<String>()

        val resHanging = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "farine fluide",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )

        assertThat(resHanging.shouldContinue).isTrue()
        assertThat(results.rawIngredientsList.last()).isEqualTo("200 g de farine fluide")
        assertThat(seenKeywords).contains("farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockA_MetadataAndHangingLine_BulletsAndInstructionsHangingLine() {
        val results = RawSections()
        results.rawInstructionsList.add("Verser dans")
        val seenKeywords = mutableSetOf<String>()

        // Bullet symbols •, -, * and null containsActionRegex
        val resBulletDot = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "• 100 g de sucre",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resBulletDot.startsWithBullet).isTrue()

        val resBulletDash = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "- 100 g de sucre",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resBulletDash.startsWithBullet).isTrue()

        val resBulletStar = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "* 100 g de sucre",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resBulletStar.startsWithBullet).isTrue()

        // Continuation in SECTION_INSTRUCTIONS
        val resInstructionHanging = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "un moule à gâteau",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resInstructionHanging.shouldContinue).isTrue()
        assertThat(results.rawInstructionsList.last()).isEqualTo("Verser dans un moule à gâteau")

        // Hanging line continuation where containsAction is true (should NOT append)
        val resHangingWithAction = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "verser la farine",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = Regex("(?i)\\bverser\\b"),
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resHangingWithAction.shouldContinue).isFalse()

        // Hanging line continuation where startsWithBullet is true (should NOT append L102)
        val resultsHangingBullet = RawSections()
        resultsHangingBullet.rawIngredientsList.add("200 g de")
        val resHangingBullet = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "• farine fluide",
            results = resultsHangingBullet,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(resHangingBullet.shouldContinue).isFalse()
    }
}
