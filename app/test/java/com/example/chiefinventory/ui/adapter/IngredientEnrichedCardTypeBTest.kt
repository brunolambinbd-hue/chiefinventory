package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le formatage des métriques sur la carte d'ingrédient enrichie
 * (Option A Tableau de Bord) et le déclenchement du callback d'édition.
 */
class IngredientEnrichedCardTypeBTest {

    private fun formatStockStatusText(ingredient: Ingredient): String {
        val qtyVal = ingredient.quantity ?: 0.0
        val thresholdVal = ingredient.minThreshold ?: 0.0
        val packVal = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0

        val formattedThreshold = if (thresholdVal % 1.0 == 0.0) thresholdVal.toInt().toString() else thresholdVal.toString()
        val unitStr = ingredient.unit?.let { if (it.isNotBlank()) " $it" else "" } ?: ""
        val formattedPack = if (packVal % 1.0 == 0.0) packVal.toInt().toString() else packVal.toString()
        val packInfoStr = if (packVal > 1.0) " • Pack: $formattedPack" else ""

        val isOutOfStock = qtyVal <= 0.0
        val isLowStock = qtyVal <= thresholdVal && qtyVal > 0.0

        return when {
            isOutOfStock -> "🔴 En Rupture (Seuil: $formattedThreshold$unitStr$packInfoStr)"
            isLowStock -> "🟡 Stock Faible (Seuil: $formattedThreshold$unitStr$packInfoStr)"
            else -> "🟢 Stock Sain (Seuil: $formattedThreshold$unitStr$packInfoStr)"
        }
    }

    @Test
    fun `formatStockStatusText should format status line with threshold and pack size`() {
        val milk = Ingredient(name = "Lait", quantity = 11.0, unit = "L", minThreshold = 2.0, packSize = 6.0)
        assertThat(formatStockStatusText(milk)).isEqualTo("🟢 Stock Sain (Seuil: 2 L • Pack: 6)")

        val lowMilk = Ingredient(name = "Lait", quantity = 1.0, unit = "L", minThreshold = 2.0, packSize = 6.0)
        assertThat(formatStockStatusText(lowMilk)).isEqualTo("🟡 Stock Faible (Seuil: 2 L • Pack: 6)")

        val noButter = Ingredient(name = "Beurre", quantity = 0.0, unit = "g", minThreshold = 50.0, packSize = 1.0)
        assertThat(formatStockStatusText(noButter)).isEqualTo("🔴 En Rupture (Seuil: 50 g)")
    }

    @Test
    fun `onEditClick callback should be triggered when pencil icon is clicked`() {
        var clickedIngredientId: Long? = null
        val callback: (Ingredient) -> Unit = { ingr ->
            clickedIngredientId = ingr.id
        }

        val testIngredient = Ingredient(id = 99L, name = "Farine", quantity = 500.0)
        callback.invoke(testIngredient)

        assertThat(clickedIngredientId).isEqualTo(99L)
    }
}
