package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.Normalizer

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage multi-ingrédients par intersection (ET logique)
 * (ex: "chicons, jambon" ou "poulet + curry").
 */
class MatchRecipeMultiIngredientFilterTypeBTest {

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    private fun matchesMultiIngredients(res: RecipeMatchResult, rawQuery: String): Boolean {
        val tokens = rawQuery.split(Regex("[,+&]|\\bet\\b"))
            .map { stripAccents(it) }
            .filter { it.length >= 2 }

        val titleStr = res.recipe.title
        val instStr = res.recipe.instructions ?: ""

        return tokens.isEmpty() || tokens.all { token ->
            stripAccents(titleStr).contains(token) ||
            stripAccents(instStr).contains(token) ||
            res.missingIngredients.any { stripAccents(it).contains(token) }
        }
    }

    @Test
    fun `matchesMultiIngredients should return true only when ALL ingredient tokens are present in recipe`() {
        val resChiconJambon = RecipeMatchResult(
            recipe = Recipe(id = 1L, title = "Tarte aux chicons et au jambon", instructions = "200 g de jambon de Parme, 4 chicons"),
            totalRequired = 2, totalInStock = 2, missingIngredients = emptyList(), matchPercentage = 100.0
        )

        val resChiconSeul = RecipeMatchResult(
            recipe = Recipe(id = 2L, title = "Gratin de chicons", instructions = "4 chicons braisés au beurre"),
            totalRequired = 1, totalInStock = 1, missingIngredients = emptyList(), matchPercentage = 100.0
        )

        val list = listOf(resChiconJambon, resChiconSeul)

        // Cas 1 : "chicons, jambon" -> Seule la recette avec les DEUX ingrédients doit matcher !
        val multiMatches = list.filter { matchesMultiIngredients(it, "chicons, jambon") }
        assertThat(multiMatches).hasSize(1)
        assertThat(multiMatches.first().recipe.title).isEqualTo("Tarte aux chicons et au jambon")

        // Cas 2 : "chicons + beurre" -> Seule la deuxième recette matchant les deux
        val multiMatches2 = list.filter { matchesMultiIngredients(it, "chicons + beurre") }
        assertThat(multiMatches2).hasSize(1)
        assertThat(multiMatches2.first().recipe.title).isEqualTo("Gratin de chicons")
    }
}
