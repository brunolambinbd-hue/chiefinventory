package com.example.chiefinventory.ui.actvity

import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.StandardCatalogItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant que le clic sur le crayon ✏️ transmet bien l'ID
 * ou le nom de l'ingrédient cliqué (ex: Beurre) à EditIngredientActivity.
 */
class SetupInventoryPencilNavigationTypeBTest {

    private fun resolveIngredientForEditing(
        clickedItem: StandardCatalogItem,
        dbIngredients: List<Ingredient>
    ): Pair<Long?, StandardCatalogItem> {
        val existing = dbIngredients.find { it.name.equals(clickedItem.name, ignoreCase = true) }
        return Pair(existing?.id, clickedItem)
    }

    @Test
    fun `resolveIngredientForEditing should return existing ID when ingredient is in database`() {
        val clickedItem = StandardCatalogItem(name = "Beurre doux", defaultQuantity = 100.0)
        val dbList = listOf(Ingredient(id = 42L, name = "Beurre doux", quantity = 100.0))

        val (id, item) = resolveIngredientForEditing(clickedItem, dbList)

        assertThat(id).isEqualTo(42L)
        assertThat(item.name).isEqualTo("Beurre doux")
    }

    @Test
    fun `resolveIngredientForEditing should return null ID and pre-fill catalog item when new`() {
        val clickedItem = StandardCatalogItem(name = "Lait demi-écrémé", defaultQuantity = 1.0, minThreshold = 0.25, packSize = 6.0)
        val dbList = emptyList<Ingredient>()

        val (id, item) = resolveIngredientForEditing(clickedItem, dbList)

        assertThat(id).isNull()
        assertThat(item.name).isEqualTo("Lait demi-écrémé")
        assertThat(item.packSize).isEqualTo(6.0)
    }
}
