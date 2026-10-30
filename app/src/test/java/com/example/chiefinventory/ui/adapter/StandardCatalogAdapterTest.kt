package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.StandardCatalogItem
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [StandardCatalogAdapter], [SelectedCatalogEntry] et [CatalogStockState].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StandardCatalogAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `SelectedCatalogEntry getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val item = StandardCatalogItem(name = "Lait", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie")
        val entry1 = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 2.0)
        val entry1Same = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 2.0)

        // Getters
        assertThat(entry1.item).isEqualTo(item)
        assertThat(entry1.isSelected).isTrue()
        assertThat(entry1.currentQuantity).isEqualTo(2.0)

        // Destructuring / componentN
        val (it, sel, qty) = entry1
        assertThat(it).isEqualTo(item)
        assertThat(sel).isTrue()
        assertThat(qty).isEqualTo(2.0)

        // Equals
        assertThat(entry1.equals(entry1)).isTrue()
        assertThat(entry1.equals(null)).isFalse()
        assertThat(entry1.equals("NotASelectedCatalogEntry")).isFalse()
        assertThat(entry1).isEqualTo(entry1Same)

        // Copy discrepancies
        val item2 = StandardCatalogItem(name = "Beurre")
        assertThat(entry1).isNotEqualTo(entry1.copy(item = item2))
        assertThat(entry1).isNotEqualTo(entry1.copy(isSelected = false))
        assertThat(entry1).isNotEqualTo(entry1.copy(currentQuantity = 99.0))

        // HashCode & ToString
        assertThat(entry1.hashCode()).isEqualTo(entry1Same.hashCode())
        assertThat(entry1.toString()).contains("Lait")
    }

    @Test
    fun `CatalogStockState enum entries should be covered`() {
        val entries = CatalogStockState.entries
        assertThat(entries).containsExactly(
            CatalogStockState.OUT_OF_STOCK,
            CatalogStockState.LOW_STOCK,
            CatalogStockState.HEALTHY_STOCK
        )

        assertThat(CatalogStockState.valueOf("OUT_OF_STOCK")).isEqualTo(CatalogStockState.OUT_OF_STOCK)
        assertThat(CatalogStockState.valueOf("LOW_STOCK")).isEqualTo(CatalogStockState.LOW_STOCK)
        assertThat(CatalogStockState.valueOf("HEALTHY_STOCK")).isEqualTo(CatalogStockState.HEALTHY_STOCK)
    }

    @Test
    fun `StandardCatalogAdapter should bind HEALTHY_STOCK LOW_STOCK and OUT_OF_STOCK entries`() {
        var editedItem: StandardCatalogItem? = null

        val item = StandardCatalogItem(
            name = "Beurre doux",
            defaultQuantity = 200.0,
            defaultUnit = "g",
            category = "Crémerie",
            defaultLocationName = "Frigo",
            minThreshold = 100.0
        )

        // 1. HEALTHY_STOCK
        val entryHealthy = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 200.0)
        val adapterHealthy = StandardCatalogAdapter(onSelectionChanged = {}, onEditIngredientClicked = { editedItem = it })
        adapterHealthy.submitList(listOf(entryHealthy)) {
            val vh0 = adapterHealthy.onCreateViewHolder(parentContainer, 0)
            adapterHealthy.onBindViewHolder(vh0, 0)

            assertThat(vh0.binding.tvIngredientName.text.toString()).isEqualTo("Beurre doux")
            assertThat(vh0.binding.tvCategoryLocation.text.toString()).contains("Crémerie")
            assertThat(vh0.binding.tvCategoryLocation.text.toString()).contains("Frigo")
            assertThat(vh0.binding.layoutQuantityStepper.visibility).isEqualTo(View.VISIBLE)

            vh0.binding.btnEditIngredient.performClick()
            assertThat(editedItem).isEqualTo(item)
        }

        // 2. LOW_STOCK
        val entryLow = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 50.0)
        val adapterLow = StandardCatalogAdapter(onSelectionChanged = {})
        adapterLow.submitList(listOf(entryLow)) {
            val vh1 = adapterLow.onCreateViewHolder(parentContainer, 0)
            adapterLow.onBindViewHolder(vh1, 0)
            assertThat(vh1.binding.layoutQuantityStepper.visibility).isEqualTo(View.VISIBLE)
        }

        // 3. OUT_OF_STOCK
        val entryOut = SelectedCatalogEntry(item = item, isSelected = false, currentQuantity = 0.0)
        val adapterOut = StandardCatalogAdapter(onSelectionChanged = {})
        adapterOut.submitList(listOf(entryOut)) {
            val vh2 = adapterOut.onCreateViewHolder(parentContainer, 0)
            adapterOut.onBindViewHolder(vh2, 0)
            assertThat(vh2.binding.layoutQuantityStepper.visibility).isEqualTo(View.GONE)
        }
    }

    @Test
    fun `StandardCatalogAdapter card click increment and decrement buttons should update quantity and selection`() {
        val adapter = StandardCatalogAdapter(onSelectionChanged = {})

        val item = StandardCatalogItem(name = "Lait", defaultQuantity = 2.0, defaultUnit = "L")
        val entry = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 2.0)

        adapter.submitList(listOf(entry)) {
            val vh = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh, 0)

            // Click cardChip -> toggles selection
            vh.binding.cardChip.performClick()
            assertThat(entry.isSelected).isFalse()

            // Click increment -> increases quantity
            vh.binding.btnIncrement.performClick()
            assertThat(entry.currentQuantity).isEqualTo(3.0)

            // Click decrement when quantity > 1.0 -> decreases quantity
            vh.binding.btnDecrement.performClick()
            assertThat(entry.currentQuantity).isEqualTo(2.0)

            // Click decrement when quantity = 1.0 -> sets isSelected = false
            entry.currentQuantity = 1.0
            vh.binding.btnDecrement.performClick()
            assertThat(entry.isSelected).isFalse()
        }
    }

    @Test
    fun `StandardCatalogAdapter null edit listener and blank unit should bind gracefully`() {
        val adapterNoEdit = StandardCatalogAdapter(onSelectionChanged = {}, onEditIngredientClicked = null)
        val itemBlankUnit = StandardCatalogItem(name = "Œufs", defaultUnit = "")
        val entry = SelectedCatalogEntry(item = itemBlankUnit, isSelected = true, currentQuantity = 6.0)

        adapterNoEdit.submitList(listOf(entry)) {
            val vh = adapterNoEdit.onCreateViewHolder(parentContainer, 0)
            adapterNoEdit.onBindViewHolder(vh, 0)

            assertThat(vh.binding.tvQuantity.text.toString()).isEqualTo("6")
        }
    }

    @Test
    fun `getSelectionCount and getSelectedEntries should return selected entries`() {
        val adapter = StandardCatalogAdapter(onSelectionChanged = {})
        val item1 = StandardCatalogItem(name = "Lait")
        val item2 = StandardCatalogItem(name = "Beurre")

        val entry1 = SelectedCatalogEntry(item = item1, isSelected = true)
        val entry2 = SelectedCatalogEntry(item = item2, isSelected = false)

        adapter.submitList(listOf(entry1, entry2)) {
            assertThat(adapter.getSelectionCount()).isEqualTo(1)
            assertThat(adapter.getSelectedEntries()).containsExactly(entry1)
        }
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should compare entries correctly`() {
        val diffCallback = StandardCatalogAdapter.DiffCallback
        val item1 = StandardCatalogItem(name = "Lait")
        val item2 = StandardCatalogItem(name = "Beurre")

        val entry1 = SelectedCatalogEntry(item = item1, isSelected = true)
        val entry1Same = SelectedCatalogEntry(item = item1, isSelected = true)
        val entry2 = SelectedCatalogEntry(item = item2, isSelected = false)

        assertThat(diffCallback.areItemsTheSame(entry1, entry1Same)).isTrue()
        assertThat(diffCallback.areItemsTheSame(entry1, entry2)).isFalse()
        assertThat(diffCallback.areContentsTheSame(entry1, entry1Same)).isTrue()
        assertThat(diffCallback.areContentsTheSame(entry1, entry2)).isFalse()
    }
}
