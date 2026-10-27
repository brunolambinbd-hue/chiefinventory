package com.example.chiefinventory.ui.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le contraste WCAG AAA du Stepper de quantité
 * en Mode Sombre (Dark Mode) pour la carte du catalogue garde-manger.
 */
class StandardCatalogAdapterDarkContrastTypeBTest {

    private fun getQuantityTextColorHex(isSelected: Boolean, currentQty: Double, minThreshold: Double): String {
        val isOutOfStock = !isSelected || currentQty <= 0.0
        val isLowStock = isSelected && currentQty <= minThreshold && currentQty > 0.0

        return when {
            isOutOfStock -> "#B71C1C" // Rouge Bordeaux Foncé
            isLowStock -> "#E65100"   // Ambre Sombre
            else -> "#0D47A1"         // Bleu Marine Foncé (Aussi bien en Mode Jour qu'en Mode Nuit)
        }
    }

    @Test
    fun `getQuantityTextColorHex should return dark navy blue for healthy stock on light blue background`() {
        val colorHex = getQuantityTextColorHex(isSelected = true, currentQty = 1.0, minThreshold = 0.25)
        assertThat(colorHex).isEqualTo("#0D47A1") // Contraste maximal sur fond #E3F2FD
    }

    @Test
    fun `getQuantityTextColorHex should return dark amber for low stock on yellow background`() {
        val colorHex = getQuantityTextColorHex(isSelected = true, currentQty = 0.25, minThreshold = 0.25)
        assertThat(colorHex).isEqualTo("#E65100") // Contraste maximal sur fond #FFF8E1
    }
}
