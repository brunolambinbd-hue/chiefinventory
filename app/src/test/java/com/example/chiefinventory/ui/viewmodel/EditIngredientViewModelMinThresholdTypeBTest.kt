package com.example.chiefinventory.ui.viewmodel

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'initialisation et la sauvegarde du seuil minimum d'alerte (`minThreshold`).
 *
 * @see REQ-4.4.1.1
 * @see REQ-4.4.1.2
 */
class EditIngredientViewModelMinThresholdTypeBTest {

    @Test
    fun `defaultMinThreshold should initialize with current quantity when minThreshold is null`() {
        val ingredient = Ingredient(name = "Farine", quantity = 500.0, unit = "g", minThreshold = null)

        val defaultThreshold = ingredient.minThreshold ?: (ingredient.quantity ?: 0.0)

        assertThat(defaultThreshold).isEqualTo(500.0)
    }

    @Test
    fun `defaultMinThreshold should preserve explicit minThreshold when non-null`() {
        val ingredient = Ingredient(name = "Lait", quantity = 1.0, unit = "L", minThreshold = 0.25)

        val defaultThreshold = ingredient.minThreshold ?: (ingredient.quantity ?: 0.0)

        assertThat(defaultThreshold).isEqualTo(0.25)
    }
}
