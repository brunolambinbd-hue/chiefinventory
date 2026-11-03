package com.chefinventory.app.utils

import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'évaluation de la faisabilité des recettes
 * d'après la comparaison directe en minuscules du garde-manger.
 *
 * @see REQ-4.6.1.1
 * @see REQ-4.6.1.2
 */
class MatchRecipeInventoryEngineTypeBTest {

    @Test
    fun `loadVocabSafely with throwing loader should return null`() {
        val result = MatchRecipeInventoryEngine.loadVocabSafely { throw RuntimeException("Resource exception") }
        assertThat(result).isNull()
    }

    @Test
    fun `buildUnitPrefixPattern with null vocabulary should return valid regex`() {
        val regexNull = MatchRecipeInventoryEngine.buildUnitPrefixPattern(null)
        assertThat(regexNull.pattern).contains("cuillère")

        val mockVocab = OcrVocabularyLoader.loadFromProjectResources().copy(culinaryMeasurementUnits = listOf("g", "   ", "cl"))
        val regexWithBlank = MatchRecipeInventoryEngine.buildUnitPrefixPattern(mockVocab)
        assertThat(regexWithBlank.pattern).contains("g")
    }

    @Test
    fun `evaluateRecipesFeasibility should match pepper only seasoning and recipe contains pantry`() {
        // Pepper only seasoning (isSel = false, isPoivre = true L135)
        val recipe1 = Recipe(id = 70L, title = "Poivre seul")
        val recipeIngr1 = listOf(RecipeIngredient(recipeId = 70L, ingredientName = "Poivre noir", quantityRequired = null))
        val pantry1 = listOf(Ingredient(id = 1L, name = "Poivre vert", quantity = 1.0))

        val matches1 = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(listOf(recipe1), mapOf(70L to recipeIngr1), pantry1)
        assertThat(matches1.first().totalInStock).isEqualTo(1)

        // CleanReq contains CleanPantry (recipe: "Farine de blé T55", pantry: "Farine" L142)
        val recipe2 = Recipe(id = 71L, title = "Farine spéciale")
        val recipeIngr2 = listOf(RecipeIngredient(recipeId = 71L, ingredientName = "Farine de blé T55", quantityRequired = 250.0))
        val pantry2 = listOf(Ingredient(id = 2L, name = "Farine", quantity = 1.0))

        val matches2 = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(listOf(recipe2), mapOf(71L to recipeIngr2), pantry2)
        assertThat(matches2.first().totalInStock).isEqualTo(1)

        // Seasoning pepper requirement with sel in pantry (covers cleanPantry.contains("sel") L148)
        val recipe3 = Recipe(id = 90L, title = "Poivre seul")
        val recipeIngr3 = listOf(RecipeIngredient(recipeId = 90L, ingredientName = "Poivre noir", quantityRequired = null))
        val pantry3 = listOf(Ingredient(id = 3L, name = "Sel fin", quantity = 1.0))

        val matches3 = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(listOf(recipe3), mapOf(90L to recipeIngr3), pantry3)
        assertThat(matches3.first().totalInStock).isEqualTo(1)

        // Seasoning requirement with unrelated item in pantry (covers cleanPantry.contains("poivre") == false L148 and supplementalInfo.isBlank() L106)
        val recipe4 = Recipe(id = 91L, title = "Poivre seul 2")
        val recipeIngr4 = listOf(RecipeIngredient(recipeId = 91L, ingredientName = "Poivre noir", quantityRequired = 1.0, unit = "g", supplementalInfo = "   "))
        val pantry4 = listOf(Ingredient(id = 4L, name = "Farine de blé", quantity = 1.0))

        val matches4 = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(listOf(recipe4), mapOf(91L to recipeIngr4), pantry4)
        assertThat(matches4.first().totalInStock).isEqualTo(0)
        assertThat(matches4.first().missingIngredients.first()).contains("1 g Poivre noir")
    }

    @Test
    fun `evaluateRecipesFeasibility should handle empty formatted missing ingredient string`() {
        val recipe = Recipe(id = 80L, title = "Recette nom vide")
        val recipeIngr = listOf(RecipeIngredient(recipeId = 80L, ingredientName = "", quantityRequired = null, unit = null, supplementalInfo = null))

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(listOf(recipe), mapOf(80L to recipeIngr), emptyList())
        assertThat(matches.first().missingIngredients).contains("")
    }

