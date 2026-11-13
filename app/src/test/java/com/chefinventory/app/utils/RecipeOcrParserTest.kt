package com.chefinventory.app.utils

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeOcrParserTest {

    private lateinit var mockedLog: MockedStatic<Log>
    private lateinit var vocabulary: OcrVocabulary
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
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
    @CoveredRequirement("6.1.1.9")
    fun testParse_EmptyAndBlankText_ReturnsEmptyResult() {
        val emptyResult = RecipeOcrParser.parse("", vocabulary)
        assertThat(emptyResult.title).isNull()
        assertThat(emptyResult.ingredients.orEmpty()).isEmpty()

        val blankResult = RecipeOcrParser.parse("   \n\t  ", vocabulary)
        assertThat(blankResult.title).isNull()
        assertThat(blankResult.ingredients.orEmpty()).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_ResourcesOverloadAndDegradedNoiseOcr() {
        val rawOcr = """
            !!! ??? ### 12345
            Tarte aux Fraises
            Ingrédients pour 4 personnes : 250g de fraises, 100g de sucre, 1 pâte sablée. 450 kcal
            Préparation : Faire cuire la pâte à blanc pendant 15 minutes. Garnir avec les fraises.
            Difficulté : facile. Prix : bon marché
        """.trimIndent()

        // Test Resources overload
        val resultFromRes = RecipeOcrParser.parse(rawOcr, context.resources)
        assertThat(resultFromRes.title).isNotNull()
        assertThat(resultFromRes.servings).isEqualTo("4")
        assertThat(resultFromRes.kcalPerServing).isEqualTo("450")
        assertThat(resultFromRes.difficulty).isEqualTo("facile")
        assertThat(resultFromRes.priceRating).isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_LOW)

        // Test severely corrupted noise line
        val noiseOnlyOcr = "12345 !@#\$%^&*() ___ --"
        val noiseResult = RecipeOcrParser.parse(noiseOnlyOcr, vocabulary)
        assertThat(noiseResult.title).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_ScenarioAubergineConrad() {
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
    @CoveredRequirement("6.1.1.9")
    fun testParse_ScenarioAvocatHilton() {
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

        val inst = result.instructions ?: ""
        assertThat(inst).contains("Faites cuire")
        assertThat(inst).contains("Coupez")
        assertThat(inst).contains("Répartissez")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_NarrativeStyleRecipe() {
        val ocrText = """
            Gâteau au Chocolat Express
            Mélangez 200 g de chocolat noir fondu avec 100 g de beurre et 3 œufs. 
            Ajoutez 80 g de farine et 100 g de sucre.
            Faites cuire au four pendant 20 minutes à 180°C.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        assertThat(result.title).contains("Gâteau")
        assertThat(result.instructions).contains("cuire au four")
    }
}
