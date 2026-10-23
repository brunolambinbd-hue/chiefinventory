package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrOrphanModifierRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe with orphan modifier fume alone on line should merge with previous ingredient`() {
        val ocrText = """
            SALADE DE SAUMON
            INGRÉDIENTS
            200 g de saumon
            fumé
            1 oignon
            haché
            2 gousses d'ail
            hachées
            persil
            hachés
            INSTRUCTIONS
            Disposer le saumon et servir.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        assertThat(ing).contains("200 g de saumon fumé")
        assertThat(ing).contains("1 oignon haché")
        assertThat(ing).contains("2 gousses d'ail hachées")
        assertThat(ing).contains("Persil hachés")
    }

    @Test
    fun `parse recipe with feminine and plural orphan modifiers should merge correctly with previous line`() {
        val ocrText = """
            SALADE COMPOSÉE
            INGRÉDIENTS
            1 truite
            fumée
            4 tranches de truite
            fumées
            100 g de poissons
            fumés
            2 pommes
            émincées
            1 gousse d'ail
            pelée
            10 olives
            dénoyautées
            INSTRUCTIONS
            Disposer et servir.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        assertThat(ing).contains("1 truite fumée")
        assertThat(ing).contains("4 tranches de truite fumées")
        assertThat(ing).contains("100 g de poissons fumés")
        assertThat(ing).contains("2 pommes émincées")
        assertThat(ing).contains("1 gousse d'ail pelée")
        assertThat(ing).contains("10 olives dénoyautées")
    }
}