    @Test
    fun `evaluateRecipesFeasibility should match oil and vinegar ingredients using pure lowercase substring matching`() {
        val recipe = Recipe(id = 10L, title = "Roulade de jambon de Parme")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 10L, ingredientName = "huile d'olive", quantityRequired = 3.0, unit = "c. à soupe"),
            RecipeIngredient(recipeId = 10L, ingredientName = "vinaigre", quantityRequired = 1.0, unit = "c. à soupe")
        )

        val pantry = listOf(
            Ingredient(id = 1L, name = "Huile d'olive vierge extra", quantity = 1.0, unit = "L"),
            Ingredient(id = 2L, name = "Vinaigre balsamique", quantity = 50.0, unit = "cl")
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(10L to recipeIngredients),
            pantryIngredients = pantry
        )

        assertThat(matches).hasSize(1)
        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(2)
        assertThat(result.totalRequired).isEqualTo(2)
        assertThat(result.matchPercentage).isEqualTo(100.0)
        assertThat(result.missingIngredients).isEmpty()
    }

    @Test
    fun `evaluateRecipesFeasibility should format missing ingredients with quantity unit name and supplemental info`() {
        val recipe = Recipe(id = 30L, title = "Roulade de jambon de Parme")
        val recipeIngredients = listOf(
            RecipeIngredient(
                recipeId = 30L,
                ingredientName = "jambon de Parme en fines tranches",
                quantityRequired = 3.0,
                unit = "paquets de",
                supplementalInfo = "125 à 180 g"
            )
        )

        val pantry = emptyList<Ingredient>()

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(30L to recipeIngredients),
            pantryIngredients = pantry
        )

        val result = matches.first()
        assertThat(result.missingIngredients).contains("3 paquets de jambon de Parme en fines tranches (125 à 180 g)")
    }

    @Test
    fun `evaluateRecipesFeasibility should report out of stock items in missing list when quantity is 0`() {
        val recipe = Recipe(id = 10L, title = "Roulade de jambon de Parme")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 10L, ingredientName = "huile d'olive", quantityRequired = 3.0, unit = "c. à soupe"),
            RecipeIngredient(recipeId = 10L, ingredientName = "vinaigre", quantityRequired = 1.0, unit = "c. à soupe")
        )

        val pantry = listOf(
            Ingredient(id = 1L, name = "Huile d'olive vierge extra", quantity = 0.0, unit = "L"), // 🔴 Out of stock
            Ingredient(id = 2L, name = "Vinaigre balsamique", quantity = 50.0, unit = "cl") // 🟢 In stock
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(10L to recipeIngredients),
            pantryIngredients = pantry
        )

        assertThat(matches).hasSize(1)
        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(1)
        assertThat(result.totalRequired).isEqualTo(2)
        assertThat(result.matchPercentage).isEqualTo(50.0)
        assertThat(result.missingIngredients.first()).contains("3 c. à soupe")
        assertThat(result.missingIngredients.first()).contains("huile d'olive")
        assertThat(result.missingIngredients.none { it.contains("vinaigre") }).isTrue()
    }

    @Test
    fun `evaluateRecipesFeasibility should match oil and vinegar even when recipe ingredientName includes spoon unit prefixes`() {
        val recipe = Recipe(id = 20L, title = "Roulade de jambon de Parme")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 20L, ingredientName = "c. à soupe d'huile d'olive", quantityRequired = 3.0, unit = null),
            RecipeIngredient(recipeId = 20L, ingredientName = "c. à soupe de vinaigre", quantityRequired = 1.0, unit = null)
        )

        val pantry = listOf(
            Ingredient(id = 1L, name = "Huile d'olive vierge extra", quantity = 1.0, unit = "L"),
            Ingredient(id = 2L, name = "Vinaigre balsamique", quantity = 50.0, unit = "cl")
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(20L to recipeIngredients),
            pantryIngredients = pantry
        )

        assertThat(matches).hasSize(1)
        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(2)
        assertThat(result.totalRequired).isEqualTo(2)
        assertThat(result.matchPercentage).isEqualTo(100.0)
        assertThat(result.missingIngredients).isEmpty()
    }

    @Test
    fun `evaluateRecipesFeasibility should match basic salt and pepper seasoning with sel fin or poivre du moulin in pantry`() {
        val recipe = Recipe(id = 30L, title = "Roulade de jambon de Parme")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 30L, ingredientName = "Sel et poivre", quantityRequired = null, unit = null)
        )

        val pantry = listOf(
            Ingredient(id = 1L, name = "Sel fin", quantity = 1.0, unit = "kg"),
            Ingredient(id = 2L, name = "Poivre du moulin", quantity = 1.0, unit = "flacon")
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(30L to recipeIngredients),
            pantryIngredients = pantry
        )

        assertThat(matches).hasSize(1)
        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(1)
        assertThat(result.matchPercentage).isEqualTo(100.0)
        assertThat(result.missingIngredients).isEmpty()
    }

    @Test
    fun `evaluateRecipesFeasibility should handle null pantry quantities and null recipe ingredient quantities`() {
        val recipe = Recipe(id = 40L, title = "Omelette nature")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 40L, ingredientName = "fines herbes", quantityRequired = 2.5, unit = "g", supplementalInfo = "hachées")
        )

        val pantry = listOf(
            Ingredient(id = 10L, name = "Fines herbes", quantity = null)
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(40L to recipeIngredients),
            pantryIngredients = pantry
        )

        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(0)
        assertThat(result.missingIngredients.first()).contains("2.5 g fines herbes (hachées)")
    }

    @Test
    fun `evaluateRecipesFeasibility should return 0 percentage when recipe has no required ingredients`() {
        val recipe = Recipe(id = 50L, title = "Eau de source")

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = emptyMap(),
            pantryIngredients = emptyList()
        )

        val result = matches.first()
        assertThat(result.totalRequired).isEqualTo(0)
        assertThat(result.matchPercentage).isEqualTo(0.0)
    }

    @Test
    fun `evaluateRecipesFeasibility should handle blank ingredientName in recipe ingredient`() {
        val recipe = Recipe(id = 60L, title = "Recette test")
        val recipeIngredients = listOf(
            RecipeIngredient(recipeId = 60L, ingredientName = "   ", quantityRequired = 1.0, unit = "  ")
        )

        val pantry = listOf(
            Ingredient(id = 1L, name = "Sel", quantity = 1.0)
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe),
            recipeIngredientsMap = mapOf(60L to recipeIngredients),
            pantryIngredients = pantry
        )

        val result = matches.first()
        assertThat(result.totalInStock).isEqualTo(0)
    }
}
