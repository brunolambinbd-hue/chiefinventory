package com.example.chiefinventory.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la conservation du champ `packSize` (conditionnement d'achat).
 */
class IngredientPackSizeDatabaseTypeBTest {

    @Test
    fun `Ingredient model should preserve packSize attribute with default 1`() {
        val ingredientDefault = Ingredient(name = "Beurre", quantity = 250.0, unit = "g")
        assertThat(ingredientDefault.packSize).isEqualTo(1.0)

        val ingredientMilkPack = Ingredient(name = "Lait demi-écrémé", quantity = 1.0, unit = "L", packSize = 6.0)
        assertThat(ingredientMilkPack.packSize).isEqualTo(6.0)
    }
}
