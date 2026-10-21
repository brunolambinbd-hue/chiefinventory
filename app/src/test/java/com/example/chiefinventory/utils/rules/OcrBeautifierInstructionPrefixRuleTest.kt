package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
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

        // Validation : "De 1 Plongez..." doit être nettoyé en "Plongez..."
        assertThat(inst).startsWith("Plongez les aubergines")
        assertThat(inst).doesNotContain("De 1 Plongez")
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

        assertThat(inst).contains("Hachez finement le surimi")
        assertThat(inst).contains("Mélangez avec la mayonnaise")
        assertThat(inst).contains("Disposez un peu de salade")
        assertThat(inst).contains("Servez avec les toasts")

        assertThat(inst).doesNotContain("Du 2 Hachez")
        assertThat(inst).doesNotContain("Des 3 Mélangez")
        assertThat(inst).doesNotContain("Le 10 Disposez")
        assertThat(inst).doesNotContain("e 1 Servez")
    }
}
