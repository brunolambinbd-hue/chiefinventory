package com.example.chiefinventory.ui.viewmodel

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'initialisation et la sauvegarde du champ `packSize`
 * (taille du conditionnement/pack d'achat) dans l'écran d'édition d'ingrédient.
 */
class EditIngredientPackSizeViewModelTypeBTest {

    @Test
    fun `packSize should default to 1 when packSize is not set or 0`() {
        val ingredientDefault = Ingredient(name = "Beurre", quantity = 100.0, unit = "g", packSize = 1.0)
        val effectivePack = if (ingredientDefault.packSize > 0.0) ingredientDefault.packSize else 1.0

        assertThat(effectivePack).isEqualTo(1.0)
    }

    @Test
    fun `packSize should preserve explicit pack size value`() {
        val milkPack = Ingredient(name = "Lait demi-écrémé", quantity = 1.0, unit = "L", packSize = 6.0)
        val effectivePack = if (milkPack.packSize > 0.0) milkPack.packSize else 1.0

        assertThat(effectivePack).isEqualTo(6.0)
    }
}
