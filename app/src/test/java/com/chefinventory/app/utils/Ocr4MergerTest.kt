package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B dédiés à [Ocr4Merger].
 */
class Ocr4MergerTest {

    private val mockModifiers = listOf("haché", "hachée", "émincé", "émincée")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_HyphenatedAndHangingConnectors() {
        val input = listOf(
            "200 g de poivrons",
            "rouges-",
            "et jaunes"
        )
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertWithMessage("La liste d'ingrédients fusionnés ne doit pas être vide")
            .that(result)
            .isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_OrphanModifier() {
        val input = listOf(
            "1 oignon",
            "émincé"
        )
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertWithMessage("Le modificateur orphelin 'émincé' doit être fusionné avec '1 oignon'")
            .that(result)
            .containsExactly("1 oignon émincé")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_CustomConnectorsAndEmptyInput() {
        assertWithMessage("Une liste d'ingrédients vide doit retourner une liste vide")
            .that(Ocr4Merger.mergeIngredients(emptyList(), emptyList(), emptyList()))
            .isEmpty()

        val input = listOf("1 verre", "de lait")
        val result = Ocr4Merger.mergeIngredients(input, emptyList(), emptyList(), customConnectors = listOf("de"))
        assertWithMessage("Le connecteur personnalisé 'de' doit permettre de fusionner '1 verre de lait'")
            .that(result)
            .containsExactly("1 verre de lait")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_EmptyInputAndPunctuationSplits() {
        assertWithMessage("Une liste d'instructions vide doit retourner une liste vide")
            .that(Ocr4Merger.mergeInstructions(emptyList()))
            .isEmpty()

        val input = listOf("Préparez la pâte. Laissez reposer. Cuisez à 180°C.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertWithMessage("Le texte d'instructions composé de 3 phrases doit être découpé phrase par phrase")
            .that(result)
            .containsExactly(
                "Préparez la pâte.",
                "Laissez reposer.",
                "Cuisez à 180°C."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsHanging_And_StartsWithConnector_Branches() {
        assertWithMessage("L'article 'le' seul doit être reconnu comme suspendu")
            .that(Ocr4Merger.isHanging("le"))
            .isTrue()

        assertWithMessage("Une ligne avec un tiret terminal '1 citron (1/2 pour le jus -' doit être suspendue")
            .that(Ocr4Merger.isHanging("1 citron (1/2 pour le jus -"))
            .isTrue()

        assertWithMessage("La préposition '100 g de' doit être reconnue comme suspendue")
            .that(Ocr4Merger.isHanging("100 g de"))
            .isTrue()

        assertWithMessage("L'unité seule '2 cuillères' doit être reconnue comme suspendue")
            .that(Ocr4Merger.isHanging("2 cuillères"))
            .isTrue()

        assertWithMessage("Un ingrédient complet '1 pomme de terre' ne doit pas être suspendu")
            .that(Ocr4Merger.isHanging("1 pomme de terre"))
            .isFalse()

        assertWithMessage("La ligne 'de farine' doit être reconnue comme démarrant par un connecteur")
            .that(Ocr4Merger.startsWithConnector("de farine"))
            .isTrue()

        assertWithMessage("La ligne 'd'huile' doit être reconnue comme démarrant par un connecteur")
            .that(Ocr4Merger.startsWithConnector("d'huile"))
            .isTrue()

        assertWithMessage("La ligne 'farine' ne doit pas être reconnue comme démarrant par un connecteur")
            .that(Ocr4Merger.startsWithConnector("farine"))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_RangeConnectors() {
        val input = listOf("1 ou", "2 citrons")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertWithMessage("Le connecteur de plage '1 ou' doit fusionner avec '2 citrons'")
            .that(result)
            .containsExactly("1 ou 2 citrons")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_NarrativeBlockAndUnclosedParens() {
        val input1 = listOf("Plongez les aubergines", "dans l'eau bouillante")
        val result1 = Ocr4Merger.mergeInstructions(input1)
        assertWithMessage("Les lignes de consignes narratives sans point doivent être fusionnées")
            .that(result1)
            .containsExactly("Plongez les aubergines dans l'eau bouillante")

        val input2 = listOf("Laissez mariner au frigo (2 heures", "à 4°C).")
        val result2 = Ocr4Merger.mergeInstructions(input2)
        assertWithMessage("Une parenthèse non refermée doit forcer la fusion avec la ligne suivante")
            .that(result2)
            .containsExactly("Laissez mariner au frigo (2 heures à 4°C).")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_HyphenatedWordReconstruction() {
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertWithMessage("La césure 'pin-' + 'cée' doit être reconstruite en 'pincée'")
            .that(result)
            .containsExactly("une pincée de sel.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_LangoustinesHyphenatedWordReconstruction() {
        val input = listOf(
            "Vous poOuvez préparer une soupe avec les têtes et les carcasses des lan-",
            "goustines."
        )
        val result = Ocr4Merger.mergeInstructions(input)
        assertWithMessage("La césure 'lan-' + 'goustines.' doit être reconstruite en 'langoustines.'")
            .that(result)
            .containsExactly(
                "Vous poOuvez préparer une soupe avec les têtes et les carcasses des langoustines."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_TrailingArticleDesMerge() {
        val input = listOf(
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des",
            "lamelles de mangue. Décorez de salade et de cresson, ajoutez les",
            "croûtons."
        )
        val result = Ocr4Merger.mergeInstructions(input)
        assertWithMessage("Les articles 'des' et 'les' en fin de ligne doivent forcer la fusion")
            .that(result)
            .containsExactly(
                "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
                "Décorez de salade et de cresson, ajoutez les croûtons."
            )
    }
}
