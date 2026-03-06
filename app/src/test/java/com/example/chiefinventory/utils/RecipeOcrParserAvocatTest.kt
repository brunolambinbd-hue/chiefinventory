package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class RecipeOcrParserAvocatTest {

    @Mock
    private lateinit var res: Resources
    
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.d(anyString(), anyString()) }.thenAnswer { invocation ->
            println("LOG D [${invocation.getArgument<String>(0)}]: ${invocation.getArgument<String>(1)}")
            0
        }

        val emptyArray = arrayOf<String>()
        `when`(res.getStringArray(anyInt())).thenReturn(emptyArray)
        
        // Mocks pour le Vin
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("chardonnay", "domaine"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("DUMMY")
        
        // CRUCIAL : On ajoute la puce '•' dans les ingrédients pour qu'elle ne soit pas prise pour le titre
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf("sel", "poivre", "aneth", "sel et poivre", "•"))
        
        // Mocks pour Instructions
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf("faites", "répartissez", "préparez", "coupez", "hachez", "mélanger", "cuire"))
        
        // On ne met QUE le vrai bruit ici (pas de verbes, pas d'aliments)
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("INGRÉDIENTS", "PERSONNES"))
        
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("hôtel", "place", "avenue", "rue"))
        `when`(res.getStringArray(R.array.phone_prefixes)).thenReturn(arrayOf("tél", "tel"))
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

        val result = RecipeOcrParser.parse(ocrText, res)

        // 1. Titre (doit être null)
        assertNull("Le titre doit être null", result.title)
        
        // 2. Ingrédients
        val ing = result.ingredients ?: ""
        assertTrue("Correction | -> 1 avocat. Reçu: '$ing'", ing.contains("1 avocat"))
        assertTrue("Contient haricots princesses", ing.contains("200 g de haricots princesses"))
        
        // Assertion flexible pour sel et poivre
        val containsSelPoivre = ing.contains("sel et poivre") || (ing.contains("sel et") && ing.contains("poivre"))
        assertTrue("Contient sel et poivre. Reçu: '$ing'", containsSelPoivre)
        assertTrue("Devrait contenir aneth", ing.contains("aneth"))

        // 3. Instructions
        val inst = result.instructions ?: ""
        assertTrue("Instruction - Faites cuire", inst.contains("Faites cuire"))
        assertTrue("Instruction - Coupez", inst.contains("Coupez"))
        assertTrue("Vinaigrette complète fusionnée", inst.contains("Préparez une vinaigrette avec l'huile d'olive"))
        assertTrue("Instruction - Répartissez", inst.contains("Répartissez"))

        // 4. Source
        assertTrue("Source contient Hilton", result.source?.contains("Hilton", ignoreCase = true) == true)

        // 5. Vin
        assertTrue("Vin contient Chardonnay", result.wine?.contains("Chardonnay", ignoreCase = true) == true)

        // 6. Portions
        assertEquals("4", result.servings)
    }
}
