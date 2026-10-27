package com.example.chiefinventory.ui.actvity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction d'URL depuis les partages de liens Android (Marmiton, 750g, Chrome).
 *
 * @see REQ-4.7.1.2
 */
class ImportWebRecipeShareIntentTypeBTest {

    @Test
    fun `extractUrlFromSharedText should extract Marmiton recipe URL from shared text`() {
        val sharedText = "Regarde cette recette gourmande : https://www.marmiton.org/recettes/recette_tarte-aux-chicons_12345.aspx ! C'est trop bon."
        val url = ImportWebRecipeActivity.extractUrlFromSharedText(sharedText)

        assertThat(url).isEqualTo("https://www.marmiton.org/recettes/recette_tarte-aux-chicons_12345.aspx")
    }

    @Test
    fun `extractUrlFromSharedText should handle raw URL string without extra text`() {
        val sharedText = "https://www.750g.com/goulash-hongrois-r99.htm"
        val url = ImportWebRecipeActivity.extractUrlFromSharedText(sharedText)

        assertThat(url).isEqualTo("https://www.750g.com/goulash-hongrois-r99.htm")
    }
}
