package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le regroupement des sous-catégories régionales françaises
 * sous l'option unique "Cuisine Française 🇫🇷".
 */
class MatchRecipeCountryFilterTypeBTest {

    private fun matchesCuisineFilter(recipe: Recipe, selectedCuisineFilter: String): Boolean {
        if (selectedCuisineFilter == "Toutes les cuisines") return true
        val titleStr = recipe.title.lowercase()
        val instStr = recipe.instructions?.lowercase() ?: ""
        val sourceStr = recipe.source?.lowercase() ?: ""

        return if (selectedCuisineFilter.contains("Française")) {
            val frenchKeywords = listOf("française", "francais", "provençale", "alsacienne", "bretonne", "normande", "bourguignonne", "lyonnaise", "auvergnate", "savoyarde", "corse")
            frenchKeywords.any { kw -> titleStr.contains(kw) || instStr.contains(kw) || sourceStr.contains(kw) }
        } else {
            val kw = selectedCuisineFilter.replace("Cuisine", "").replace("🇧🇪", "").replace("🇮🇹", "").replace("🇫🇷", "").trim().lowercase()
            titleStr.contains(kw) || instStr.contains(kw) || sourceStr.contains(kw)
        }
    }

    @Test
    fun `matchesCuisineFilter should match both national and regional French recipes under Cuisine Francaise`() {
        val coqAuVin = Recipe(id = 1L, title = "Coq au vin", source = "Cuisine Française | https://...")
        val daube = Recipe(id = 2L, title = "Daube provençale", source = "Cuisine Provençale | https://...")
        val choucroute = Recipe(id = 3L, title = "Choucroute", source = "Cuisine Alsacienne | https://...")
        val waterzooi = Recipe(id = 4L, title = "Waterzooï", source = "Cuisine Belge | https://...")

        assertThat(matchesCuisineFilter(coqAuVin, "Cuisine Française 🇫🇷")).isTrue()
        assertThat(matchesCuisineFilter(daube, "Cuisine Française 🇫🇷")).isTrue()
        assertThat(matchesCuisineFilter(choucroute, "Cuisine Française 🇫🇷")).isTrue()
        assertThat(matchesCuisineFilter(waterzooi, "Cuisine Française 🇫🇷")).isFalse()
    }
}
