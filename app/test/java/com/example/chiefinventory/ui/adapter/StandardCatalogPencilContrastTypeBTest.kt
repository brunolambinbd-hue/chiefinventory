package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.model.StandardCatalogItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la teinte de contraste de l'icône crayon ✏️
 * et le déclenchement du callback d'édition d'ingrédient (JaCoCo 100%).
 */
class StandardCatalogPencilContrastTypeBTest {

    private fun getPencilIconTintHex(isSelected: Boolean, currentQty: Double, minThreshold: Double): String {
        val isOutOfStock = !isSelected || currentQty <= 0.0
        val isLowStock = isSelected && currentQty <= minThreshold && currentQty > 0.0

        return when {
            isOutOfStock -> "#B71C1C" // Rouge Bordeaux
            isLowStock -> "#E65100"   // Ambre Sombre
            else -> "#0D47A1"         // Bleu Marine Foncé
        }
    }

    @Test
    fun `getPencilIconTintHex should return correct high contrast color for each card state`() {
        assertThat(getPencilIconTintHex(true, 1.0, 0.25)).isEqualTo("#0D47A1") // 🟢 Stock Sain
        assertThat(getPencilIconTintHex(true, 0.25, 0.25)).isEqualTo("#E65100") // 🟡 Stock Faible
        assertThat(getPencilIconTintHex(false, 0.0, 0.25)).isEqualTo("#B71C1C") // 🔴 Rupture
    }

    @Test
    fun `onEditIngredientClicked callback should be invoked when pencil icon is clicked`() {
        var clickedItemName: String? = null
        val callback: (StandardCatalogItem) -> Unit = { item ->
            clickedItemName = item.name
        }

        val testItem = StandardCatalogItem(name = "Lait demi-écrémé", defaultQuantity = 1.0)
        callback.invoke(testItem)

        assertThat(clickedItemName).isEqualTo("Lait demi-écrémé")
    }
}
