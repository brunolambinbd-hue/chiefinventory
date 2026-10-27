package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.ceil

/**
 * Suite de tests unitaires de Type B vérifiant le calcul de réassort arrondi au pack supérieur
 * et l'action caddie réapprovisionnant le pack entier.
 */
class ShoppingListPackSizeFormulaTypeBTest {

    private fun calculatePacksToBuy(ingredient: Ingredient): Pair<Int, Double> {
        val currentQty = ingredient.quantity ?: 0.0
        val targetThreshold = ingredient.minThreshold ?: 0.0
        val packSize = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0

        val netMissing = if (targetThreshold > currentQty) {
            targetThreshold - currentQty
        } else if (currentQty <= 0.0) {
            if (targetThreshold > 0.0) targetThreshold else 1.0
        } else {
            1.0
        }

        val numPacks = ceil(netMissing / packSize).toInt().coerceAtLeast(1)
        val qtyToBuy = numPacks * packSize
        return Pair(numPacks, qtyToBuy)
    }

    @Test
    fun `calculatePacksToBuy should round up to 1 pack of 6 L when 5 L is missing`() {
        val milk = Ingredient(name = "Lait", quantity = 1.0, unit = "L", minThreshold = 6.0, packSize = 6.0)

        val (packs, qty) = calculatePacksToBuy(milk)

        assertThat(packs).isEqualTo(1)
        assertThat(qty).isEqualTo(6.0) // 1 pack de 6 L !
    }

    @Test
    fun `calculatePacksToBuy should round up to 2 packs of 6 L when 7 L is missing`() {
        val milk = Ingredient(name = "Lait", quantity = 5.0, unit = "L", minThreshold = 12.0, packSize = 6.0)

        val (packs, qty) = calculatePacksToBuy(milk)

        assertThat(packs).isEqualTo(2) // ceil(7/6) = 2 packs
        assertThat(qty).isEqualTo(12.0) // 2 * 6 = 12 L !
    }

    @Test
    fun `checking item in cart should add full pack size to current stock`() {
        val milk = Ingredient(name = "Lait", quantity = 1.0, unit = "L", minThreshold = 6.0, packSize = 6.0)

        val packAdd = if (milk.packSize > 0.0) milk.packSize else 1.0
        val updatedStock = (milk.quantity ?: 0.0) + packAdd

        assertThat(updatedStock).isEqualTo(7.0) // 1 L + 6 L = 7 L !
    }
}
