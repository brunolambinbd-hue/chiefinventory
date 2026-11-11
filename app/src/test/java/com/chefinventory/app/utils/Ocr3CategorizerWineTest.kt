package com.chefinventory.app.utils

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction et la gestion de la section Vin
 * par le sous-composant [Ocr3CategorizerWine].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerWineTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsWineLineAndContainsWholeWordAndLoadResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val wineRes = Ocr3CategorizerWine.loadResources(context.resources)
        assertWithMessage("Les appellations de vins du projet Android doivent être chargées")
            .that(wineRes.appellations)
            .isNotEmpty()

        assertWithMessage("Les producteurs de vins du projet Android doivent être chargés")
            .that(wineRes.producers)
            .isNotEmpty()

        // generic wine words -> false
        assertWithMessage("Le mot générique d'ingrédient 'vin' ne doit pas être qualifié de ligne de recommandation de vin")
            .that(Ocr3CategorizerWine.isWineLine("vin", vocabulary))
            .isFalse()

        // contains whole word edge cases
        assertWithMessage("Une chaîne d'espaces ne doit pas correspondre à un mot entier")
            .that(Ocr3CategorizerWine.containsWholeWord("   ", "Bordeaux"))
            .isFalse()

        assertWithMessage("Un mot à chercher vide ne doit pas correspondre")
            .that(Ocr3CategorizerWine.containsWholeWord("Bordeaux 1996", "   "))
            .isFalse()

        assertWithMessage("Le mot entier 'Bordeaux' dans 'Bordeaux 1996' doit correspondre")
            .that(Ocr3CategorizerWine.containsWholeWord("Bordeaux 1996", "Bordeaux"))
            .isTrue()

        // Short vintage year line (<15)
        assertWithMessage("Une ligne d'année seule '2015' sous en-tête vin doit être qualifiée de vin")
            .that(Ocr3CategorizerWine.isWineLine("2015", vocabulary))
            .isTrue()

        // Wine producer match
        assertWithMessage("Le nom de domaine 'Château Margaux 2015' doit être reconnu comme vin")
            .that(Ocr3CategorizerWine.isWineLine("Château Margaux 2015", vocabulary))
            .isTrue()

        // Test loadResources exception and keyword match
        val mockRes = mock<Resources> {
            on { getStringArray(com.chefinventory.app.R.array.wine_appellations) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_producers) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_keywords) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_title_keywords) } doThrow Resources.NotFoundException()
            on { getString(com.chefinventory.app.R.string.wine_remove_pattern) } doThrow Resources.NotFoundException()
        }

        val emptyRes = Ocr3CategorizerWine.loadResources(mockRes)
        assertWithMessage("Le motif de suppression de vin par défaut doit être vide en cas d'exception")
            .that(emptyRes.removePattern)
            .isEmpty()

        val keywordRes = Ocr3CategorizerWine.WineResources(
            appellations = emptyList(),
            producers = emptyList(),
            keywords = listOf("cuvée", "millesime"),
            titleKeywords = emptyList(),
            removePattern = ""
        )
        assertWithMessage("Une ligne contenant le mot-clé œnologique 'cuvée' doit être qualifiée de vin")
            .that(Ocr3CategorizerWine.isWineLine("Une belle cuvée 2018", keywordRes))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ImplicitWineBlankAndDuplicateBranches() {
        val state = Ocr3CategorizerWine.WineState()

        // Case 1: Implicit wine header line "suggestion :" where cleanWineLine is "" (isNotBlank == false)
        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "suggestion :",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("L'en-tête implicite 'suggestion :' doit être consommé sans ajouter de vin vide")
            .that(handled1)
            .isTrue()

        assertWithMessage("La liste des vins détectés doit rester vide")
            .that(state.detectedWineList)
            .isEmpty()

        // Case 2: Implicit wine duplicate matching SECOND item in multi-element list -> duplicate rejected
        state.detectedWineList.add("Château Margaux 1990")
        state.detectedWineList.add("Bordeaux 1996 75 cl")
        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("La ligne de vin en doublon 'Bordeaux 1996 75 cl' doit être consommée et rejetée")
            .that(handled2)
            .isTrue()

        assertWithMessage("La taille de la liste des vins doit rester de 2 éléments sans doublon")
            .that(state.detectedWineList)
            .hasSize(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_BrandIsNotBlankValidation() {
        val customVocab = vocabulary.copy(knownSourceBrands = listOf("", "   ", "Delhaize"))

        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Delhaize",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = state
        )

        assertWithMessage("La ligne d'enseigne 'Château Delhaize' doit être consommée")
            .that(handled)
            .isTrue()

        assertWithMessage("La présence d'une enseigne connue 'Delhaize' doit fermer la section vin")
            .that(state.isWineSectionClosed)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_KnownBrandAndNonBrandBranches() {
        val customVocab = vocabulary.copy(knownSourceBrands = listOf("Carrefour", "Delhaize"))

        // Case 1: Known brand "Delhaize" present -> closes wine section
        val stateWithBrand = Ocr3CategorizerWine.WineState()
        stateWithBrand.isWineHeaderActive = true

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Delhaize",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = stateWithBrand
        )
        assertWithMessage("La ligne avec marque connue 'Delhaize' doit fermer la section vin")
            .that(handled1)
            .isTrue()

        assertWithMessage("L'état isWineSectionClosed doit être vrai")
            .that(stateWithBrand.isWineSectionClosed)
            .isTrue()

        // Case 2: Non-brand / Non-existent brand "BrandInexistant777" -> does not close wine section
        val stateWithoutBrand = Ocr3CategorizerWine.WineState()
        stateWithoutBrand.isWineHeaderActive = true

        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château BrandInexistant777",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = stateWithoutBrand
        )
        assertWithMessage("Une marque inexistante ne doit pas fermer la section vin")
            .that(handled2)
            .isTrue()

        assertWithMessage("L'état isWineSectionClosed doit rester faux")
            .that(stateWithoutBrand.isWineSectionClosed)
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_Vintage2010Term3Branch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 2010",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("La ligne avec millésime 'Bordeaux 2010' doit être consommée")
            .that(handled)
            .isTrue()

        assertWithMessage("Le vin 'Bordeaux 2010' doit être ajouté à la liste des vins")
            .that(state.detectedWineList)
            .contains("Bordeaux 2010")

        assertWithMessage("L'année seule sous en-tête actif doit fermer la section vin")
            .that(state.isWineSectionClosed)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DuplicateWineUnderActiveHeaderElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true
        state.detectedWineList.add("Château Margaux")

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Margaux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Le vin en doublon sous en-tête actif doit être consommé")
            .that(handled)
            .isTrue()

        assertWithMessage("La liste des vins ne doit pas contenir de doublon")
            .that(state.detectedWineList)
            .hasSize(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DuplicateWineImplicitDetectionElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.detectedWineList.add("Bordeaux 1996 75 cl")

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Le vin implicite en doublon doit être consommé sans ajout")
            .that(handled)
            .isTrue()

        assertWithMessage("La taille de la liste des vins doit rester de 1 élément")
            .that(state.detectedWineList)
            .hasSize(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_PureHeaderLineAndMultiLineWithoutVolume() {
        val state = Ocr3CategorizerWine.WineState()

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Conseil vin :",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("L'en-tête de vin 'Conseil vin :' doit être consommé")
            .that(handled1)
            .isTrue()

        assertWithMessage("L'en-tête de vin doit devenir actif dans state")
            .that(state.isWineHeaderActive)
            .isTrue()

        assertWithMessage("Aucun vin ne doit être ajouté pour la ligne d'en-tête seule")
            .that(state.detectedWineList)
            .isEmpty()

        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Margaux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("Le nom de château sous en-tête doit être consommé")
            .that(handled2)
            .isTrue()

        assertWithMessage("Le vin 'Château Margaux' doit être ajouté à la liste")
            .that(state.detectedWineList)
            .contains("Château Margaux")

        assertWithMessage("La section vin ne doit pas encore être fermée sans volume")
            .that(state.isWineSectionClosed)
            .isFalse()

        val handled3 = Ocr3CategorizerWine.processWineLine(
            workingLine = "75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("La ligne de volume '75 cl' doit fermer la section vin")
            .that(handled3)
            .isTrue()

        assertWithMessage("L'état de la section vin doit passer à fermée")
            .that(state.isWineSectionClosed)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_NonExplicitWineHeaderElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertWithMessage("La ligne d'ingrédient '200 g de farine' ne doit pas être traitée par la section vin")
            .that(handled)
            .isFalse()

        assertWithMessage("L'en-tête de vin ne doit pas devenir actif")
            .that(state.isWineHeaderActive)
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ActiveHeaderWhenWineSectionIsClosedElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true
        state.isWineSectionClosed = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Sommelier : Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Une ligne de vin reçue alors que la section vin est déjà fermée doit être rejetée")
            .that(handled)
            .isTrue()

        assertWithMessage("Aucun vin supplémentaire ne doit être ajouté à la liste")
            .that(state.detectedWineList)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_NonWineLineReturnsFalseElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Faire cuire au four",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Une consigne de cuisson 'Faire cuire au four' ne doit pas être traitée par la section vin")
            .that(handled)
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ExhaustiveWineHeadersAndVintages() {
        val wineHeaders = listOf(
            "Conseil vin : Pinot Noir 75cl",
            "Vin conseillé : Sauvignon 75cl",
            "Accord vin : Chardonnay 75cl",
            "L'accord met/vin : Bordeaux 2010",
            "Sommelier : Merlot Carrefour 2015"
        )
        wineHeaders.forEach { wineLine ->
            val state = Ocr3CategorizerWine.WineState()
            val handled = Ocr3CategorizerWine.processWineLine(
                workingLine = wineLine,
                currentSection = Ocr3Categorizer.SECTION_NONE,
                isIngredientListClosed = false,
                containsAction = false,
                startsWithConnector = false,
                vocabulary = vocabulary,
                wineState = state
            )
            assertWithMessage("La ligne d'en-tête de vin '$wineLine' doit être traitée avec succès")
                .that(handled)
                .isTrue()

            assertWithMessage("Le vin issu de '$wineLine' doit être ajouté à la liste des vins")
                .that(state.detectedWineList)
                .isNotEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_CapturesValidWineLineAndUpdatesWineState() {
        val state = Ocr3CategorizerWine.WineState()
        val line = "Sauvignon de Touraine 1995, 75 cl"

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = line,
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("La ligne de vin valide 'Sauvignon de Touraine 1995, 75 cl' doit être traitée")
            .that(handled)
            .isTrue()

        assertWithMessage("Le vin doit être conservé dans la liste des vins détectés")
            .that(state.detectedWineList)
            .contains("Sauvignon de Touraine 1995, 75 cl")

        assertWithMessage("La section vin doit être marquée comme fermée après détection de volume")
            .that(state.isWineSectionClosed)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_RejectsLineContainingCulinaryActionVerb() {
        val state = Ocr3CategorizerWine.WineState()
        val line = "Mélangez le vin avec la sauce"

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = line,
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Une ligne avec un verbe d'action culinaire 'Mélangez' ne doit pas être traitée comme vin")
            .that(handled)
            .isFalse()

        assertWithMessage("Aucun vin ne doit être ajouté")
            .that(state.detectedWineList)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_CapturesMultiLineWineWithAppellations() {
        val state = Ocr3CategorizerWine.WineState()

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Atinium, Bordeaux Graves",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "MO, 1996, 75 cl",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("La première ligne de vin 'Atinium, Bordeaux Graves' doit être traitée")
            .that(handled1)
            .isTrue()

        assertWithMessage("La seconde ligne de vin 'MO, 1996, 75 cl' doit être traitée")
            .that(handled2)
            .isTrue()

        assertWithMessage("La première ligne du vin multiligne doit être conservée")
            .that(state.detectedWineList)
            .contains("Atinium, Bordeaux Graves")

        assertWithMessage("La seconde ligne du vin multiligne doit être conservée")
            .that(state.detectedWineList)
            .contains("M.O., 1996, 75 cl")

        assertWithMessage("La section vin doit être fermée après la ligne contenant le volume '75 cl'")
            .that(state.isWineSectionClosed)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DiscardsNoiseLinesAfterWineSectionIsClosed() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true
        state.isWineSectionClosed = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "ATINUM",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertWithMessage("Une ligne de bruit reçue après fermeture de la section vin doit être ignorée")
            .that(handled)
            .isTrue()

        assertWithMessage("Aucun vin supplémentaire ne doit être ajouté")
            .that(state.detectedWineList)
            .isEmpty()
    }
}
