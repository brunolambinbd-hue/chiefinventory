package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Les copies de BlockAResult avec des valeurs identiques doivent être égales")
            .that(res1)
            .isEqualTo(res2)

        assertWithMessage("Les objets BlockAResult avec une propriété différente ne doivent pas être égaux")
            .that(res1)
            .isNotEqualTo(res3)

        assertWithMessage("Le hashcode de la copie de BlockAResult doit correspondre à l'original")
            .that(res1.hashCode())
            .isEqualTo(res2.hashCode())

        assertWithMessage("La méthode toString de BlockAResult doit contenir le nom de la classe")
            .that(res1.toString())
            .contains("BlockAResult")

        val (cont, line, lower, action, bullet, conn) = res1
        assertWithMessage("Le composant shouldContinue déconstruit doit être vrai")
            .that(cont)
            .isTrue()

        assertWithMessage("Le composant workingLine déconstruit doit correspondre à '200 g de farine'")
            .that(line)
            .isEqualTo("200 g de farine")

        assertWithMessage("Le composant lowerLine déconstruit doit correspondre à '200 g de farine'")
            .that(lower)
            .isEqualTo("200 g de farine")

        assertWithMessage("Le composant containsAction déconstruit doit être faux")
            .that(action)
            .isFalse()

        assertWithMessage("Le composant startsWithBullet déconstruit doit être faux")
            .that(bullet)
            .isFalse()

        assertWithMessage("Le composant startsWithConnector déconstruit doit être faux")
            .that(conn)
            .isFalse()
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
        assertWithMessage("Le traitement d'une dimension technique doit signaler de continuer")
            .that(resTech.shouldContinue)
            .isTrue()

        assertWithMessage("La dimension technique doit être redirigée vers la liste brute d'instructions")
            .that(results.rawInstructionsList)
            .contains("5 cm x 10 cm")

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
        assertWithMessage("Le traitement de méta-données pures (portions) doit continuer le pipeline")
            .that(resMeta.shouldContinue)
            .isTrue()

        assertWithMessage("Le nombre de personnes '4' doit être extrait dans detectedServings")
            .that(results.detectedServings)
            .isEqualTo("4")
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

        assertWithMessage("La continuation d'une ligne d'ingrédient suspendue doit continuer le pipeline")
            .that(resHanging.shouldContinue)
            .isTrue()

        assertWithMessage("La ligne suspendue '200 g de' et la suite 'farine fluide' doivent être fusionnées")
            .that(results.rawIngredientsList.last())
            .isEqualTo("200 g de farine fluide")

        assertWithMessage("Le mot-clé d'ingrédient 'farine' doit être enregistré dans seenKeywords")
            .that(seenKeywords)
            .contains("farine")
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
        assertWithMessage("Une ligne commençant par une puce '•' doit être détectée comme telle")
            .that(resBulletDot.startsWithBullet)
            .isTrue()

        val resBulletDash = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "- 100 g de sucre",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une ligne commençant par un tiret '-' doit être détectée comme puce")
            .that(resBulletDash.startsWithBullet)
            .isTrue()

        val resBulletStar = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
            rawLine = "* 100 g de sucre",
            results = results,
            vocabulary = vocabulary,
            containsActionRegex = null,
            ingredientConnectors = listOf("de"),
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            seenIngredientKeywords = seenKeywords
        )
        assertWithMessage("Une ligne commençant par une étoile '*' doit être détectée comme puce")
            .that(resBulletStar.startsWithBullet)
            .isTrue()

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
        assertWithMessage("La continuation de consigne d'instruction suspendue doit continuer le pipeline")
            .that(resInstructionHanging.shouldContinue)
            .isTrue()

        assertWithMessage("La consigne suspendue 'Verser dans' et la suite 'un moule à gâteau' doivent être fusionnées")
            .that(results.rawInstructionsList.last())
            .isEqualTo("Verser dans un moule à gâteau")

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
        assertWithMessage("Une ligne contenant un verbe d'action ne doit pas être fusionnée avec la précédente")
            .that(resHangingWithAction.shouldContinue)
            .isFalse()

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
        assertWithMessage("Une ligne d'ingrédient commençant par une puce ne doit pas fusionner avec la précédente")
            .that(resHangingBullet.shouldContinue)
            .isFalse()
    }
}
