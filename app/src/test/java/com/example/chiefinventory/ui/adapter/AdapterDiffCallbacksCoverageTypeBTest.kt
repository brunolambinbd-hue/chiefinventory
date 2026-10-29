package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.utils.RecipeMatchResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B couvrant la logique des [DiffUtil.ItemCallback]
 * accessibles du paquetage [com.example.chiefinventory.ui.adapter] pour la couverture JaCoCo.
 */
class AdapterDiffCallbacksCoverageTypeBTest {

    @Test
    fun `MatchRecipeAdapter DiffCallback should compare recipe IDs and equality`() {
        val recipe1 = Recipe(id = 1L, title = "Tarte Tatin")
        val recipe2 = Recipe(id = 2L, title = "Osso bucco")
        val res1 = RecipeMatchResult(recipe1, 2, 2, emptyList(), 100.0)
        val res1Same = RecipeMatchResult(recipe1, 2, 2, emptyList(), 100.0)
        val res2 = RecipeMatchResult(recipe2, 3, 1, listOf("1 kg de veau"), 33.3)

        assertThat(MatchRecipeAdapter.DiffCallback.areItemsTheSame(res1, res1Same)).isTrue()
        assertThat(MatchRecipeAdapter.DiffCallback.areItemsTheSame(res1, res2)).isFalse()
        assertThat(MatchRecipeAdapter.DiffCallback.areContentsTheSame(res1, res1Same)).isTrue()
        assertThat(MatchRecipeAdapter.DiffCallback.areContentsTheSame(res1, res2)).isFalse()
    }

