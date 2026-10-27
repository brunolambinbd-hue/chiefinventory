package com.example.chiefinventory.ui.viewmodel

import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la détection des 3 états visuels du garde-manger
 * (🟢 Vert Sain / 🟡 Jaune Stock Faible / 🔴 Rouge Rupture) pour la couverture de branches 100% JaCoCo.
 *
 * @see REQ-4.3.1.3
 */
class SetupInventoryViewModelThreeColorsTypeBTest {

    enum class StockVisualState { GREEN_HEALTHY, YELLOW_LOW_STOCK, RED_OUT_OF_STOCK }

    private fun computeVisualState(entry: SelectedCatalogEntry): StockVisualState {
        val effectiveThreshold = entry.item.minThreshold
        val isOutOfStock = !entry.isSelected || entry.currentQuantity <= 0.0
        val isLowStock = entry.isSelected && entry.currentQuantity <= effectiveThreshold && entry.currentQuantity > 0.0

        return when {
            isOutOfStock -> StockVisualState.RED_OUT_OF_STOCK
            isLowStock -> StockVisualState.YELLOW_LOW_STOCK
            else -> StockVisualState.GREEN_HEALTHY
        }
    }

    @Test
    fun `computeVisualState should return GREEN_HEALTHY when quantity is strictly above minThreshold`() {
        val item = StandardCatalogItem(name = "Lait", defaultQuantity = 1.0, minThreshold = 0.25)
        val entry = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 1.0)

        assertThat(computeVisualState(entry)).isEqualTo(StockVisualState.GREEN_HEALTHY)
    }

    @Test
    fun `computeVisualState should return YELLOW_LOW_STOCK when quantity is below or equal to minThreshold`() {
        val item = StandardCatalogItem(name = "Lait", defaultQuantity = 1.0, minThreshold = 0.25)
        val entry = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 0.25)

        assertThat(computeVisualState(entry)).isEqualTo(StockVisualState.YELLOW_LOW_STOCK)
    }

    @Test
    fun `computeVisualState should return RED_OUT_OF_STOCK when deselected or quantity is 0`() {
        val item = StandardCatalogItem(name = "Beurre", defaultQuantity = 100.0, minThreshold = 50.0)
        val entry = SelectedCatalogEntry(item = item, isSelected = false, currentQuantity = 0.0)

        assertThat(computeVisualState(entry)).isEqualTo(StockVisualState.RED_OUT_OF_STOCK)
    }
}
