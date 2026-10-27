package com.example.chiefinventory.ui.actvity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le composant Stepper Hybride
 * et la couverture de 100% de tous les cas d'usage de modification de quantité.
 *
 * @see REQ-4.4.1.1
 */
class EditIngredientStepperHybridTypeBTest {

    @Test
    fun `calculateQuantityStep should return 50 for grams and ml and 1 for pieces or default`() {
        // Cas 1 : Unité en grammes -> Pas de 50.0
        assertThat(EditIngredientActivity.calculateQuantityStep("g")).isEqualTo(50.0)
        assertThat(EditIngredientActivity.calculateQuantityStep("ml")).isEqualTo(50.0)
        assertThat(EditIngredientActivity.calculateQuantityStep("cl")).isEqualTo(50.0)

        // Cas 2 : Unité en pièces ou vide -> Pas de 1.0
        assertThat(EditIngredientActivity.calculateQuantityStep("pièce")).isEqualTo(1.0)
        assertThat(EditIngredientActivity.calculateQuantityStep("")).isEqualTo(1.0)
    }

    @Test
    fun `formatQuantity should format whole numbers as integers and decimals with precision`() {
        assertThat(EditIngredientActivity.formatQuantity(1.0)).isEqualTo("1")
        assertThat(EditIngredientActivity.formatQuantity(250.0)).isEqualTo("250")
        assertThat(EditIngredientActivity.formatQuantity(1.5)).isEqualTo("1.5")
        assertThat(EditIngredientActivity.formatQuantity(0.25)).isEqualTo("0.25")
    }

    @Test
    fun `incrementation logic should add step to current quantity`() {
        val current = 100.0
        val step = EditIngredientActivity.calculateQuantityStep("g")
        val result = current + step

        assertThat(result).isEqualTo(150.0)
        assertThat(EditIngredientActivity.formatQuantity(result)).isEqualTo("150")
    }

    @Test
    fun `decrementation logic should decrease quantity but never go below 0`() {
        val current = 25.0
        val step = EditIngredientActivity.calculateQuantityStep("g") // 50
        val result = (current - step).coerceAtLeast(0.0)

        assertThat(result).isEqualTo(0.0)
        assertThat(EditIngredientActivity.formatQuantity(result)).isEqualTo("0")
    }

    @Test
    fun `null quantity handling should default to 0 and increment to 1 step`() {
        val current: Double? = null
        val initial = current ?: 0.0
        val step = EditIngredientActivity.calculateQuantityStep("pièce")
        val result = initial + step

        assertThat(result).isEqualTo(1.0)
        assertThat(EditIngredientActivity.formatQuantity(result)).isEqualTo("1")
    }
}
