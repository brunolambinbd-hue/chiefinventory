package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer2_BlockB_WineSource].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer2_BlockB_WineSourceTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBlockBResultDataClassGeneratedMethods() {
        val res1 = BlockBResult(shouldContinue = true, workingLine = "1 cuillère à soupe")
        val res2 = res1.copy()
        val res3 = res1.copy(shouldContinue = false)

        assertWithMessage("Les objets BlockBResult avec des valeurs identiques doivent être égaux")
            .that(res1)
            .isEqualTo(res2)

        assertWithMessage("Les objets BlockBResult avec une propriété différente ne doivent pas être égaux")
            .that(res1)
            .isNotEqualTo(res3)

        assertWithMessage("Le hashcode de la copie de BlockBResult doit correspondre à l'original")
            .that(res1.hashCode())
            .isEqualTo(res2.hashCode())

        assertWithMessage("La méthode toString de BlockBResult doit contenir le nom de la classe")
            .that(res1.toString())
            .contains("BlockBResult")

        val (cont, line) = res1
        assertWithMessage("Le composant shouldContinue déconstruit doit être vrai")
            .that(cont)
            .isTrue()

        assertWithMessage("Le composant workingLine déconstruit doit correspondre à '1 cuillère à soupe'")
            .that(line)
            .isEqualTo("1 cuillère à soupe")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockB_UnfinishedWineAndExcludedKeywords_WineAndSourceAndPurge() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()
        val wineState = Ocr3CategorizerWine.WineState()

        // 1. Unfinished quantity line ("1 cuillère à soupe" + "de moutarde")
        results.rawIngredientsList.add("1 cuillère à soupe")
        val resUnfinished = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "de moutarde",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertWithMessage("Le rattrapage d'ingrédient inachevé doit continuer le pipeline")
            .that(resUnfinished.shouldContinue)
            .isTrue()

        assertWithMessage("La quantité inachevée '1 cuillère à soupe' doit fusionner avec 'de moutarde'")
            .that(results.rawIngredientsList.last())
            .isEqualTo("1 cuillère à soupe de moutarde")

        // 2. Source line ("Tel: 01 45 67 89 10")
        val resSource = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "Tel: 01 45 67 89 10",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertWithMessage("Le traitement de ligne de source doit continuer le pipeline")
            .that(resSource.shouldContinue)
            .isTrue()

        assertWithMessage("Le numéro de téléphone doit être extrait dans detectedSourceList")
            .that(results.detectedSourceList)
            .contains("01 45 67 89 10")

        // 3. Excluded keywords purge ("200 g de farine pub") -> "200 g de farine"
        val resPurge = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertWithMessage("La ligne purgée d'ingrédients ne doit pas s'arrêter prématurément")
            .that(resPurge.shouldContinue)
            .isFalse()

        assertWithMessage("La ligne nettoyée '200 g de farine' doit être retournée inchangée")
            .that(resPurge.workingLine)
            .isEqualTo("200 g de farine")
    }
}
