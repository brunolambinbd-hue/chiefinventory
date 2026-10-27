package com.example.chiefinventory.ui.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le nouveau format de liste de courses "Quantité d'Abord"
 * (ex: "5 L — Lait demi-écrémé").
 *
 * @see REQ-4.5.1.2
 */
class ShoppingListNewFormatTypeBTest {

    private fun formatShoppingItemLabel(qtyToBuy: Double, unit: String?, name: String): String {
        val formattedQty = if (qtyToBuy % 1.0 == 0.0) qtyToBuy.toInt().toString() else qtyToBuy.toString()
        val unitStr = unit?.let { if (it.isNotBlank()) " $it" else "" } ?: ""
        return "$formattedQty$unitStr — $name"
    }

    @Test
    fun `formatShoppingItemLabel should place quantity and unit first followed by ingredient name`() {
        assertThat(formatShoppingItemLabel(5.0, "L", "Lait demi-écrémé")).isEqualTo("5 L — Lait demi-écrémé")
        assertThat(formatShoppingItemLabel(250.0, "g", "Beurre doux")).isEqualTo("250 g — Beurre doux")
        assertThat(formatShoppingItemLabel(6.0, "pièce", "Œufs")).isEqualTo("6 pièce — Œufs")
        assertThat(formatShoppingItemLabel(1.0, null, "Sirop de Liège")).isEqualTo("1 — Sirop de Liège")
    }
}
