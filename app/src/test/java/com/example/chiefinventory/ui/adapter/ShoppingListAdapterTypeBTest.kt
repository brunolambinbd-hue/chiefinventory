package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fichier de test spécifique pour [ShoppingListAdapter] et [ShoppingListAdapter.Companion]
 * assurant la couverture à 100% de toutes les branches.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShoppingListAdapterTypeBTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `calculateNetMissing helper function should cover all threshold and currentQty branches`() {
        // Branch 1: targetThreshold > currentQty
        assertThat(ShoppingListAdapter.calculateNetMissing(currentQty = 2.0, targetThreshold = 10.0)).isEqualTo(8.0)
        assertThat(ShoppingListAdapter.calculateNetMissing(currentQty = -1.0, targetThreshold = 6.0)).isEqualTo(7.0)

        // Branch 2: targetThreshold <= currentQty
        assertThat(ShoppingListAdapter.calculateNetMissing(currentQty = 0.0, targetThreshold = 0.0)).isEqualTo(1.0)
        assertThat(ShoppingListAdapter.calculateNetMissing(currentQty = 5.0, targetThreshold = 3.0)).isEqualTo(1.0)
    }

    @Test
    fun `formatPackInfo helper function should cover packSize and numPacks plural branches`() {
        // PackSize <= 1.0 -> returns empty string
        assertThat(ShoppingListAdapter.formatPackInfo(packSize = 1.0, numPacks = 1)).isEmpty()

        // PackSize > 1.0, numPacks = 1 -> " (1 pack de 6)"
        assertThat(ShoppingListAdapter.formatPackInfo(packSize = 6.0, numPacks = 1)).isEqualTo(" (1 pack de 6)")

        // PackSize > 1.0, numPacks > 1 -> " (2 packs de 6)"
        assertThat(ShoppingListAdapter.formatPackInfo(packSize = 6.0, numPacks = 2)).isEqualTo(" (2 packs de 6)")
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should reach 100 percent coverage`() {
        val ingr1 = Ingredient(id = 10L, name = "Lait", quantity = 0.0)
        val ingr1Same = Ingredient(id = 10L, name = "Lait", quantity = 0.0)
        val ingr2 = Ingredient(id = 20L, name = "Beurre", quantity = 100.0)

        assertThat(ShoppingListAdapter.DiffCallback.areItemsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(ShoppingListAdapter.DiffCallback.areItemsTheSame(ingr1, ingr2)).isFalse()
        assertThat(ShoppingListAdapter.DiffCallback.areContentsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(ShoppingListAdapter.DiffCallback.areContentsTheSame(ingr1, ingr2)).isFalse()
    }

    @Test
    fun `ShoppingListAdapter should bind items with unit category and click listeners`() {
        var changedItem: Ingredient? = null
        var isBoughtState: Boolean? = null

        val adapter = ShoppingListAdapter { ingr, bought ->
            changedItem = ingr
            isBoughtState = bought
        }

        val unbought = Ingredient(id = 1L, name = "Œufs", quantity = 0.0, unit = "pcs", minThreshold = 6.0, packSize = 6.0, category = null)
        val bought = Ingredient(id = 2L, name = "Sucre", quantity = 1.0, unit = null, minThreshold = null, packSize = 1.0, category = "Épicerie")

        adapter.submitList(listOf(unbought, bought))

        val h0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h0, 0)
        assertThat(h0.binding.tvQuantityToBuy.text.toString()).isEqualTo("6 pcs")
        assertThat(h0.binding.tvCategoryAndLocation.text.toString()).isEqualTo("Épicerie") // Null category fallback

        val h1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h1, 1)
        assertThat(h1.binding.tvQuantityToBuy.text.toString()).isEqualTo("1")

        // Clicks
        h0.binding.cbBought.isChecked = true
        assertThat(changedItem).isEqualTo(unbought)
        assertThat(isBoughtState).isTrue()

        h0.binding.cardShoppingItem.performClick()
        assertThat(changedItem).isEqualTo(unbought)
    }
}