    @Test
    fun `IngredientAdapter DiffCallback should compare ingredient IDs and equality`() {
        val ingr1 = Ingredient(id = 10L, name = "Beurre", quantity = 100.0)
        val ingr1Same = Ingredient(id = 10L, name = "Beurre", quantity = 100.0)
        val ingr2 = Ingredient(id = 20L, name = "Lait", quantity = 1.0)

        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr2)).isFalse()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr2)).isFalse()
    }

    @Test
    fun `RecipeAdapter RecipeDiffCallback should compare recipe IDs and equality`() {
        val callback = RecipeAdapter.RecipeDiffCallback()
        val rec1 = Recipe(id = 5L, title = "Coq au vin")
        val rec1Same = Recipe(id = 5L, title = "Coq au vin")
        val rec2 = Recipe(id = 6L, title = "Panna cotta")

        assertThat(callback.areItemsTheSame(rec1, rec1Same)).isTrue()
        assertThat(callback.areItemsTheSame(rec1, rec2)).isFalse()
        assertThat(callback.areContentsTheSame(rec1, rec1Same)).isTrue()
        assertThat(callback.areContentsTheSame(rec1, rec2)).isFalse()
    }

    @Test
    fun `ShoppingListAdapter DiffCallback should compare ingredient IDs and equality`() {
        val item1 = Ingredient(id = 30L, name = "Œufs", quantity = 6.0)
        val item1Same = Ingredient(id = 30L, name = "Œufs", quantity = 6.0)
        val item2 = Ingredient(id = 40L, name = "Farine", quantity = 500.0)

        assertThat(ShoppingListAdapter.DiffCallback.areItemsTheSame(item1, item1Same)).isTrue()
        assertThat(ShoppingListAdapter.DiffCallback.areItemsTheSame(item1, item2)).isFalse()
        assertThat(ShoppingListAdapter.DiffCallback.areContentsTheSame(item1, item1Same)).isTrue()
        assertThat(ShoppingListAdapter.DiffCallback.areContentsTheSame(item1, item2)).isFalse()
    }

    @Test
    fun `StandardCatalogAdapter DiffCallback should compare catalog items by name`() {
        val catItem1 = StandardCatalogItem(name = "Beurre doux", defaultQuantity = 100.0)
        val catItem2 = StandardCatalogItem(name = "Lait entier", defaultQuantity = 1.0)

        val entry1 = SelectedCatalogEntry(item = catItem1, isSelected = true)
        val entry1Same = SelectedCatalogEntry(item = catItem1, isSelected = true)
        val entry2 = SelectedCatalogEntry(item = catItem2, isSelected = false)

        assertThat(StandardCatalogAdapter.DiffCallback.areItemsTheSame(entry1, entry1Same)).isTrue()
        assertThat(StandardCatalogAdapter.DiffCallback.areItemsTheSame(entry1, entry2)).isFalse()
        assertThat(StandardCatalogAdapter.DiffCallback.areContentsTheSame(entry1, entry1Same)).isTrue()
        assertThat(StandardCatalogAdapter.DiffCallback.areContentsTheSame(entry1, entry2)).isFalse()
    }

    @Test
    fun `StandardCatalogAdapter determineStockState should evaluate stock states`() {
        assertThat(StandardCatalogAdapter.determineStockState(isSelected = false, currentQuantity = 10.0, threshold = 5.0)).isEqualTo(CatalogStockState.OUT_OF_STOCK)
        assertThat(StandardCatalogAdapter.determineStockState(isSelected = true, currentQuantity = 0.0, threshold = 5.0)).isEqualTo(CatalogStockState.OUT_OF_STOCK)
        assertThat(StandardCatalogAdapter.determineStockState(isSelected = true, currentQuantity = 3.0, threshold = 5.0)).isEqualTo(CatalogStockState.LOW_STOCK)
        assertThat(StandardCatalogAdapter.determineStockState(isSelected = true, currentQuantity = 10.0, threshold = 5.0)).isEqualTo(CatalogStockState.HEALTHY_STOCK)
    }

    @Test
    fun `RecipeCategoryAdapter DiffCallback should compare recipe categories by ID`() {
        val callback = RecipeCategoryAdapter.DiffCallback()
        val cat1 = RecipeCategoryInfo(categoryId = 10L, categoryName = "Entrée", recipeCount = 5)
        val cat1Same = RecipeCategoryInfo(categoryId = 10L, categoryName = "Entrée", recipeCount = 5)
        val cat2 = RecipeCategoryInfo(categoryId = 20L, categoryName = "Dessert", recipeCount = 12)

        assertThat(callback.areItemsTheSame(cat1, cat1Same)).isTrue()
        assertThat(callback.areItemsTheSame(cat1, cat2)).isFalse()
        assertThat(callback.areContentsTheSame(cat1, cat1Same)).isTrue()
        assertThat(callback.areContentsTheSame(cat1, cat2)).isFalse()
    }

    @Test
    fun `IngredientSelectAdapter DiffCallback should compare ingredient items by name`() {
        val callback = IngredientSelectAdapter.DiffCallback()
        val item1 = IngredientSelectAdapter.IngredientItem(name = "Sucre", isSelected = true)
        val item1Same = IngredientSelectAdapter.IngredientItem(name = "Sucre", isSelected = true)
        val item2 = IngredientSelectAdapter.IngredientItem(name = "Sel", isSelected = false)

        assertThat(callback.areItemsTheSame(item1, item1Same)).isTrue()
        assertThat(callback.areItemsTheSame(item1, item2)).isFalse()
        assertThat(callback.areContentsTheSame(item1, item1Same)).isTrue()
        assertThat(callback.areContentsTheSame(item1, item2)).isFalse()
    }

    @Test
    fun `CategoryAdapter DiffCallback should compare categories by name and content`() {
        val cat1 = com.example.chiefinventory.ui.model.CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 0)
        val cat1Same = com.example.chiefinventory.ui.model.CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 0)
        val cat2 = com.example.chiefinventory.ui.model.CategoryInfo(name = "Comics", possessedCount = 5, totalCount = 15, statusColorRes = 0)

        assertThat(CategoryAdapter.DiffCallback.areItemsTheSame(cat1, cat1Same)).isTrue()
        assertThat(CategoryAdapter.DiffCallback.areItemsTheSame(cat1, cat2)).isFalse()
        assertThat(CategoryAdapter.DiffCallback.areContentsTheSame(cat1, cat1Same)).isTrue()
        assertThat(CategoryAdapter.DiffCallback.areContentsTheSame(cat1, cat2)).isFalse()
    }

    @Test
    fun `CategoryAdapterRevised DiffCallback should compare categories and return change payload`() {
        val cat1 = com.example.chiefinventory.ui.model.CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 0)
        val cat1Same = com.example.chiefinventory.ui.model.CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 0)
        val cat1Changed = com.example.chiefinventory.ui.model.CategoryInfo(name = "BD", possessedCount = 12, totalCount = 20, statusColorRes = 0)
        val cat2 = com.example.chiefinventory.ui.model.CategoryInfo(name = "Comics", possessedCount = 5, totalCount = 15, statusColorRes = 0)

        assertThat(CategoryAdapterRevised.DiffCallback.areItemsTheSame(cat1, cat1Same)).isTrue()
        assertThat(CategoryAdapterRevised.DiffCallback.areItemsTheSame(cat1, cat2)).isFalse()
        assertThat(CategoryAdapterRevised.DiffCallback.areContentsTheSame(cat1, cat1Same)).isTrue()
        assertThat(CategoryAdapterRevised.DiffCallback.areContentsTheSame(cat1, cat1Changed)).isFalse()

        assertThat(CategoryAdapterRevised.DiffCallback.getChangePayload(cat1, cat1Same)).isNull()
        assertThat(CategoryAdapterRevised.DiffCallback.getChangePayload(cat1, cat1Changed)).isEqualTo(CategoryAdapterRevised.Payload.PROGRESS_UPDATE)
    }
}
