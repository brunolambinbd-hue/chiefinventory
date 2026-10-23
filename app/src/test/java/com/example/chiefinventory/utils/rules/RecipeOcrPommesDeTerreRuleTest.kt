package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrPommesDeTerreRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse pommes de terre farcies recipe should repair Donmes to Pommes and berbes to herbes`() {
        val ocrText = """
            Donmes de terre farcies
            aux radis et aux berbes
            INGRÉDIENTS
            4 nouvelles pommes de terre
            4 noix de beurre
            8 c. à soupe de crème fouettée
            4 radis
            2 gousses d'ail
            1 c. à soupe de persil haché
            1 c. à soupe de ciboulette hachée
            1 c. à soupe de cerfeuil haché
            1 c. à soupe d'estragon haché
            Cressonnette
            sel et poivre
            40 mn
            INSTRUCTIONS
            Faites cuire les pommes de terre en robe des champs.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // 1. Validation de la réparation orthographique de Donmes -> Pommes et berbes -> herbes
        assertThat(result.title).ignoringCase().contains("Pommes de terre farcies aux radis et aux herbes")

        // 2. Validation du temps
        assertThat(result.prepTime).isEqualTo("40")
    }

    @Test
    fun `parse pommes de terre farcies recipe should fluidly merge et hyphenated instruction lines`() {
        val ocrText = """
            Pommes de terre farcies aux radis et aux herbes
            INGRÉDIENTS
            4 nouvelles pommes de terre
            INSTRUCTIONS
            •Pendant ce temps, hachez fin
            les radis et les gousses d'ail et-
            mélangez-les à la crème
            fouettée et aux herbes.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        val inst = result.instructions ?: ""

        assertThat(inst).contains("hachez fin les radis et les gousses d'ail et mélangez-les à la crème")
    }
}
