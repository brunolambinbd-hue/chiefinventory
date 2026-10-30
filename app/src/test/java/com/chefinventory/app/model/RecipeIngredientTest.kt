package com.chefinventory.app.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés pour la classe de données [RecipeIngredient].
 */
class RecipeIngredientTest {

    @Test
    fun `RecipeIngredient all properties getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val ri1 = RecipeIngredient(
            id = 1L,
            recipeId = 10L,
            ingredientName = "Beurre doux",
            quantityRequired = 100.0,
            unit = "g",
            supplementalInfo = "Doux 82% MG",
            locationId = 2L
        )

        val ri1Same = RecipeIngredient(
            id = 1L,
            recipeId = 10L,
            ingredientName = "Beurre doux",
            quantityRequired = 100.0,
            unit = "g",
            supplementalInfo = "Doux 82% MG",
            locationId = 2L
        )

        val riNulls = RecipeIngredient(
            id = 2L,
            recipeId = 20L,
            ingredientName = "Sel",
            quantityRequired = null,
            unit = null,
            supplementalInfo = null,
            locationId = null
        )

        // Default constructor parameters (lines 33 & 39)
        val riDefault = RecipeIngredient(recipeId = 30L, ingredientName = "Poivre")
        assertThat(riDefault.id).isEqualTo(0L)
        assertThat(riDefault.recipeId).isEqualTo(30L)
        assertThat(riDefault.ingredientName).isEqualTo("Poivre")
        assertThat(riDefault.quantityRequired).isNull()
        assertThat(riDefault.unit).isNull()
        assertThat(riDefault.supplementalInfo).isNull()
        assertThat(riDefault.locationId).isNull()

        // Getters
        assertThat(ri1.id).isEqualTo(1L)
        assertThat(ri1.recipeId).isEqualTo(10L)
        assertThat(ri1.ingredientName).isEqualTo("Beurre doux")
        assertThat(ri1.quantityRequired).isEqualTo(100.0)
        assertThat(ri1.unit).isEqualTo("g")
        assertThat(ri1.supplementalInfo).isEqualTo("Doux 82% MG")
        assertThat(ri1.locationId).isEqualTo(2L)

        // Destructuring / componentN (1 à 7)
        val (id, recipeId, name, qty, unit, info, locId) = ri1
        assertThat(id).isEqualTo(1L)
        assertThat(recipeId).isEqualTo(10L)
        assertThat(name).isEqualTo("Beurre doux")
        assertThat(qty).isEqualTo(100.0)
        assertThat(unit).isEqualTo("g")
        assertThat(info).isEqualTo("Doux 82% MG")
        assertThat(locId).isEqualTo(2L)

        // Equals
        assertThat(ri1.equals(ri1)).isTrue()
        assertThat(ri1.equals(null)).isFalse()
        assertThat(ri1.equals("NotARecipeIngredient")).isFalse()
        assertThat(ri1).isEqualTo(ri1Same)

        // Discrepancies on every parameter via copy
        assertThat(ri1).isNotEqualTo(ri1.copy(id = 99L))
        assertThat(ri1).isNotEqualTo(ri1.copy(recipeId = 99L))
        assertThat(ri1).isNotEqualTo(ri1.copy(ingredientName = "X"))
        assertThat(ri1).isNotEqualTo(ri1.copy(quantityRequired = null))
        assertThat(ri1).isNotEqualTo(ri1.copy(unit = null))
        assertThat(ri1).isNotEqualTo(ri1.copy(supplementalInfo = null))
        assertThat(ri1).isNotEqualTo(ri1.copy(locationId = null))

        // HashCode & ToString
        assertThat(ri1.hashCode()).isEqualTo(ri1Same.hashCode())
        assertThat(riNulls.hashCode()).isNotEqualTo(ri1.hashCode())
        assertThat(ri1.toString()).contains("Beurre doux")
    }
}
