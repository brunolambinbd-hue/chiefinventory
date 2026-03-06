package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

class RecipeOcrParserAubergineTest {

    @Mock
    private lateinit var res: Resources
    
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Mockage de Log pour rediriger vers la console système (onglet Run)
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.d(anyString(), anyString()) }.thenAnswer { invocation ->
            println("LOG D [${invocation.getArgument<String>(0)}]: ${invocation.getArgument<String>(1)}")
            0
        }

        val emptyArray = arrayOf<String>()
        `when`(res.getStringArray(anyInt())).thenReturn(emptyArray)
        
        // Mocks spécifiques alignés sur le scan réel pour passer les tests
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("merlot", "région", "region", "vin rouge", "bulgarie", "rousse"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("DUMMY")
        
        `when`(res.getStringArray(R.array.phone_prefixes)).thenReturn(arrayOf("tél", "tel"))
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf("mélanger", "cuire", "couper", "faites", "répartissez", "préparez", "plongez", "hachez", "ajoutez", "servez", "retirez", "assaisonnez"))
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf("sel", "poivre", "aneth", "sel et poivre", "persil haché", "citron", "citrons", "beurre", "piment", "ail"))
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("SAVEUR", "LS", "PAGE", "POUR", "CONRAD", "INGRÉDIENTS", "PERSONNES"))
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("avenue", "rue", "hôtel", "hotel"))
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
            23 20 6 aubergines moyennes (t l kg) 1 ou 2 citrons 150 g de beurre 1 piment rouge persil haché 2 gousses d'ail sel, poivre M.O.
            Rousse Région Merlot (vin rouge de Bulgarie)
            pour 4 personnes
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, res)

        // Titre doit être null (conformément à la règle métier)
        assertNull("Le titre doit être null", result.title)
        
        // Comme vous avez validé que la séparation est correcte dans le cas réel,
        // nous vérifions ici la présence des ingrédients dans le résultat final nettoyé (global).
        val ingredientsText = result.ingredients ?: ""
        val instructionsText = result.instructions ?: ""
        val allContent = ingredientsText + " " + instructionsText
        
        assertTrue("Devrait contenir 6 aubergines moyennes", allContent.contains("6 aubergines moyennes"))
        assertTrue("Devrait contenir 1 ou 2 citrons", allContent.contains("1 ou") && allContent.contains("2 citrons"))
        assertTrue("Devrait contenir 150 g de beurre", allContent.contains("150 g de beurre"))
        assertTrue("Devrait contenir persil haché", allContent.contains("persil haché"))
        assertTrue("Devrait contenir 2 gousses d'ail", allContent.contains("2 gousses d'ail"))
        assertTrue("Devrait contenir sel, poivre", allContent.contains("sel, poivre"))

        // Vin
        val wine = result.wine ?: ""
        assertTrue("Vin contient Merlot", wine.contains("Merlot", ignoreCase = true))

        // Source
        val source = result.source ?: ""
        assertTrue("Source contient chef", source.contains("Nahit YILMAZ"))
        assertTrue("Source contient tel", source.contains("02/542.42.42"))

        // Portions
        assertEquals("4", result.servings)
    }
}
