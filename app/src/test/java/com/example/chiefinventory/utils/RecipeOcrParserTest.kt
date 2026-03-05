package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class RecipeOcrParserTest {

    @Mock
    private lateinit var res: Resources

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        `when`(res.getStringArray(anyInt())).thenReturn(arrayOf<String>())
        `when`(res.getString(anyInt())).thenReturn("")
        
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf("mélanger", "cuire", "couper", "faites", "répartissez", "préparez"))
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf("sel", "poivre", "aneth"))
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("SAVEUR", "LS", "PAGE", "POUR", "CONRAD", "INGRÉDIENTS", "PERSONNES"))
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("avenue", "rue", "hôtel"))
        `when`(res.getStringArray(R.array.phone_prefixes)).thenReturn(arrayOf("tél", "tel"))
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("merlot", "region"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("")
    }

    @Test
    fun `test first scenario salade avocat from screen`() {
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
            •Coupez l'avocat en petits dés et hachez I'aneth.
            • Coupez le saumon en dés de 5 mm sur 5 mm.
            • Préparez une vinaigrette avec l'huile d'olive, le jus de citron, le miel, le vinaigre, du sel et du poivre.
            dans un saladier jusqu'à ce que vous obteniez une belle salade mêlée.
            • Répartissez la salade sur 4 assiettes et décorez avec des brins d'aneth.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, res)

        // Vérification du Titre (devrait extraire Salade d'avocat du reste)

        // Vérification des Ingrédients
        assertTrue(result.ingredients?.contains("1 avocat") == true)
        assertTrue(result.ingredients?.contains("200 g de saumon") == true)
        
        // Vérification du nettoyage 1/2 citron
        assertTrue(result.ingredients?.contains("1/2 citron") == true)

        // Vérification des Instructions
        assertTrue(result.instructions?.contains("Faites cuire") == true)
        assertTrue(result.instructions?.contains("Répartissez la salade") == true)
    }

    @Test
    fun `test legacy conrad scenario`() {
        val ocrText = """
            LS 175
            CONRAD
            Avenue Louise 71
            02/542.42.42
            Nahit YILMAZ
            alaade d'aubergines
            Ingrédients pour 4 personnes
            2 aubergines
            sel, poivre
            mélanger le tout
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, res)
        assertEquals("Salade d'aubergines", result.title)
        assertTrue(result.source?.contains("Nahit YILMAZ") == true)
    }
}
