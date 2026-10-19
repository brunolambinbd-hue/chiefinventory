package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.IngredientParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientParserRuleTest {

    @Test
    fun `parse should extract standard quantity, unit and name`() {
        val result = IngredientParser.parse("200 g de farine")
        assertThat(result.quantity).isEqualTo(200.0)
        assertThat(result.unit).isEqualTo("g")
        assertThat(result.name).isEqualTo("farine")
    }

    @Test
    fun `parse should extract parenthetical information`() {
        val result = IngredientParser.parse("3 oeufs (gros calibre)")
        assertThat(result.quantity).isEqualTo(3.0)
        assertThat(result.unit).isNull()
        assertThat(result.name).isEqualTo("oeufs")
        assertThat(result.supplementalInfo).isEqualTo("gros calibre")
    }

    @Test
    fun `parse should handle fractions`() {
        val result = IngredientParser.parse("1/2 litre de lait")
        assertThat(result.quantity).isEqualTo(0.5)
        assertThat(result.unit).isEqualTo("litre")
        assertThat(result.name).isEqualTo("lait")
    }
}
