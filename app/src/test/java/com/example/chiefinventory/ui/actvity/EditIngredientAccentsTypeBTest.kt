package com.example.chiefinventory.ui.actvity

import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.utils.IngredientParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la prise en charge intégrale des caractères accentués
 * (é, è, ê, à, ç, ù, ô, î, ë, ï, ü, œ, æ) dans tous les champs d'édition d'ingrédient.
 */
class EditIngredientAccentsTypeBTest {

    @Test
    fun `Ingredient model should preserve accented characters in name, unit, supplementalInfo and description`() {
        val accentedIngredient = Ingredient(
            name = "Échalotes & Chicons à l'ail",
            quantity = 2.0,
            unit = "boîte (500 g)",
            supplementalInfo = "égréné & haché fin",
            description = "Acheté à l'épicerie fine de la côte d'Opale"
        )

        assertThat(accentedIngredient.name).isEqualTo("Échalotes & Chicons à l'ail")
        assertThat(accentedIngredient.unit).isEqualTo("boîte (500 g)")
        assertThat(accentedIngredient.supplementalInfo).isEqualTo("égréné & haché fin")
        assertThat(accentedIngredient.description).isEqualTo("Acheté à l'épicerie fine de la côte d'Opale")
    }

    @Test
    fun `IngredientParser should preserve accents during string parsing`() {
        val rawInput = "2 boîtes d'échalotes hachées (500 g)"
        val parsed = IngredientParser.parse(rawInput, customUnits = listOf("boîte", "boîtes"))

        assertThat(parsed.name).contains("échalotes")
        assertThat(parsed.unit).isEqualTo("boîtes")
    }
}
