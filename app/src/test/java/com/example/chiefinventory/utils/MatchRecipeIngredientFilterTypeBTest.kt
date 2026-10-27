package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.Normalizer

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage textuel par ingrédient ou titre
 * dans le moteur de faisabilité du garde-manger.
 */
class MatchRecipeIngredientFilterTypeBTest {

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    private fun matchesIngredientQuery(res: RecipeMatchResult, query: String): Boolean {
        val cleanQuery = stripAccents(query)
        if (cleanQuery.isBlank()) return true

        val cleanTitle = stripAccents(res.recipe.title)
        val cleanInst = stripAccents(res.recipe.instructions ?: "")
        val matchesMissing = res.missingIngredients.any { stripAccents(it).contains(cleanQuery) }

        return cleanTitle.contains(cleanQuery) || cleanInst.contains(cleanQuery) || matchesMissing
    }

    @Test
    fun `matchesIngredientQuery should filter results by ingredient or title query`() {
        val res1 = RecipeMatchResult(
            recipe = Recipe(id = 1L, title = "Tarte aux chicons", instructions = "4 chicons, 100g beurre"),
            totalRequired = 2, totalInStock = 2, missingIngredients = emptyList(), matchPercentage = 100.0
        )
        val res2 = RecipeMatchResult(
            recipe = Recipe(id = 2L, title = "Bœuf Bourguignon", instructions = "600g bœuf, oignons, vin rouge"),
            totalRequired = 3, totalInStock = 2, missingIngredients = listOf("1 bouteille vin rouge"), matchPercentage = 66.6
        )

        val list = listOf(res1, res2)

        val chiconMatches = list.filter { matchesIngredientQuery(it, "chicon") }
        assertThat(chiconMatches).hasSize(1)
        assertThat(chiconMatches.first().recipe.title).isEqualTo("Tarte aux chicons")

        val vinMatches = list.filter { matchesIngredientQuery(it, "vin") }
        assertThat(vinMatches).hasSize(1)
        assertThat(vinMatches.first().recipe.title).isEqualTo("Bœuf Bourguignon")
    }
}
