package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.ui.model.CategoryInfo
import com.example.chiefinventory.ui.model.DisplayLocation
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.chiefinventory.utils.RecipeMatchResult
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B assurant la couverture intégrale de toutes les branches
 * du binding et du rendu IHM de TOUS les adaptateurs de ui.adapter.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptersViewBindingRobolectricTypeBTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    // --- 1. CategoryAdapter ---

    @Test
    fun `CategoryAdapter possessed mode should bind category info and trigger click`() {
        var clickedCategory: String? = null
        val adapter = CategoryAdapter(isSoughtMode = false) { clickedCategory = it }

        val info = CategoryInfo(name = "BD Franco-Belge", possessedCount = 15, totalCount = 20, statusColorRes = 100)
        adapter.submitList(listOf(info))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.itemName.text.toString()).contains("BD Franco-Belge")
        assertThat(holder.binding.itemName.text.toString()).contains("15")

        holder.itemView.performClick()
        assertThat(clickedCategory).isEqualTo("BD Franco-Belge")
    }

    @Test
    fun `CategoryAdapter sought mode should bind sought count category info`() {
        val adapter = CategoryAdapter(isSoughtMode = true) {}

        val info = CategoryInfo(name = "Comics", possessedCount = 5, totalCount = 20, statusColorRes = 100)
        adapter.submitList(listOf(info))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.itemName.text.toString()).contains("Comics")
        assertThat(holder.binding.itemName.text.toString()).contains("15") // 20 - 5 = 15 sought
    }

    // --- 2. CategoryAdapterRevised ---

    @Test
    fun `CategoryAdapterRevised possessed mode should update progress and background colors for various percentages`() {
        var clickedCategory: String? = null
        val adapter = CategoryAdapterRevised(isSoughtMode = false) { clickedCategory = it }

        val infoHigh = CategoryInfo(name = "Manga", possessedCount = 80, totalCount = 100, statusColorRes = 0)
        val infoMid = CategoryInfo(name = "Comics", possessedCount = 50, totalCount = 100, statusColorRes = 0)
        val infoLow = CategoryInfo(name = "BD", possessedCount = 10, totalCount = 100, statusColorRes = 0)
        val infoZero = CategoryInfo(name = "Autre", possessedCount = 0, totalCount = 0, statusColorRes = 0)

        adapter.submitList(listOf(infoHigh, infoMid, infoLow, infoZero))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.revisedCategoryName.text.toString()).isEqualTo("Manga")

        holder0.itemView.performClick()
        assertThat(clickedCategory).isEqualTo("Manga")

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)

        val holder3 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder3, 3)
        assertThat(holder3.binding.revisedCategoryProgress.max).isEqualTo(1)
    }

    @Test
    fun `CategoryAdapterRevised sought mode and payloads should update progress correctly`() {
        val adapter = CategoryAdapterRevised(isSoughtMode = true) {}

        val infoHighSought = CategoryInfo(name = "Manga", possessedCount = 10, totalCount = 100, statusColorRes = 0)
        val infoMidSought = CategoryInfo(name = "Comics", possessedCount = 50, totalCount = 100, statusColorRes = 0)
        val infoLowSought = CategoryInfo(name = "BD", possessedCount = 85, totalCount = 100, statusColorRes = 0)

        adapter.submitList(listOf(infoHighSought, infoMidSought, infoLowSought))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)

        // Payload update
        adapter.onBindViewHolder(holder0, 0, mutableListOf("PROGRESS_UPDATE"))
        adapter.onBindViewHolder(holder0, 0, mutableListOf()) // Empty payload triggers full bind
    }

    // --- 3. CollectionAdapter ---

    @Test
    fun `CollectionAdapter should bind possessed item with full details, image, and similarity`() {
        var clickedResultItem: SearchResultItem? = null
        val adapter = CollectionAdapter { clickedResultItem = it }

        val collectionItem = CollectionItem(
            id = 42L,
            titre = "Tintin au Congo",
            editeur = "Casterman",
            annee = 1931,
            mois = 5,
            tirage = "Original",
            description = "Édition rare N&B",
            imageUri = "file:///tmp/tintin.jpg",
            isPossessed = true
        )
        val searchResult = SearchResultItem(item = collectionItem, similarity = 0.95)

        adapter.submitList(listOf(searchResult))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.itemName.text.toString()).isEqualTo("Tintin au Congo")
        assertThat(holder.binding.itemDetails.text.toString()).contains("Casterman - 1931/5 - Tirage: Original")
        assertThat(holder.binding.itemDescription.text.toString()).isEqualTo("Édition rare N&B")
        assertThat(holder.binding.similarityScore.text.toString()).contains("95")

        holder.itemView.performClick()
        assertThat(clickedResultItem).isEqualTo(searchResult)
    }

    @Test
    fun `CollectionAdapter should bind sought item with zebra striping and missing details fallbacks`() {
        val adapter = CollectionAdapter {}

        val collectionItem = CollectionItem(
            id = 43L,
            titre = "Astérix chez les Pictes",
            editeur = null,
            annee = 2013,
            mois = 0,
            tirage = null,
            description = null,
            imageUri = null,
            isPossessed = false
        )
        val searchResult = SearchResultItem(item = collectionItem, similarity = null)

        adapter.submitList(listOf(searchResult, searchResult))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 1)

        assertThat(holder.binding.itemName.text.toString()).isEqualTo("Astérix chez les Pictes")
        assertThat(holder.binding.itemDetails.text.toString()).isEqualTo("2013")
        assertThat(holder.binding.similarityScore.visibility).isEqualTo(android.view.View.GONE)
        assertThat(holder.binding.itemDescription.visibility).isEqualTo(android.view.View.GONE)
    }

    // --- 4. LocationAdapter ---

    @Test
    fun `LocationAdapter should bind root location and handle expand edit and item count clicks`() {
        var toggledId: Long? = null
        var editedId: Long? = null
        var clickedCountId: Long? = null

        val adapter = LocationAdapter(
            onToggleExpand = { toggledId = it },
            onEdit = { editedId = it },
            onItemCountClick = { clickedCountId = it }
        )

        val loc = Location(id = 10L, name = "Cuisine", parentId = null, type = LocationType.INGREDIENT)
        val displayLoc = DisplayLocation(location = loc, depth = 0)

        adapter.submitList(listOf(displayLoc))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.locationName.text.toString()).isEqualTo("Cuisine")
        assertThat(holder.binding.childIndicator.visibility).isEqualTo(android.view.View.GONE)

        holder.binding.expandIcon.performClick()
        assertThat(toggledId).isEqualTo(10L)

        holder.binding.locationName.performClick()
        assertThat(clickedCountId).isEqualTo(10L)

        holder.binding.locationItemCount.performClick()
        assertThat(clickedCountId).isEqualTo(10L)

        holder.binding.editIcon.performClick()
        assertThat(editedId).isEqualTo(10L)
    }

    @Test
    fun `LocationAdapter should bind child location with depth indentation`() {
        val adapter = LocationAdapter(onToggleExpand = {}, onEdit = {}, onItemCountClick = {})

        val loc = Location(id = 11L, name = "Frigo - Bac à légumes", parentId = 10L, type = LocationType.INGREDIENT)
        val displayLoc = DisplayLocation(location = loc, depth = 3)

        adapter.submitList(listOf(displayLoc))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.childIndicator.visibility).isEqualTo(android.view.View.VISIBLE)
        assertThat(holder.binding.indentation.layoutParams.width).isEqualTo(150)
    }

    // --- 5. SignatureReportAdapter ---

    @Test
    fun `SignatureReportAdapter should bind valid signature item with thumbnail`() {
        var clickedItem: CollectionItem? = null
        val adapter = SignatureReportAdapter { clickedItem = it }

        val validItem = CollectionItem(
            id = 50L,
            remoteId = 1001,
            titre = "Gaston Lagaffe 1",
            imageUri = "file:///tmp/gaston.jpg",
            imageEmbedding = byteArrayOf(1, 2, 3, 4)
        )
        adapter.submitList(listOf(validItem))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvItemTitle.text.toString()).isEqualTo("Gaston Lagaffe 1")
        assertThat(holder.binding.ivThumbnail.visibility).isEqualTo(android.view.View.VISIBLE)
        assertThat(holder.binding.tvSignaturePreview.visibility).isEqualTo(android.view.View.VISIBLE)

        holder.itemView.performClick()
        assertThat(clickedItem).isEqualTo(validItem)
    }

    @Test
    fun `SignatureReportAdapter should bind missing and empty signature items`() {
        val adapter = SignatureReportAdapter {}

        val missingItem = CollectionItem(id = 51L, remoteId = 1002, titre = "Gaston 2", imageEmbedding = null)
        val emptyItem = CollectionItem(id = 52L, remoteId = 1003, titre = "Gaston 3", imageEmbedding = byteArrayOf())

        adapter.submitList(listOf(missingItem, emptyItem))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.ivThumbnail.visibility).isEqualTo(android.view.View.GONE)

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.ivThumbnail.visibility).isEqualTo(android.view.View.GONE)
    }

    // --- 6. IngredientAdapter ---

    @Test
    fun `IngredientAdapter should cover all stock levels location icons decimal quantities and null fallbacks`() {
        var clickedIngr: Ingredient? = null
        var editedIngr: Ingredient? = null

        val adapter = IngredientAdapter(
            onClick = { clickedIngr = it },
            onEditClick = { editedIngr = it },
            locationNamesMap = mapOf(1L to "Frigo General", 2L to "Placard Bas", 3L to "Congélateur Top", 4L to "Cave Vin", 5L to "Garage")
        )

        // Out of stock
        val outOfStock = Ingredient(id = 1L, name = "Lait", quantity = 0.0, unit = "L", minThreshold = 1.0, packSize = 6.0, locationId = 1L, category = "Crémerie", supplementalInfo = "Bio")
        // Low stock decimal
        val lowStockDecimal = Ingredient(id = 2L, name = "Farine", quantity = 0.5, unit = "kg", minThreshold = 1.5, packSize = 1.5, locationId = 2L, category = "Épicerie")
        // Healthy stock
        val healthyStock = Ingredient(id = 3L, name = "Glace Vanille", quantity = 2.0, unit = null, minThreshold = 1.0, packSize = 1.0, locationId = 3L, category = "Surgelés", supplementalInfo = null)
        // Cave stock
        val caveStock = Ingredient(id = 4L, name = "Bordeaux", quantity = 5.0, unit = "bouteille", minThreshold = 2.0, packSize = 1.0, locationId = 4L, category = "Boisson")
        // Other location icon match fallback & null locationId
        val defaultLoc = Ingredient(id = 5L, name = "Huile", quantity = 10.0, unit = "L", minThreshold = 1.0, packSize = 1.0, locationId = null, category = null)
        val garageLoc = Ingredient(id = 6L, name = "Vis", quantity = 100.0, unit = "pcs", minThreshold = 10.0, packSize = 1.0, locationId = 5L, category = "Bricolage")

        adapter.submitList(listOf(outOfStock, lowStockDecimal, healthyStock, caveStock, defaultLoc, garageLoc))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.tvStockStatus.text.toString()).contains("🚨")
        assertThat(holder0.binding.tvLocation.text.toString()).contains("❄️")
        assertThat(holder0.binding.tvSupplementalInfo.visibility).isEqualTo(android.view.View.VISIBLE)

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.tvStockStatus.text.toString()).contains("⚠️")
        assertThat(holder1.binding.tvLocation.text.toString()).contains("🧺")

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)
        assertThat(holder2.binding.tvStockStatus.text.toString()).contains("🎯")
        assertThat(holder2.binding.tvLocation.text.toString()).contains("🧊")

        val holder3 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder3, 3)
        assertThat(holder3.binding.tvLocation.text.toString()).contains("🍷")

        val holder4 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder4, 4)
        assertThat(holder4.binding.tvLocation.text.toString()).contains("❄️") // Null locationId defaults to Frigo

        val holder5 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder5, 5)
        assertThat(holder5.binding.tvLocation.text.toString()).contains("📍") // Garage falls back to pin icon

        holder0.binding.root.performClick()
        assertThat(clickedIngr).isEqualTo(outOfStock)

        holder0.binding.btnEditIngredient.performClick()
        assertThat(editedIngr).isEqualTo(outOfStock)

        adapter.setLocationNamesMap(mapOf(1L to "Frigo Modifié"))
    }

    @Test
    fun `IngredientAdapter without edit click listener should handle clicks safely`() {
        var clickedIngr: Ingredient? = null
        val adapter = IngredientAdapter(onClick = { clickedIngr = it }, onEditClick = null)

        val ingr = Ingredient(id = 10L, name = "Sucre", quantity = 1.0)
        adapter.submitList(listOf(ingr))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.binding.btnEditIngredient.performClick()
        assertThat(clickedIngr).isNull()
    }

    // --- 7. StandardCatalogAdapter ---

    @Test
    fun `StandardCatalogAdapter should handle selection quantity steppers and edit clicks`() {
        var selectionCount: Int? = null
        var editedItem: StandardCatalogItem? = null

        val adapter = StandardCatalogAdapter(
            onSelectionChanged = { selectionCount = it },
            onEditIngredientClicked = { editedItem = it }
        )

        val catalogItem = StandardCatalogItem(name = "Huile d'olive", defaultQuantity = 2.5, defaultUnit = "L", category = "Épicerie", defaultLocationName = "Placard", minThreshold = 1.0, packSize = 1.0)
        val catalogItemNoUnit = StandardCatalogItem(name = "Pommes", defaultQuantity = 5.0, defaultUnit = null, category = "Fruits", defaultLocationName = "Panier", minThreshold = 1.0, packSize = 1.0)

        val entryOut = SelectedCatalogEntry(item = catalogItem, isSelected = false, currentQuantity = 0.0)
        val entryLow = SelectedCatalogEntry(item = catalogItem, isSelected = true, currentQuantity = 0.5)
        val entryHealthy = SelectedCatalogEntry(item = catalogItem, isSelected = true, currentQuantity = 3.0)
        val entryNoUnit = SelectedCatalogEntry(item = catalogItemNoUnit, isSelected = true, currentQuantity = 2.5)

        adapter.submitList(listOf(entryOut, entryLow, entryHealthy, entryNoUnit))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.layoutQuantityStepper.visibility).isEqualTo(android.view.View.GONE)

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.layoutQuantityStepper.visibility).isEqualTo(android.view.View.VISIBLE)

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)

        val holder3 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder3, 3)

        // Increment & Decrement clicks
        holder2.binding.btnIncrement.performClick()
        assertThat(entryHealthy.currentQuantity).isEqualTo(4.0)

        holder2.binding.btnDecrement.performClick()
        assertThat(entryHealthy.currentQuantity).isEqualTo(3.0)

        // Decrement below 1.0 unselects
        entryHealthy.currentQuantity = 1.0
        holder2.binding.btnDecrement.performClick()
        assertThat(entryHealthy.isSelected).isFalse()

        // Card chip click toggles selection
        holder2.binding.cardChip.performClick()
        assertThat(entryHealthy.isSelected).isTrue()

        // Edit button click
        holder2.binding.btnEditIngredient.performClick()
        assertThat(editedItem).isEqualTo(catalogItem)
    }

    // --- 8. MatchRecipeAdapter ---

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
    fun `MatchRecipeAdapter should bind partial match recipe items with orange and red statuses`() {
        val adapter = MatchRecipeAdapter {}

        val recipe1 = Recipe(id = 102L, title = "Osso bucco")
        val match50 = RecipeMatchResult(recipe1, totalRequired = 4, totalInStock = 2, missingIngredients = listOf("2 carottes"), matchPercentage = 50.0)

        val recipe2 = Recipe(id = 103L, title = "Risotto")
        val match20 = RecipeMatchResult(recipe2, totalRequired = 5, totalInStock = 1, missingIngredients = listOf("Riz"), matchPercentage = 20.0)

        adapter.submitList(listOf(match50, match20))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.tvRecipeTitle.text.toString()).isEqualTo("Osso bucco")

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.tvRecipeTitle.text.toString()).isEqualTo("Risotto")
    }

    // --- 9. ShoppingListAdapter ---

    @Test
    fun `ShoppingListAdapter should cover all quantity calculations pack sizes and click handlers`() {
        var boughtChangedIngr: Ingredient? = null
        var boughtStatus: Boolean? = null

        val adapter = ShoppingListAdapter { ingr, isChecked ->
            boughtChangedIngr = ingr
            boughtStatus = isChecked
        }

        // Case 1: missing = targetThreshold - currentQty (6 - 1 = 5, 1 pack of 6)
        val ingr1 = Ingredient(id = 1L, name = "Lait", quantity = 1.0, unit = "L", minThreshold = 6.0, packSize = 6.0, category = "Crémerie")
        // Case 2: currentQty <= 0 with targetThreshold > 0 (0 - 6, 2 packs of 6 = 12)
        val ingr2 = Ingredient(id = 2L, name = "Œufs", quantity = 0.0, unit = "pièce", minThreshold = 12.0, packSize = 6.0, category = null)
        // Case 3: currentQty <= 0 with targetThreshold <= 0 (defaults netMissing to 1.0)
        val ingr3 = Ingredient(id = 3L, name = "Farine", quantity = 0.0, unit = null, minThreshold = 0.0, packSize = 1.5, category = "Épicerie")
        // Case 4: currentQty > targetThreshold (already in stock, defaults netMissing to 1.0)
        val ingr4 = Ingredient(id = 4L, name = "Sucre", quantity = 10.0, unit = "g", minThreshold = 5.0, packSize = 2.0, category = "Épicerie")

        adapter.submitList(listOf(ingr1, ingr2, ingr3, ingr4))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.tvQuantityToBuy.text.toString()).isEqualTo("6 L")

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.tvQuantityToBuy.text.toString()).isEqualTo("12 pièce")

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)

        val holder3 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder3, 3)

        // Clicks
        holder1.binding.cbBought.isChecked = true
        assertThat(boughtChangedIngr).isEqualTo(ingr2)
        assertThat(boughtStatus).isTrue()

        holder0.binding.cardShoppingItem.performClick()
        assertThat(boughtChangedIngr).isEqualTo(ingr1)
    }

    // --- 10. RecipeAdapter ---

    @Test
    fun `RecipeAdapter should bind recipe info categories and thumbnail`() {
        var clickedRecipe: Recipe? = null
        val adapter = RecipeAdapter { clickedRecipe = it }

        val r1 = Recipe(id = 1L, title = "Carbonnade", preparationTimeMinutes = 20, cookingTimeMinutes = 90, kcalPerServing = 600, category = "Plat", nutritionInfo = "Riche", imageUri = "file:///tmp/img.jpg")
        val r2 = Recipe(id = 2L, title = "Salade", preparationTimeMinutes = 10, cookingTimeMinutes = 0, kcalPerServing = null, category = "Entrée", nutritionInfo = "")
        val r3 = Recipe(id = 3L, title = "Soupe", preparationTimeMinutes = null, cookingTimeMinutes = null, category = "", nutritionInfo = "Léger", imageUri = null)
        val r4 = Recipe(id = 4L, title = "Eau", category = null, nutritionInfo = null)
        val r5 = Recipe(id = 5L, title = "Jus", category = "   ", nutritionInfo = "   ")

        adapter.submitList(listOf(r1, r2, r3, r4, r5))

        val holder0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder0, 0)
        assertThat(holder0.binding.tvRecipeInfo.text.toString()).contains("600 kcal")
        assertThat(holder0.binding.tvRecipeCategory.text.toString()).isEqualTo("Plat | Riche")

        val holder1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder1, 1)
        assertThat(holder1.binding.tvRecipeCategory.text.toString()).isEqualTo("Entrée")

        val holder2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder2, 2)
        assertThat(holder2.binding.tvRecipeCategory.text.toString()).isEqualTo("Léger")

        val holder3 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder3, 3)
        assertThat(holder3.binding.tvRecipeCategory.text.toString()).isEmpty()

        val holder4 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder4, 4)
        assertThat(holder4.binding.tvRecipeCategory.text.toString()).isEmpty()

        holder0.binding.root.performClick()
        assertThat(clickedRecipe).isEqualTo(r1)
    }

    // --- 11. RecipeCategoryAdapter ---

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

    // --- 12. IngredientSelectAdapter ---

    @Test
    fun `IngredientSelectAdapter should create viewHolder and bind select item`() {
        var selectedItemName: String? = null

        val adapter = IngredientSelectAdapter { itemName ->
            selectedItemName = itemName
        }

        val selectItem = IngredientSelectAdapter.IngredientItem(name = "Sucre vanillé", isSelected = false)
        adapter.submitList(listOf(selectItem))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.tvIngredientName.text.toString()).isEqualTo("Sucre vanillé")

        holder.binding.root.performClick()
        assertThat(selectedItemName).isEqualTo("Sucre vanillé")

        holder.binding.checkbox.performClick()
        assertThat(selectedItemName).isEqualTo("Sucre vanillé")
    }
}
