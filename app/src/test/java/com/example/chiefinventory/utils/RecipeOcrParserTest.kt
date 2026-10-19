package com.example.chiefinventory.utils

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

class RecipeOcrParserTest {

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
            Nahit YILMAZ
            HôConrad International, avenue Louise 71, 1050 BRUXELLES
            : 02/542.42.42
            175 alade d'aubergines
            Plongez les aubergines dans l'eau bouillante et retirez la peau, Hachez la chair et faites-la suer dans un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.
            Ingrédients pour 4 personnes
            23 20 6 aubergines moyennes (t l kg) 1 ou 2 citrons 150 g de beurre 1 piment rouge persil haché 2 gousses d'ail sel, poivre M.O.
            Rousse Région Merlot (vin rouge de Bulgarie)
            pour 4 personnes
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""
        val containsMerlotOrBulgarie = wine.contains("Merlot", ignoreCase = true) || wine.contains("Bulgarie", ignoreCase = true)
        assertWithMessage("Vin contient Merlot ou Bulgarie").that(containsMerlotOrBulgarie).isTrue()

        assertThat(result.servings).isEqualTo("4")

        val source = result.source ?: ""
        assertThat(source).contains("Nahit YILMAZ")

        val ing = result.ingredients ?: ""
        assertThat(ing).ignoringCase().contains("aubergines moyennes")
        assertThat(ing).contains("1 ou")
        assertThat(ing).contains("2 citrons")
        assertThat(ing).contains("150 g de beurre")
        assertThat(ing).ignoringCase().contains("persil haché")
        assertThat(ing).contains("2 gousses d'ail")

        val inst = result.instructions ?: ""
        assertThat(inst).contains("Plongez les aubergines")
        assertThat(inst).contains("Servez frais")
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
            • Répartissez la salade sur 4 assiettes.
            Chardonnay Domaine des Roches
            Hôtel Hilton, Place Rogier 20, 1000 BRUXELLES
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""
        assertThat(wine).ignoringCase().contains("Chardonnay")

        val source = result.source ?: ""
        assertThat(source).ignoringCase().contains("Hilton")

        val ingLower = (result.ingredients ?: "").lowercase()
        assertThat(ingLower).contains("avocat")
        assertThat(ingLower).contains("haricots princesses")
        assertThat(ingLower).contains("saumon cru")
        assertThat(ingLower).contains("aneth")
        assertThat(ingLower).contains("huile d'olive")
        assertThat(ingLower).contains("1/2 citron")
        assertThat(ingLower).contains("miel")
        assertThat(ingLower).contains("vinaigre")
        assertThat(ingLower).contains("sel et poivre")

        val inst = result.instructions ?: ""
        assertThat(inst).contains("Faites cuire")
        assertThat(inst).contains("Coupez")
        assertThat(inst).contains("Répartissez")
    }
}
