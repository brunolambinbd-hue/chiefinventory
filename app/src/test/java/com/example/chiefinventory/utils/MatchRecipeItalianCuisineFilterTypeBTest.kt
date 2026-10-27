package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage par cuisine italienne
 * dans l'écran "Que Cuisiner par mon Garde-Manger".
 */
class MatchRecipeItalianCuisineFilterTypeBTest {

    private fun matchesCuisineFilter(recipe: Recipe, selectedCuisineFilter: String): Boolean {
        if (selectedCuisineFilter == "Toutes les cuisines") return true
        val kw = selectedCuisineFilter.replace("Cuisine", "").replace("🇧🇪", "").replace("🇮🇹", "").replace("🇫🇷", "").trim().lowercase()
        val titleStr = recipe.title.lowercase()
        val instStr = recipe.instructions?.lowercase() ?: ""
        val sourceStr = recipe.source?.lowercase() ?: ""

        return titleStr.contains(kw) || instStr.contains(kw) || sourceStr.contains(kw)
    }

    @Test
    fun `matchesCuisineFilter should match Italian recipes when Cuisine Italienne option is selected`() {
        val ossoBucco = Recipe(id = 1L, title = "Osso bucco", source = "Cuisine Italienne | https://fr.wikibooks.org/wiki/Osso_bucco")
        val waterzooi = Recipe(id = 2L, title = "Waterzooï", source = "Cuisine Belge | https://fr.wikibooks.org/wiki/Waterzooi")

        assertThat(matchesCuisineFilter(ossoBucco, "Cuisine Italienne 🇮🇹")).isTrue()
        assertThat(matchesCuisineFilter(waterzooi, "Cuisine Italienne 🇮🇹")).isFalse()
        assertThat(matchesCuisineFilter(waterzooi, "Cuisine Belge 🇧🇪")).isTrue()
    }
}
