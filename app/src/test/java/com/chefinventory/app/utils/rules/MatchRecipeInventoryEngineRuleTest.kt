package com.chefinventory.app.utils.rules

import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
import com.chefinventory.app.utils.MatchRecipeInventoryEngine
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant l'évaluation de la faisabilité des recettes
 * d'après le taux de couverture du stock du garde-manger.
 */
class MatchRecipeInventoryEngineRuleTest {

    @Test
    fun `evaluateRecipesFeasibility should rank 100 percent match recipe at the top`() {
        val recipe1 = Recipe(id = 1L, title = "Tarte aux chicons")
        val recipe2 = Recipe(id = 2L, title = "Roulade de jambon de Parme")

        val ingr1 = listOf(
            RecipeIngredient(recipeId = 1L, ingredientName = "chicons", quantityRequired = 1.0, unit = "kg"),
            RecipeIngredient(recipeId = 1L, ingredientName = "beurre", quantityRequired = 100.0, unit = "g")
        )

        val ingr2 = listOf(
            RecipeIngredient(recipeId = 2L, ingredientName = "jambon de Parme", quantityRequired = 2.0, unit = "paquets"),
            RecipeIngredient(recipeId = 2L, ingredientName = "melon", quantityRequired = 1.0, unit = "pièce")
        )

        val recipeIngredientsMap = mapOf(1L to ingr1, 2L to ingr2)

        val pantry = listOf(
            Ingredient(id = 10L, name = "Chicons / Endives", quantity = 1.0, unit = "kg"),
            Ingredient(id = 11L, name = "Beurre doux", quantity = 250.0, unit = "g"),
            Ingredient(id = 12L, name = "Jambon de Parme", quantity = 0.0, unit = "paquets") // Out of stock
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe1, recipe2),
            recipeIngredientsMap = recipeIngredientsMap,
            pantryIngredients = pantry
        )

        assertWithMessage("Les 2 recettes doivent être évaluées pour leur faisabilité")
            .that(matches)
            .hasSize(2)

        // Recipe 1 (Tarte aux chicons) has 2/2 ingredients in stock -> 100%
        val topMatch = matches.first()
        assertWithMessage("La recette à 100% 'Tarte aux chicons' doit figurer en première position")
            .that(topMatch.recipe.title)
            .isEqualTo("Tarte aux chicons")

        assertWithMessage("Le pourcentage de correspondance de la première recette doit valoir 100.0%")
            .that(topMatch.matchPercentage)
            .isEqualTo(100.0)

        assertWithMessage("La liste des ingrédients manquants pour la première recette doit être vide")
            .that(topMatch.missingIngredients)
            .isEmpty()

        // Recipe 2 (Roulade de jambon) has 0/2 ingredients in stock -> 0%
        val secondMatch = matches[1]
        assertWithMessage("La recette à 0% 'Roulade de jambon de Parme' doit figurer en seconde position")
            .that(secondMatch.recipe.title)
            .isEqualTo("Roulade de jambon de Parme")

        assertWithMessage("Le pourcentage de correspondance de la seconde recette doit valoir 0.0%")
            .that(secondMatch.matchPercentage)
            .isEqualTo(0.0)

        assertWithMessage("L'ingrédient manquant 'jambon de Parme' doit être indiqué")
            .that(secondMatch.missingIngredients.first())
            .contains("jambon de Parme")
    }
}
