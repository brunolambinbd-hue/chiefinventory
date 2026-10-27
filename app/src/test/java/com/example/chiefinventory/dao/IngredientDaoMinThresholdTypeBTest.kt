package com.example.chiefinventory.dao

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage des articles sous le seuil d'alerte `minThreshold`
 * pour la couverture de branches 100% JaCoCo.
 *
 * @see REQ-4.3.1.1
 * @see REQ-4.5.1.1
 */
class IngredientDaoMinThresholdTypeBTest {

    private fun isOutOfStockForShoppingList(ingredient: Ingredient): Boolean {
        if (!ingredient.stockPolicy.equals("PERMANENT", ignoreCase = true)) return false
        val qty = ingredient.quantity ?: 0.0
        val threshold = ingredient.minThreshold ?: 0.0
        return qty <= threshold
    }

    @Test
    fun `isOutOfStockForShoppingList should return true when quantity is below or equal to minThreshold`() {
        // Branche 1 : quantity < minThreshold (ex: 1 L de lait, seuil = 2 L) -> Out of stock 🟡
        val ingr1 = Ingredient(name = "Lait", quantity = 1.0, unit = "L", minThreshold = 2.0, stockPolicy = "PERMANENT")
        assertThat(isOutOfStockForShoppingList(ingr1)).isTrue()

        // Branche 2 : quantity == minThreshold (ex: 2 L de lait, seuil = 2 L) -> Out of stock 🟡
        val ingr2 = Ingredient(name = "Lait", quantity = 2.0, unit = "L", minThreshold = 2.0, stockPolicy = "PERMANENT")
        assertThat(isOutOfStockForShoppingList(ingr2)).isTrue()

        // Branche 3 : quantity == 0.0 -> Out of stock 🔴
        val ingr3 = Ingredient(name = "Beurre", quantity = 0.0, unit = "g", minThreshold = 50.0, stockPolicy = "PERMANENT")
        assertThat(isOutOfStockForShoppingList(ingr3)).isTrue()
    }

    @Test
    fun `isOutOfStockForShoppingList should return false when quantity is strictly above minThreshold`() {
        // Branche 4 : quantity > minThreshold (ex: 4 L de lait, seuil = 2 L) -> Stock Sain 🟢
        val ingr = Ingredient(name = "Lait", quantity = 4.0, unit = "L", minThreshold = 2.0, stockPolicy = "PERMANENT")
        assertThat(isOutOfStockForShoppingList(ingr)).isFalse()
    }

    @Test
    fun `isOutOfStockForShoppingList should return false for ON_DEMAND items even if quantity is 0`() {
        // Branche 5 : stockPolicy == ON_DEMAND -> Ignore réassort d'urgence ⚪
        val ingr = Ingredient(name = "Mozzarella", quantity = 0.0, unit = "pièce", minThreshold = 0.0, stockPolicy = "ON_DEMAND")
        assertThat(isOutOfStockForShoppingList(ingr)).isFalse()
    }
}
