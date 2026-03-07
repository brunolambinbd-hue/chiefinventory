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

class RecipeOcrParserOceaneTest {

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
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("touraine", "sauvignon", "blanc"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("DUMMY")
        
        // Mocks pour Ingrédients
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf(
            "mayonnaise", "sel", "poivre", "tabasco", "orange", "chicon", "ketchup", 
            "yaourt nature", "décortiquées", "grosses", "frisée", "océane"
        ))
        
        // Mocks pour Instructions
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf(
            "mélangez", "servez", "coupez", "ajoutez", "prélevez", "lavez", "répartissez", "garnissez"
        ))
        
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("INGRÉDIENTS", "PERSONNES", "POUR"))
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("hôtel", "place", "avenue", "rue"))
    }

    @After
    fun tearDown() {
        mockedLog.close()
    }

    @Test
    fun `test scenario salade oceane with real ocr data`() {
        // Texte OCR EXACT extrait des logs de l'émulateur
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
            yalade océane
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

        val result = RecipeOcrParser.parse(ocrText, res)

        // 1. Titre (doit être null)
        assertNull("Le titre doit être null", result.title)
        
        // 2. Portions
        assertEquals("4", result.servings)

        // 3. Ingrédients
        val ing = result.ingredients ?: ""
        val ingNorm = ing.replace(Regex("\\s+"), " ")
        
        assertTrue("Fusion crevettes réussie", ingNorm.contains("250 g de crevettes grises décortiquées"))
        assertTrue("Fusion mayonnaise réussie", ingNorm.contains("2 c. à soupe de mayonnaise"))
        assertTrue("Fusion yaourt réussie", ingNorm.contains("250 g de yaourt nature"))
        
        // Correction : "l" minuscule OCR devient "1" via IngredientParser.preClean
        assertTrue("Contient chicon. Reçu: '$ingNorm'", ingNorm.contains("1 beau chicon"))
        // "l orange" devient "1 orange"
        assertTrue("Contient orange", ingNorm.contains("1 orange"))
        // "I12" devient "1/2"
        assertTrue("Contient frisée", ingNorm.contains("1/2 salade frisée"))

        assertTrue("Contient sel et poivre", ingNorm.contains("sel et poivre"))

        // 4. Vin
        val wine = result.wine ?: ""
        assertTrue("Vin contient Touraine", wine.contains("Touraine", ignoreCase = true))
        assertTrue("Vin contient Sauvignon", wine.contains("Sauvignon", ignoreCase = true))

        // 5. Instructions
        val inst = result.instructions ?: ""
        assertTrue("Phrase zeste continue", inst.contains("zeste de l'orange"))
        assertTrue("Contient Mélangez", inst.contains("Mélangez"))
    }
}
