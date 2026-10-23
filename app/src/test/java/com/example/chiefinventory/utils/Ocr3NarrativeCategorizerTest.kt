package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la séparation stricte entre recettes de Type 1 (structurées) et Type 2 (narratives).
 */
class Ocr3NarrativeCategorizerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie que le détecteur identifie correctement une recette narrative (Type 2) sans en-tête d'ingrédients.
     */
    @Test
    fun `isNarrativeInstructionOnlyRecipe should return true for short instruction-only recipe`() {
        val lines = listOf(
            "1 Faites revenir 1 oignon haché, 3 carottes en rondelles et 1 càs de gingembre râpé dans 1 càs d'huile.",
            "2 Ajoutez-y 50 cl de bouillon de légumes, portez à ébullition.",
            "3 Mixez en velouté fin."
        )

        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)).isTrue()
    }

    /**
     * Vérifie que le détecteur identifie le cas réel Soupe glacée à la carotte comme une recette de Type 2.
     */
    @Test
    fun `isNarrativeInstructionOnlyRecipe should return true for real Soupe glacee a la carotte scan`() {
        val lines = listOf(
            "Bon mardhế",
            "Préparation: 10 mn",
            "Cuisson: 25 mn",
            "Repos: 3 h",
            "1 Faites revenir 1 oignon haché,",
            "3 carottes en rondelles et 1 càs",
            "de gingembre râpé dans 1 càs",
            "d'huile, 5 mn, à feu moyen, le",
            "temps que l'oignon devienne",
            "transparent.",
            "2 Ajoutez-y 50 cl de bouillon",
            "de légumes, portez à ébullition",
            "et poursuivez la cuisson, 15 mn,",
            "à feu moyen.",
            "3 Mixez en velouté fin. Laissez",
            "refroidir puis réservez au frigo,",
            "3h."
        )

        val isNarrative = Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
        println("DEBUG: isNarrative = $isNarrative")
        assertThat(isNarrative).isTrue()
    }

    /**
     * Vérifie que le détecteur renvoie false pour une recette classique (Type 1) avec section Ingrédients explicite.
     */
    @Test
    fun `isNarrativeInstructionOnlyRecipe should return false for structured classic recipe with ingredient header`() {
        val lines = listOf(
            "INGRÉDIENTS",
            "200 g de farine",
            "100 g de beurre",
            "INSTRUCTIONS",
            "Mélanger la farine."
        )

        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)).isFalse()
    }

    /**
     * Vérifie que le détecteur renvoie false pour une recette classique (Type 1) sans en-tête mais avec une liste d'ingrédients autonomes avec quantités.
     */
    @Test
    fun `isNarrativeInstructionOnlyRecipe should return false for structured classic recipe with standalone ingredient lines`() {
        val lines = listOf(
            "200 g de farine",
            "100 g de beurre",
            "Faites fondre le beurre"
        )

        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)).isFalse()
    }

    /**
     * Vérifie que le filtre isolateur de durées de temps rejette "5 mn", "3 h", "15 minutes" et "200°C".
     */
    @Test
    fun `isDurationOrTime should return true for time durations and temperature`() {
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("5 mn", "mn", "")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("3 h.", "h", "")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("15 minutes à feu moyen", "minutes", "")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("200°C", null, "")).isTrue()

        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("250 g de farine", "g", "farine")).isFalse()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("50 cl de bouillon", "cl", "bouillon")).isFalse()
    }

    /**
     * Vérifie l'extraction intégrale des consignes fluides et des 5 ingrédients implicites sur le scan réel de Soupe glacée à la carotte.
     */
    @Test
    fun `categorizeNarrativeRecipe should preserve complete instructions and extract implied ingredients for real Soupe glacee recipe`() {
        val rawOcr = """
            Soupe glacée
            à la carotte
            Bon mardhế
            Préparation: 10 mn
            Cuisson: 25 mn
            Repos: 3 h
            1 Faites revenir 1 oignon haché,
            3 carottes en rondelles et 1 càs
            de gingembre râpé dans 1 càs
            d'huile, 5 mn, à feu moyen, le
            temps que l'oignon devienne
            transparent.
            2 Ajoutez-y 50 cl de bouillon
            de légumes, portez à ébullition
            et poursuivez la cuisson, 15 mn,
            à feu moyen.
            3 Mixez en velouté fin. Laissez
            refroidir puis réservez au frigo,
            3h.
            A savoir
            L'ajout de gingembre permet
            de se passer le sel car il est un
            superbe exhausteur de goût.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcr, vocabulary)

        // 1. Validation de l'extraction des 5 ingrédients implicites pour l'indexation du garde-manger
        assertThat(result.ingredients).contains("oignon")
        assertThat(result.ingredients).contains("carottes")
        assertThat(result.ingredients).contains("gingembre")
        assertThat(result.ingredients).contains("huile")
        assertThat(result.ingredients).contains("bouillon")

        // 2. Validation que les consignes d'instructions sont conservées intégrales, fluides et non trouées
        assertThat(result.instructions).contains("Faites revenir 1 oignon haché")
        assertThat(result.instructions).contains("3 carottes en rondelles")
        assertThat(result.instructions).contains("gingembre râpé")
    }

    /**
     * Vérifie l'extraction des ingrédients quantifiés ET des assaisonnements non quantifiés (sel et poivre) sur le scan réel de Soupe glacée au concombre.
     */
    @Test
    fun `categorizeNarrativeRecipe should extract unquantified sel et poivre for real Soupe glacee au concombre scan`() {
        val rawOcr = """
            Soupe glacée
            au concombre
            1 Versez 1 concombre en
            Préparation: 10 mn morceaux, 250 g de yaourt au
            Bon marché
            lait de brebis bien froid, 2 c. à soupe
            de menthe fraiche hachée, 1
            gousse d'ail pelée, sel et poivre
            dans le bol d'un robot ménager.
            2 Mixez en velouté lisse.
            3 Servez dans des bols glacés et
            garnissez de feuilles de menthe.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcr, vocabulary)

        // 1. Validation de l'extraction des ingrédients quantifiés ET de sel et poivre
        assertThat(result.ingredients).contains("concombre")
        assertThat(result.ingredients).contains("yaourt")
        assertThat(result.ingredients).contains("menthe")
        assertThat(result.ingredients).contains("ail")
        assertThat(result.ingredients).contains("Sel et poivre")

        // 2. Validation des instructions fluides
        assertThat(result.instructions).contains("Versez 1 concombre")
    }
}
