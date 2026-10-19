package com.example.chiefinventory.utils

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

class RecipeOcrParserAvocatTest {

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
    fun `test second scenario salade avocat from screen`() {
        val ocrText = """
            INGRÉDIENTS
            | avocat
            200 g de haricots princesses
            200 g de saumon cru
            aneth
            6 c. à soupe d'huile d'olive
            le jus de 1/2 citron
            lc. à café de miel
            lc. à soupe de vinaigre
            sel et poivre alade d'avocat et princesses
            Faites cuire les haricots princesses 10 à 15 minutes jusqu'à ce que vous puissiez y piquer facilement une fourchette. Retirez le jus de cuisson et passez
            les haricots sous un jet d'eau froide.
            •Coupez les haricots en deux.
            •Préparez une vinaigrette avec
            T'huile d'olive, le jus de citron, le miel, le vinaigre, du sel et du poivre.
            •Répartissez la salade sur 4 assiettes.
            Chardonnay Domaine des Roches
            Hôtel Hilton, Place Rogier 20, 1000 BRUXELLES
            pour 4 personnes
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // Test des ingrédients, instructions, titre et vin pour la recette avocat
        assertThat(result.title).isEqualTo("Salade d'avocat et princesses")

        val ing = result.ingredients ?: ""
        assertThat(ing).contains("1 avocat")
        assertThat(ing).contains("200 g de haricots princesses")
        assertThat(ing).ignoringCase().contains("aneth")

        val inst = result.instructions ?: ""
        assertThat(inst).contains("Faites cuire")
        assertThat(inst).contains("Coupez")
        assertThat(inst).contains("Répartissez")

        assertThat(result.source).ignoringCase().contains("Hilton")
        assertThat(result.wine).ignoringCase().contains("Chardonnay")
        assertThat(result.servings).isEqualTo("4")
    }
}
