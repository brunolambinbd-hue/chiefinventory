package com.example.chiefinventory.utils

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

class RecipeOcrParserAubergineTest {

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
    fun `test first scenario salade aubergine Conrad from screen`() {
        val ocrText = """
            CONRAD
            BR
            LS
            Nahit YILMAZ
            Hôtel Conrad International
            avenue Louise 71
            1050 BRUXELLES
            Tél. : 02/542.42.42
            e
            175
            saveur de
            alade d'aubergines
            Plongez les aubergines dans l'eau bouillante et retirez la peau, Hachez la chair et faites-la suer dans
            un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et
            le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.
            23
            20
            Ingrédients pour 4 personnes
            6 aubergines moyennes (t l kg)
            1 ou 2 citrons
            150 g de beurre
            I piment rouge
            persil haché
            2 gousses d'ail
            sel, poivre
            Pour la décoration: tomates
            Notre vin conseillé
            Rousse Région Merlot
            (vin rouge de Bulgarie)
            M.O.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // Test uniquement du vin et de la source sur la recette aubergines
        assertThat(result.wine).ignoringCase().contains("Merlot")
        assertThat(result.source).ignoringCase().contains("Hôtel")
        assertThat(result.source).contains("Nahit YILMAZ")
        assertThat(result.source).contains("02/542.42.42")

        val ingNorm = (result.ingredients ?: "").replace("\n", " ")
        assertThat(ingNorm).ignoringCase().contains("aubergines moyennes")
        assertThat(ingNorm).contains("1 ou 2 citrons")
        assertThat(ingNorm).contains("150 g de beurre")
        assertThat(ingNorm).ignoringCase().contains("persil haché")
        assertThat(ingNorm).contains("2 gousses d'ail")

        assertThat(result.servings).isEqualTo("4")
    }
}
