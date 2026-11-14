package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression de niveau A (Pipeline Complet [RecipeOcrParser])
 * dédiée spécifiquement au scan réel de la recette des Nems croustillants au crabe.
 */
class RecipeOcrParserNemsAuCrabeTest {

    /** Capture brute réelle ML Kit du scan caméra (issue du Logcat smartphone). */
    private val rawOcrScanText = listOf(
        "Pour 12 pièces",
        "Préparation: 15 mn",
        "Cuisson: 10 mn +2 mn",
        "par fournée",
        "1 boîte de crabe (120 g,",
        "poids égoutté)",
        "200 g de légumes émincés",
        "pour wok (avec soja)",
        "6 feuilles de brick",
        "une petite pointe de cing",
        "épices chinoises",
        "1c. à soupe d'huile",
        "d'arachide",
        "1 euf",
        "graines de sésame",
        "sel et poivre",
        "Pour la sauce",
        "4c. à soupe de jus",
        "d'orange",
        "2 c. à soupe de sweet chili",
        "sauce",
        "1c.à soupe de coriandre",
        "fraîche hachée",
        "COmoustioue 18/05/2011",
        "ENTRÉES ET AMUSE-BOUCHE O",
        "NEMS CROUSTILLANTS",
        "AU CRABE",
        "PRÉPARATION",
        "•Faites revenir les légumes à feu doux dans une",
        "sauteuse contenant I'huile. Ajoutez les cinq épices,",
        "couvrez et laissez suer 10 mn à feu doux.",
        "• Retirez du feu, ajoutez le crabe bien égoutté, mélangez",
        "et rectifiez l'assaisonnement.",
        "•Coupez les feuilles de brick en deux. Prenez une demi-",
        "feuille et déposez un petite part de farce allongée, au",
        "centre, près du bord coupé. Repliez les pointes vers la",
        "farce et enroulez le tout. Mouillez un peu pour maintenir",
        "fermé. Préparez 12 nems de cette manière.",
        "Vous pouvez faire dorer les nems dans la friteuse",
        "(180°) ou alors au four (préchauffé sur th. 7-210):",
        "déposez-les sur la plaque du four recouverte de papier",
        "cuisson, badigeonnezles d'euf battu et saupoudrez-les",
        "de sésame. Comptez 10 mn de cuisson.",
        "• Servez chaud, avec la sauce (préparée en mélangeant",
        "simplement les ingrédients indiqués).",
        "CONSEIL VIN DE ROLAND GOHY",
        "Ll'accord sans faute: un rosé qui se révélera complémentaire aux notes aigres-douces des",
        "nems, permettra d'atténuer leffet piquant du piment et pourra aussi, éventuellement, fa-",
        "Voriser ('harmonie des couleurs. Un blanc pourra aussi être choisi en raison de la rigueur",
        "sans compromission de son union controversée et pourtant favorable aux deux parties. Le",
        "blanc de Marqués de Riscal, un rueda espagnol, très fin et élégant, qui saura simposer en",
        "gardant sa distinction naturelle. Marqués de Riscal, DO rueda 2010, Carrefour:7,49 €.",
        "(O Bon marché O Abordable",
        "Kanet",
        "Cher"
    ).joinToString("\n")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    private fun assertLinesMatch(actual: List<String>, expected: List<String>, description: String) {
        if (actual != expected) {
            val message = "$description :\n\n" +
                "${OcrTestStrings.HEADER_EXPECTED} (${expected.size} lines) :\n" +
                expected.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n") + "\n\n" +
                "${OcrTestStrings.HEADER_ACTUAL} (${actual.size} lines) :\n" +
                actual.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n")
            fail(message)
        }
    }

    @Test
    fun testNemsAuCrabeRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("NEMS CROUSTILLANTS AU CRABE")

        // 2. Validation des Méta-données de Temps et Coût
        assertWithMessage(OcrTestStrings.PREP_TIME_MISMATCH)
            .that(result.prepTime)
            .isEqualTo("15")

        assertWithMessage(OcrTestStrings.COOK_TIME_MISMATCH)
            .that(result.cookTime)
            .isEqualTo("10")

        assertWithMessage("Le niveau de coût 'Bon marché' doit être extrait comme le code '1'")
            .that(result.priceRating)
            .isEqualTo("1")

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .contains("Carrefour")

        // 3. Validation des Ingrédients Clés (Fusions Sweet Chili Sauce et Coriandre Fraîche Hachée)
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("L'ingrédient '2 c. à soupe de sweet chili sauce' doit être fusionné au complet")
            .that(ingredientLines)
            .contains("2 c. à soupe de sweet chili sauce")

        assertWithMessage("L'ingrédient de sauce '1 c. à soupe de coriandre fraîche hachée' doit être fusionné au complet")
            .that(ingredientLines)
            .contains("1 c. à soupe de coriandre fraîche hachée")
    }
}
