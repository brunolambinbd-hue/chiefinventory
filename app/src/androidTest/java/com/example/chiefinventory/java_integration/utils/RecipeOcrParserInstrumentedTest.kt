package com.example.chiefinventory.java_integration.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.utils.RecipeOcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test instrumenté pour vérifier que le parsing OCR fonctionne correctement
 * avec les vraies ressources (XML) du projet.
 */
@RunWith(AndroidJUnit4::class)
class RecipeOcrParserInstrumentedTest {

    @Test
    fun testRealWorldOcrScenario() {
        // Obtenir le contexte et les ressources réelles de l'application
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val res = appContext.resources

        // Le texte simulant le scan qui posait problème
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
            Rousse Region Merlot
        """.trimIndent()

        // Exécution du parsing
        val result = RecipeOcrParser.parse(ocrText, res)

        // 1. Vérification du Titre (Lettrine S réparée et bruits supprimés)
        // Note: Si "alaade" est détecté, notre règle le transforme en "Salade"
        assertEquals("Salade d'aubergines", result.title)

        // 2. Vérification des Portions
        assertEquals("4", result.servings)

        // 3. Vérification des Ingrédients
        val ingredients = result.ingredients ?: ""
        assertTrue("Devrait contenir l'ingrédient principal", ingredients.contains("2 aubergines"))
        assertTrue("Devrait contenir l'assaisonnement", ingredients.contains("sel"))
        // Nahit YILMAZ ne doit pas être un ingrédient
        assertTrue("Ne doit pas contenir le chef", !ingredients.contains("Nahit YILMAZ"))

        // 4. Vérification de la Source
        val source = result.source ?: ""
        assertTrue("Doit contenir l'hôtel", source.contains("CONRAD", ignoreCase = true))
        assertTrue("Doit contenir l'adresse", source.contains("Avenue Louise"))
        assertTrue("Doit contenir le téléphone", source.contains("02/542.42.42"))
        assertTrue("Doit contenir le nom du chef", source.contains("Nahit YILMAZ"))

        // 5. Vérification du Vin
        assertTrue("Le vin doit être correctement identifié", result.wine?.contains("Merlot") == true)

        // 6. Vérification des Instructions
        assertTrue("L'instruction doit être présente", result.instructions?.contains("mélanger") == true)
    }
}
