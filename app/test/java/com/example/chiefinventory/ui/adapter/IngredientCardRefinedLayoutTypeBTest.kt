package com.example.chiefinventory.ui.adapter

import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'affichage épuré des cartes d'ingrédients sans répétition
 * et avec l'emplacement maison (Frigo, Placard, Surgélateur, Cave) et le rayon de magasin.
 */
class IngredientCardRefinedLayoutTypeBTest {

    private fun formatRefinedStatusText(ingredient: Ingredient): String {
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
            isOutOfStock -> "🚨 En Rupture (Seuil: $formattedThreshold$unitStr$packInfoStr)"
            isLowStock -> "⚠️ Stock Faible (Seuil: $formattedThreshold$unitStr$packInfoStr)"
            else -> "🎯 Seuil min : $formattedThreshold$unitStr$packInfoStr"
        }
    }

    private fun formatLocationLine(locationName: String?, categoryStr: String): String {
        val rawHomeLocName = locationName ?: "Frigo"
        val homeLocIcon = when {
            rawHomeLocName.contains("Frigo", ignoreCase = true) -> "❄️"
            rawHomeLocName.contains("Placard", ignoreCase = true) -> "🧺"
            rawHomeLocName.contains("Surgélateur", ignoreCase = true) || rawHomeLocName.contains("Congélateur", ignoreCase = true) -> "🧊"
            rawHomeLocName.contains("Cave", ignoreCase = true) -> "🍷"
            else -> "📍"
        }
        return "$homeLocIcon $rawHomeLocName  •  📍 $categoryStr"
    }

    @Test
    fun `formatRefinedStatusText should omit repetitive Stock Sain phrase for healthy stock`() {
        val milk = Ingredient(name = "Lait", quantity = 11.0, unit = "L", minThreshold = 2.0, packSize = 6.0)
        assertThat(formatRefinedStatusText(milk)).isEqualTo("🎯 Seuil min : 2 L • Pack: 6")

        val lowButter = Ingredient(name = "Beurre", quantity = 50.0, unit = "g", minThreshold = 100.0, packSize = 1.0)
        assertThat(formatRefinedStatusText(lowButter)).isEqualTo("⚠️ Stock Faible (Seuil: 100 g)")

        val noEggs = Ingredient(name = "Œufs", quantity = 0.0, unit = "pièce", minThreshold = 6.0, packSize = 12.0)
        assertThat(formatRefinedStatusText(noEggs)).isEqualTo("🚨 En Rupture (Seuil: 6 pièce • Pack: 12)")
    }

    @Test
    fun `formatLocationLine should combine home location icon and store aisle category`() {
        val locLine = formatLocationLine("Frigo", "Crémerie")
        assertThat(locLine).isEqualTo("❄️ Frigo  •  📍 Crémerie")

        val locLinePlacard = formatLocationLine("Placard", "Féculents")
        assertThat(locLinePlacard).isEqualTo("🧺 Placard  •  📍 Féculents")
    }
}
