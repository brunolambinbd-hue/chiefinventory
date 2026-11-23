package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que la ligne d'instruction césurée 'nappez les lan-'
 * verrouille la ligne suivante 'goustines d'un peu de sauce au curry.' dans les instructions
 * et qu'aucun faux ingrédient n'est créé.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrLangoustineNappezHyphenTest {

    private lateinit var context: Context
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipeOcrParser_NappezLanGoustinesHyphenation_LocksInInstructionsAndFusesFluidly() {
        val rawLines = listOf(
            "Ingrédients pour 4 personnes",
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature",
            "4 belles feuilles de salade",
            "cresson, sel et poivre",
            "Salade de langoustines au curry",
            "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et",
            "un trait de jus de citron. Salez et poivrez; réservez.",
            "Décortiquez les langoustines. Réservez 4 belles têtes.",
            "Pelez la mangue, taillez-la en fines lamelles. Lavez et essorez la salade; lavez le cresson.",
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
            "Décorez de salade et de cresson, ajoutez les croûtons.",
            "Disposez une tête de langoustine sur chaque assiette, nappez les lan-",
            "goustines d'un peu de sauce au curry.",
            "Servez frais, en présentant le restant de la sauce à part.",
            "Vous pouvez préparer une soupe avec les têtes et les carcasses des langoustines."
        )

        val result = RecipeOcrParser.parse(rawLines, vocabulary, offTaxonomyList = listOf("langoustines", "mangue", "mayonnaise", "curry", "citron", "yaourt", "salade", "cresson", "sauce au curry"))

        assertWithMessage("La suite de la césure 'goustines d'un peu de sauce au curry.' ne doit PAS être volée dans la liste des ingrédients")
            .that(result.ingredients)
            .doesNotContain("Goustines")

        assertWithMessage("L'instruction césurée 'nappez les lan-' + 'goustines...' doit être fusionnée sans coupure dans les instructions")
            .that(result.instructions)
            .contains("Disposez une tête de langoustine sur chaque assiette, nappez les langoustines d'un peu de sauce au curry.")
    }
}
