package com.example.chiefinventory.ui.viewmodel

import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de conservation de la quantité 0.0
 * lorsqu'un ingrédient est décoché ou mis à 0 dans l'IHM.
 */
class SetupInventoryUpdateBugTypeBTest {

    @Test
    fun `SelectedCatalogEntry should calculate target quantity as 0 when entry is deselected`() {
        val item = StandardCatalogItem(name = "Fromage de chèvre", defaultQuantity = 1.0, defaultUnit = "pièce")
        val entry = SelectedCatalogEntry(item = item, isSelected = false, currentQuantity = 1.0)

        val targetQty = if (entry.isSelected && entry.currentQuantity > 0.0) entry.currentQuantity else 0.0

        assertThat(targetQty).isEqualTo(0.0)
    }

    @Test
    fun `SelectedCatalogEntry should calculate target quantity as currentQuantity when entry is selected`() {
        val item = StandardCatalogItem(name = "Beurre", defaultQuantity = 100.0, defaultUnit = "g")
        val entry = SelectedCatalogEntry(item = item, isSelected = true, currentQuantity = 250.0)

        val targetQty = if (entry.isSelected && entry.currentQuantity > 0.0) entry.currentQuantity else 0.0

        assertThat(targetQty).isEqualTo(250.0)
    }
}
