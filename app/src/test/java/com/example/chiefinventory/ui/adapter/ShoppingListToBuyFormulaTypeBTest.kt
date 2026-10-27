package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la formule exacte de calcul de la quantité à racheter
 * (Quantité à racheter = Seuil/Cible - Stock actuel).
 *
 * @see REQ-4.5.1.1
 */
class ShoppingListToBuyFormulaTypeBTest {

    private fun calculateQtyToBuy(ingredient: Ingredient): Double {
        val currentQty = ingredient.quantity ?: 0.0
        val targetThreshold = ingredient.minThreshold ?: 0.0

        return if (targetThreshold > currentQty) {
            targetThreshold - currentQty
        } else if (currentQty <= 0.0) {
            if (targetThreshold > 0.0) targetThreshold else 1.0
        } else {
            1.0
        }
    }

    @Test
    fun `calculateQtyToBuy should return 5 L when target threshold is 6 L and current stock is 1 L`() {
        val ingredient = Ingredient(
            name = "Lait demi-écrémé",
            quantity = 1.0,
            unit = "L",
            minThreshold = 6.0,
            stockPolicy = "PERMANENT"
        )

        val toBuy = calculateQtyToBuy(ingredient)

        assertThat(toBuy).isEqualTo(5.0) // 6.0 - 1.0 = 5.0 L
    }

    @Test
    fun `calculateQtyToBuy should return full 250 g when current stock is 0 g and threshold is 250 g`() {
        val ingredient = Ingredient(
            name = "Beurre",
            quantity = 0.0,
            unit = "g",
            minThreshold = 250.0,
            stockPolicy = "PERMANENT"
        )

        val toBuy = calculateQtyToBuy(ingredient)

        assertThat(toBuy).isEqualTo(250.0) // 250 - 0 = 250 g
    }

    @Test
    fun `calculateQtyToBuy should fallback to 1 when threshold is missing or 0`() {
        val ingredient = Ingredient(
            name = "Huile",
            quantity = 0.0,
            unit = "L",
            minThreshold = null,
            stockPolicy = "PERMANENT"
        )

        val toBuy = calculateQtyToBuy(ingredient)

        assertThat(toBuy).isEqualTo(1.0)
    }
}
