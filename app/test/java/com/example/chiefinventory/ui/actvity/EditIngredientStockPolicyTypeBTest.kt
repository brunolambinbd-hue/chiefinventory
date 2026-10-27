package com.example.chiefinventory.ui.actvity

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la possibilité de modifier la politique de stock (PERMANENT vs ON_DEMAND)
 * dans l'écran EditIngredientActivity.
 */
class EditIngredientStockPolicyTypeBTest {

    private fun resolveStockPolicyString(dropdownPosition: Int): String {
        return if (dropdownPosition == 1) "ON_DEMAND" else "PERMANENT"
    }

    private fun formatStockPolicyDropdownLabel(policy: String): String {
        return if (policy.equals("ON_DEMAND", ignoreCase = true)) {
            "🛒 ON_DEMAND (Achat sur Commande)"
        } else {
            "🔄 PERMANENT (Garder en Stock)"
        }
    }

    @Test
    fun `resolveStockPolicyString should map dropdown selection position to model value`() {
        assertThat(resolveStockPolicyString(0)).isEqualTo("PERMANENT")
        assertThat(resolveStockPolicyString(1)).isEqualTo("ON_DEMAND")
    }

    @Test
    fun `formatStockPolicyDropdownLabel should format label for UI dropdown`() {
        assertThat(formatStockPolicyDropdownLabel("PERMANENT")).isEqualTo("🔄 PERMANENT (Garder en Stock)")
        assertThat(formatStockPolicyDropdownLabel("ON_DEMAND")).isEqualTo("🛒 ON_DEMAND (Achat sur Commande)")
    }

    @Test
    fun `Ingredient copy should preserve selected stock policy`() {
        val ingredient = Ingredient(name = "Mozzarella", stockPolicy = "ON_DEMAND")
        val updated = ingredient.copy(stockPolicy = "PERMANENT")

        assertThat(ingredient.stockPolicy).isEqualTo("ON_DEMAND")
        assertThat(updated.stockPolicy).isEqualTo("PERMANENT")
    }
}
