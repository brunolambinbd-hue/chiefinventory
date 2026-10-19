package com.example.chiefinventory.utils

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

class RecipeOcrParserOceaneTest {

    private lateinit var mockedLog: MockedStatic<Log>
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.d(anyString(), anyString()) }.thenAnswer { invocation ->
            println("LOG D [${invocation.getArgument<String>(0)}]: ${invocation.getArgument<String>(1)}")
            0
        }

        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        mockedLog.close()
    }

    @Test
    fun `test scenario salade oceane with real ocr data`() {
        val ocrText = """
            INGRÉDIENTS
            POUR
            4 PERSONNES
            250 g de crevettes
            grises décortiquées
            4 petites tomates
            (ou 2 grosses)
            l beau chicon
            I12 salade frisée
            l orange
            250 g de yaourt nature
            2 c. à soupe de
            mayonnaise
            2 c. à soupe de ketchup
            lc.à soupe de
            ciboulette hachée
            Tabasco
            sel et poivre
            • Prélevez finement le zeste de
            l'orange, taillez-le en fins bâton-
            nets que vous plongez 1 minute
            dans un fond d'eau bouillante.
            Rafraîchissez à l'eau froide et lais-
            sez bien égoutter.
            • Mélangez le yaourt, le ketchup,
            la mayonnaise et la ciboulette.
            Ajoutez du Tabasco selon vos
            goûts, salez et poivrez. Réservez.
            •Lavez et essorez la salade, ajou-
            tez-y le chicon nettoyé et émincé.
            Disposez sur de grandes assiettes
            et saupoudrez de zeste d'orange.
            •Coupez les tomates en huit,
            répartissez-les sur les assiettes et
            garnissez le centre de crevettes
            grises.
            • Servez frais (ne laissez pas trop
            longtemps en attente); présentez la
            sauce à part.
            VIN CONSEILLÉ
            Touraine,
            Sauvignon blanc, 1995, 75 cl
            TOURAINE
            AUVICNoN
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // Test des portions, ingrédients et instructions pour la recette océane
        assertThat(result.servings).isEqualTo("4")

        val ingNorm = (result.ingredients ?: "").replace(Regex("\\s+"), " ")
        assertThat(ingNorm).contains("250 g de crevettes grises décortiquées")
        assertThat(ingNorm).contains("250 g de yaourt nature")
        assertThat(ingNorm).contains("1 beau chicon")
        assertThat(ingNorm).contains("1 orange")
        assertThat(ingNorm).contains("1/2 salade frisée")
        assertThat(ingNorm).ignoringCase().contains("sel et poivre")

        assertThat(result.wine).ignoringCase().contains("Touraine")
        assertThat(result.wine).ignoringCase().contains("Sauvignon")

        val inst = result.instructions ?: ""
        assertThat(inst).contains("zeste de l'orange")
        assertThat(inst).contains("Mélangez")
    }
}
