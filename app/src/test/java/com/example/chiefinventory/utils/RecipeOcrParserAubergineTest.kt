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
        // Mockage exhaustif des ressources
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
        // On utilise ici le texte OCR EXACT renvoyé par l'émulateur
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

        val result = RecipeOcrParser.parse(ocrText, res)

        // 1. Titre (doit être null)
        assertNull("Le titre doit être null", result.title)
        
        // 2. Vin
        val wine = result.wine ?: ""
        assertNotEquals("Le champ vin ne doit pas être vide", "", wine.trim())
        assertTrue("Vin contient Merlot", wine.contains("Merlot", ignoreCase = true))

        // 3. Source (Vérification de la robustesse Hôtel)
        val source = result.source ?: ""
        assertTrue("Source contient Hôtel", source.contains("Hôtel", ignoreCase = true))
        assertTrue("Source contient chef", source.contains("Nahit YILMAZ"))
        assertTrue("Source contient tel", source.contains("02/542.42.42"))

        // 4. Ingrédients (Normalisation pour gérer le découpage OCR)
        val ing = result.ingredients ?: ""
        val ingNorm = ing.replace("\n", " ")
        
        assertTrue("Contient 6 aubergines moyennes", ingNorm.contains("6 aubergines moyennes"))
        assertTrue("Contient 1 ou 2 citrons", ingNorm.contains("1 ou 2 citrons"))
        assertTrue("Contient 150 g de beurre", ingNorm.contains("150 g de beurre"))
        assertTrue("Contient persil haché", ingNorm.contains("persil haché"))
        assertTrue("Contient 2 gousses d'ail", ingNorm.contains("2 gousses d'ail"))
        assertTrue("Contient sel, poivre", ingNorm.contains("sel, poivre"))

        // 5. Portions
        assertEquals("4", result.servings)
    }
}
