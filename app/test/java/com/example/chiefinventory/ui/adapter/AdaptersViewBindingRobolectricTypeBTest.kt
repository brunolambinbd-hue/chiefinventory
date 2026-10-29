package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.dao.SignatureReportItem
import com.example.chiefinventory.model.Category
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.utils.RecipeMatchResult
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B assurant la couverture à 100% du binding
 * et du rendu IHM (onCreateViewHolder, onBindViewHolder, clics) de tous les adaptateurs de ui.adapter.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptersViewBindingRobolectricTypeBTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `MatchRecipeAdapter should create viewHolder and bind 100 percent match recipe item`() {
        var clickedResult: RecipeMatchResult? = null
        val adapter = MatchRecipeAdapter { clickedResult = it }

        val recipe = Recipe(id = 101L, title = "Tarte Tatin")
        val matchResult = RecipeMatchResult(recipe, totalRequired = 3, totalInStock = 3, missingIngredients = emptyList(), matchPercentage = 100.0)

        adapter.submitList(listOf(matchResult))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvRecipeTitle.text.toString()).isEqualTo("Tarte Tatin")
        assertThat(holder.binding.tvMatchPercentage.text.toString()).isEqualTo("100% Stock")

        holder.binding.root.performClick()
        assertThat(clickedResult).isEqualTo(matchResult)
    }

    @Test
    fun `MatchRecipeAdapter should bind partial match recipe item with missing details`() {
        val adapter = MatchRecipeAdapter {}

        val recipe = Recipe(id = 102L, title = "Osso bucco")
        val matchResult = RecipeMatchResult(recipe, totalRequired = 4, totalInStock = 2, missingIngredients = listOf("2 carottes", "1 verre de vin"), matchPercentage = 50.0)

        adapter.submitList(listOf(matchResult))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvRecipeTitle.text.toString()).isEqualTo("Osso bucco")
        assertThat(holder.binding.tvMissingDetails.text.toString()).contains("2 carottes, 1 verre de vin")
    }

    @Test
    fun `IngredientAdapter should create viewHolder and bind healthy ingredient`() {
        var clickedIngr: Ingredient? = null
        var editedIngr: Ingredient? = null

        val adapter = IngredientAdapter(
            onClick = { clickedIngr = it },
            onEditClick = { editedIngr = it },
            locationNamesMap = mapOf(1L to "Frigo")
        )

        val ingredient = Ingredient(id = 1L, name = "Beurre", quantity = 250.0, unit = "g", minThreshold = 100.0, packSize = 1.0, locationId = 1L, category = "Crémerie")
        adapter.submitList(listOf(ingredient))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvName.text.toString()).isEqualTo("Beurre")
        assertThat(holder.binding.tvQuantity.text.toString()).isEqualTo("250 g")

        holder.binding.root.performClick()
        assertThat(clickedIngr).isEqualTo(ingredient)

        holder.binding.btnEditIngredient.performClick()
        assertThat(editedIngr).isEqualTo(ingredient)
    }

    @Test
    fun `StandardCatalogAdapter should create viewHolder and bind selected catalog entry`() {
        var selectionCount: Int? = null
        var editedItem: StandardCatalogItem? = null

        val adapter = StandardCatalogAdapter(
            onSelectionChanged = { selectionCount = it },
            onEditIngredientClicked = { editedItem = it }
        )

        val catalogItem = StandardCatalogItem(name = "Lait demi-écrémé", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie", defaultLocationName = "Frigo", minThreshold = 0.25, packSize = 6.0)
        val entry = SelectedCatalogEntry(item = catalogItem, isSelected = true, currentQuantity = 1.0)

        adapter.submitList(listOf(entry))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvIngredientName.text.toString()).isEqualTo("Lait demi-écrémé")

        holder.binding.btnIncrement.performClick()
        assertThat(entry.currentQuantity).isEqualTo(2.0)

        holder.binding.btnDecrement.performClick()
        assertThat(entry.currentQuantity).isEqualTo(1.0)

        holder.binding.btnEditIngredient.performClick()
        assertThat(editedItem).isEqualTo(catalogItem)

        assertThat(adapter.getSelectionCount()).isEqualTo(1)
        assertThat(adapter.getSelectedEntries()).hasSize(1)
    }

    @Test
    fun `ShoppingListAdapter should create viewHolder and bind ingredient to buy`() {
        var boughtChangedIngr: Ingredient? = null
        var boughtStatus: Boolean? = null

        val adapter = ShoppingListAdapter { ingr, isChecked ->
            boughtChangedIngr = ingr
            boughtStatus = isChecked
        }

        val ingredient = Ingredient(id = 5L, name = "Œufs", quantity = 0.0, unit = "pièce", minThreshold = 6.0, packSize = 6.0, category = "Crémerie")
        adapter.submitList(listOf(ingredient))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvQuantityToBuy.text.toString()).isEqualTo("6 pièce")
        assertThat(holder.binding.tvItemName.text.toString()).contains("Œufs")

        holder.binding.cbBought.isChecked = true
        assertThat(boughtChangedIngr).isEqualTo(ingredient)
        assertThat(boughtStatus).isTrue()
    }

    @Test
    fun `RecipeAdapter should create viewHolder and bind recipe item`() {
        var clickedRecipe: Recipe? = null
        val adapter = RecipeAdapter { clickedRecipe = it }

        val recipe = Recipe(id = 12L, title = "Carbonnade flamande", preparationTimeMinutes = 20, cookingTimeMinutes = 90, servings = 4, category = "Plat principal")
        adapter.submitList(listOf(recipe))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvRecipeTitle.text.toString()).isEqualTo("Carbonnade flamande")

        holder.binding.root.performClick()
        assertThat(clickedRecipe).isEqualTo(recipe)
    }

    @Test
    fun `LocationAdapter should create viewHolder and bind location item`() {
        var clickedLoc: Location? = null
        val adapter = LocationAdapter { clickedLoc = it }

        val location = Location(id = 1L, name = "Frigo", parentId = null, type = LocationType.STORAGE)
        adapter.submitList(listOf(location))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvLocationName.text.toString()).isEqualTo("Frigo")

        holder.binding.root.performClick()
        assertThat(clickedLoc).isEqualTo(location)
    }

    @Test
    fun `CollectionAdapter should create viewHolder and bind collection item`() {
        var clickedItem: CollectionItem? = null
        val adapter = CollectionAdapter { clickedItem = it }

        val collectionItem = CollectionItem(id = 88L, titre = "BD Spirou n°12", isPossessed = true)
        adapter.submitList(listOf(collectionItem))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvTitle.text.toString()).isEqualTo("BD Spirou n°12")

        holder.binding.root.performClick()
        assertThat(clickedItem).isEqualTo(collectionItem)
    }

    @Test
    fun `CategoryAdapterRevised should create viewHolder and bind category item`() {
        var clickedCat: Category? = null
        val adapter = CategoryAdapterRevised { clickedCat = it }

        val category = Category(id = 2L, name = "Comics")
        adapter.submitList(listOf(category))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvCategoryName.text.toString()).isEqualTo("Comics")

        holder.binding.root.performClick()
        assertThat(clickedCat).isEqualTo(category)
    }

    @Test
    fun `RecipeCategoryAdapter should create viewHolder and bind recipe category info`() {
        var clickedCat: RecipeCategoryInfo? = null
        val adapter = RecipeCategoryAdapter { clickedCat = it }

        val info = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 15)
        adapter.submitList(listOf(info))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvCategoryName.text.toString()).isEqualTo("Desserts")

        holder.binding.root.performClick()
        assertThat(clickedCat).isEqualTo(info)
    }

    @Test
    fun `SignatureReportAdapter should create viewHolder and bind signature report item`() {
        val adapter = SignatureReportAdapter()

        val item = SignatureReportItem(id = 99L, titre = "Objet Signature Test", hasSignature = true)
        adapter.submitList(listOf(item))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvTitle.text.toString()).isEqualTo("Objet Signature Test")
    }

    @Test
    fun `IngredientSelectAdapter should create viewHolder and bind select item`() {
        var selectedItem: IngredientSelectAdapter.IngredientItem? = null
        var selectedStatus: Boolean? = null

        val adapter = IngredientSelectAdapter { item, isChecked ->
            selectedItem = item
            selectedStatus = isChecked
        }

        val selectItem = IngredientSelectAdapter.IngredientItem(id = 77L, name = "Sucre vanillé", isSelected = false)
        adapter.submitList(listOf(selectItem))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.cbSelect.text.toString()).isEqualTo("Sucre vanillé")

        holder.binding.cbSelect.isChecked = true
        assertThat(selectedItem).isEqualTo(selectItem)
        assertThat(selectedStatus).isTrue()
    }
}
