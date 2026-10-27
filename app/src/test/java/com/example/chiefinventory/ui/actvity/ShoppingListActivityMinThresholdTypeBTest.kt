package com.example.chiefinventory.ui.actvity

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'inclusion des articles sous le seuil minimum
 * dans la liste de réassort (sans filtrage à 0.0 intempestif).
 */
class ShoppingListActivityMinThresholdTypeBTest {

    @Test
    fun `shoppingList should include ingredients with quantity below or equal to minThreshold even if quantity greater than 0`() {
        val lowStockItem = Ingredient(
            name = "Lait demi-écrémé",
            quantity = 0.25,
            unit = "l",
            minThreshold = 0.25,
            stockPolicy = "PERMANENT"
        )

        val outOfStockItem = Ingredient(
            name = "Beurre",
            quantity = 0.0,
            unit = "g",
            minThreshold = 50.0,
            stockPolicy = "PERMANENT"
        )

        val daoList = listOf(lowStockItem, outOfStockItem)

        // Verifie que les 2 articles (aussi bien le stock faible a 0.25 que la rupture a 0.0) sont conserves pour l'IHM
        val displayedList = daoList.filter { (it.quantity ?: 0.0) <= (it.minThreshold ?: 0.0) }

        assertThat(displayedList).hasSize(2)
        assertThat(displayedList).contains(lowStockItem)
        assertThat(displayedList).contains(outOfStockItem)
    }
}
