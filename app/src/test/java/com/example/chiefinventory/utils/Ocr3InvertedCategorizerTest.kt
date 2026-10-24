package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'aiguillage et la détection du Type 3 (Inversé).
 */
class Ocr3InvertedCategorizerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie que le détecteur identifie le scan de Chits de concombre comme une recette inversée (Type 3)
     * et retourne le titre factice "Recette avec instructions en premier" pour la phase 1.
     */
    @Test
    fun `isInvertedRecipe should return true and categorizeInvertedRecipe should return dummy title for Chits de concombre`() {
        val ocrText = """
            Chits de concombre au: eus de truite
            1) Découpez deux grands Çons de 1/2 cm dans le concombre.
            2) Pendant ce temps, préparez la farce.
            3) Farcissez les puits de concombre.
            4) Décorez les assiettes.
            POUR 4 PERSONNES
            1 grand concombre
            1 petit pot d'œufs de truite (50 g)
            250 g de fromage blanc
            1 c. à soupe de cerfeuil haché
            1 c. à soupe de ciboulette hachée
            1 c. à soupe d'aneth haché
            1 c. à soupe d'oignons frais hachés
            quelques feuilles de salade
            1 tomate
            sel et poivre
            20 mn
            BOURGOGNE CHARDONNAY Michel Laroche
        """.trimIndent()

        val isInv = Ocr3InvertedCategorizer.isInvertedRecipe(ocrText.lines(), vocabulary)
        System.err.println("DEBUG IS_INVERTED = " + Ocr3InvertedCategorizer.isInvertedRecipe(ocrText.lines(), vocabulary))
        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        System.err.println("DEBUG RESULT TITLE = " + result.title)

        // Validation de l'aiguillage du Type 3 et du titre réel extrait
        System.err.println("=== TEST INSTRUCTIONS RESULT ===\n" + result.instructions)
        assertThat(result.title).ignoringCase().contains("Puits de concombre aux œufs de truite")
        assertThat(result.ingredients).ignoringCase().contains("1 grand concombre")
        assertThat(result.instructions).ignoringCase().contains("Découpez deux grands")
    }

    /**
     * Vérifie que le détecteur renvoie false pour une recette classique de Type 1 (ingrédients en premier).
     */
    @Test
    fun `isInvertedRecipe should return false for classic Type 1 recipe`() {
        val lines = listOf(
            "INGRÉDIENTS",
            "200 g de farine",
            "100 g de beurre",
            "INSTRUCTIONS",
            "Mélanger la farine."
        )

        assertThat(Ocr3InvertedCategorizer.isInvertedRecipe(lines, vocabulary)).isFalse()
    }
}
