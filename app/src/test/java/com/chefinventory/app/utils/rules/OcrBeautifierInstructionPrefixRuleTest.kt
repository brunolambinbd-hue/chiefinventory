package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class OcrBeautifierInstructionPrefixRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should strip decorative prefixes like De 1 before imperative action verb in instructions`() {
        val ocrText = """
            Salade d'aubergines
            INGRÉDIENTS
            6 aubergines
            sel et poivre
            De 1 Plongez les aubergines dans l'eau bouillante et retirez la peau.
            Hachez la chair et faites-la suer dans un peu de beurre.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        assertWithMessage("Les instructions doivent démarrer par 'Plongez les aubergines'")
            .that(inst)
            .startsWith("Plongez les aubergines")

        assertWithMessage("Le préfixe parasite 'De 1 Plongez' doit être effacé")
            .that(inst)
            .doesNotContain("De 1 Plongez")
    }

    @Test
    fun `parse recipe should strip preposition variations like Du Des Le La En Pour and digit variations like 2 3 10 before action verbs`() {
        val ocrText = """
            Salade de surimi
            INGRÉDIENTS
            120 g de surimi
            sel et poivre
            Du 2 Hachez finement le surimi ou le crabe.
            Des 3 Mélangez avec la mayonnaise.
            Le 10 Disposez un peu de salade.
            e 1 Servez avec les toasts.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        assertWithMessage("La consigne nettoyée doit contenir 'Hachez finement le surimi'")
            .that(inst)
            .contains("Hachez finement le surimi")

        assertWithMessage("La consigne nettoyée doit contenir 'Mélangez avec la mayonnaise'")
            .that(inst)
            .contains("Mélangez avec la mayonnaise")

        assertWithMessage("La consigne nettoyée doit contenir 'Disposez un peu de salade'")
            .that(inst)
            .contains("Disposez un peu de salade")

        assertWithMessage("La consigne nettoyée doit contenir 'Servez avec les toasts'")
            .that(inst)
            .contains("Servez avec les toasts")

        assertWithMessage("Le préfixe parasite 'Du 2 Hachez' doit être éliminé")
            .that(inst)
            .doesNotContain("Du 2 Hachez")

        assertWithMessage("Le préfixe parasite 'Des 3 Mélangez' doit être éliminé")
            .that(inst)
            .doesNotContain("Des 3 Mélangez")

        assertWithMessage("Le préfixe parasite 'Le 10 Disposez' doit être éliminé")
            .that(inst)
            .doesNotContain("Le 10 Disposez")

        assertWithMessage("Le préfixe parasite 'e 1 Servez' doit être éliminé")
            .that(inst)
            .doesNotContain("e 1 Servez")
    }

    @Test
    fun `beautifyInstructions should filter out standalone trash words like Les at the end of instructions`() {
        val ocrText = """
            Cocktail de crevettes
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            Décorez éventuellement de quelques brins de ciboulette.
            Les
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        assertWithMessage("La consigne valide 'Décorez éventuellement de quelques brins de ciboulette.' doit être conservée")
            .that(inst)
            .contains("Décorez éventuellement de quelques brins de ciboulette.")

        assertWithMessage("L'article orphelin de fin de page 'Les.' ne doit pas figurer")
            .that(inst)
            .doesNotContain("Les.")

        assertWithMessage("L'article orphelin 'Les' ne doit pas figurer")
            .that(inst)
            .doesNotContain("Les")
    }

    @Test
    fun `beautifyInstructions should filter out corrupted recipe header noise lines like Ego1928 eu RECETTES`() {
        val ocrText = """
            Cocktail de crevettes
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            Əgo1928 eu) RECETTES
            Décorez éventuellement de quelques brins de ciboulette.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        assertWithMessage("La consigne valide 'Décorez éventuellement de quelques brins de ciboulette.' doit être conservée")
            .that(inst)
            .contains("Décorez éventuellement de quelques brins de ciboulette.")

        assertWithMessage("Le bruit de numérisation 'Əgo1928' doit être éliminé")
            .that(inst)
            .doesNotContain("Əgo1928")

        assertWithMessage("Le mot-clé d'en-tête 'RECETTES' doit être éliminé")
            .that(inst)
            .doesNotContain("RECETTES")
    }
}
