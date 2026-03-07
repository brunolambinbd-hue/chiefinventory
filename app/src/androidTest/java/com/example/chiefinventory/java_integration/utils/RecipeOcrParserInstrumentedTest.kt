package com.example.chiefinventory.java_integration.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.utils.RecipeOcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test instrumenté pour vérifier que le parsing OCR fonctionne correctement
 * avec les vraies ressources (XML) du projet sur un appareil Android.
 */
@RunWith(AndroidJUnit4::class)
class RecipeOcrParserInstrumentedTest {

    @Test
    fun testScenarioAubergineConrad() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val res = appContext.resources

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
        
        // 2. Ingrédients (Normalisation pour vérification)
        val ing = (result.ingredients ?: "").replace(Regex("\\s+"), " ")
        assertTrue("Contient 6 aubergines", ing.contains("6 aubergines moyennes"))
        assertTrue("Contient 150 g de beurre", ing.contains("150 g de beurre"))
        assertTrue("Contient sel, poivre", ing.contains("sel, poivre"))

        // 3. Source (Vérification du correctif Hôtel)
        val source = result.source ?: ""
        assertTrue("Source contient Hôtel", source.contains("Hôtel", ignoreCase = true))
        assertTrue("Source contient tel", source.contains("02/542.42.42"))

        // 4. Portions
        assertEquals("4", result.servings)
    }

    @Test
    fun testScenarioAvocatPrincesses() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val res = appContext.resources

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

        // 1. Ingrédients (Vérification des liaisons 'et' / 'ou')
        val ing = (result.ingredients ?: "").replace(Regex("\\s+"), " ")
        assertTrue("Correction | -> 1 avocat", ing.contains("1 avocat"))
        assertTrue("Contient sel et poivre", ing.contains("sel et poivre"))
        assertTrue("Contient haricots", ing.contains("200 g de haricots"))

        // 2. Instructions (Vérification de la continuité narrative)
        val inst = result.instructions ?: ""
        assertTrue("Vinaigrette complète", inst.contains("Préparez une vinaigrette avec l'huile d'olive"))
        assertTrue("Phrase non coupée", inst.contains("les haricots sous un jet d'eau froide"))

        // 3. Vin
        assertTrue("Vin contient Chardonnay", result.wine?.contains("Chardonnay", ignoreCase = true) == true)

        // 4. Portions
        assertEquals("4", result.servings)
    }
}
