package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientParserOldTest {

    @Test
    fun `preClean should fix common OCR errors with fractions`() {
        assertThat(IngredientParser_old.preClean("112 litre d'eau")).isEqualTo("1/2 litre d'eau")
        assertThat(IngredientParser_old.preClean("114 de litre")).isEqualTo("1/4 de litre")
        assertThat(IngredientParser_old.preClean("W2 sel")).isEqualTo("1/2 sel")
        assertThat(IngredientParser_old.preClean("1 12 tomate")).isEqualTo("1/2 tomate")
    }

    @Test
    fun `preClean should fix OCR errors with spoons`() {
        assertThat(IngredientParser_old.preClean("lc à sope")).isEqualTo("1 c. à soupe")
        assertThat(IngredientParser_old.preClean("!c. à café")).isEqualTo("1 c. à café")
        assertThat(IngredientParser_old.preClean("càs")).isEqualTo("c. à s.")
        assertThat(IngredientParser_old.preClean("1c. a café")).isEqualTo("1 c. à café")
    }

    @Test
    fun `preClean should restore linguistic errors like d'eau`() {
        assertThat(IngredientParser_old.preClean("Ldeau")).isEqualTo("1 l d'eau")
        assertThat(IngredientParser_old.preClean("jauned' euf")).isEqualTo("jaune d'oeuf")
        assertThat(IngredientParser_old.preClean("lajout")).isEqualTo("1 ajout")
    }

    @Test
    fun `parse should extract exact standard quantities and units`() {
        val result = IngredientParser_old.parse("250 g de farine")
        assertThat(result.quantity).isEqualTo(250.0)
        assertThat(result.unit).isEqualTo("g")
        assertThat(result.name).isEqualTo("farine")
        assertThat(result.supplementalInfo).isNull()
    }

    @Test
    fun `parse should extract supplemental info in parentheses`() {
        val result = IngredientParser_old.parse("3 oeufs (gros calibre)")
        assertThat(result.quantity).isEqualTo(3.0)
        assertThat(result.unit).isNull()
        assertThat(result.name).isEqualTo("oeufs")
        assertThat(result.supplementalInfo).isEqualTo("gros calibre")
    }

    @Test
    fun `parse should handle fractions natively`() {
        val result = IngredientParser_old.parse("1/2 litre de lait")
        assertThat(result.quantity).isEqualTo(0.5)
        assertThat(result.unit).isEqualTo("litre")
        assertThat(result.name).isEqualTo("lait")
    }

    @Test
    fun `parse should handle ranges and pick the second quantity`() {
        val result = IngredientParser_old.parse("2 à 3 pincées de sel")
        assertThat(result.quantity).isEqualTo(3.0)
        assertThat(result.unit).isNull()
        assertThat(result.name).isEqualTo("pincées de sel")
    }

    @Test
    fun `parse should handle decimal quantities`() {
        val result = IngredientParser_old.parse("1.5 kg de pommes")
        assertThat(result.quantity).isEqualTo(1.5)
        assertThat(result.unit).isEqualTo("kg")
        assertThat(result.name).isEqualTo("pommes")
    }

    @Test
    fun `parse should fallback gracefully on unparseable inputs`() {
        val result = IngredientParser_old.parse("Une poignée de cerises")
        assertThat(result.quantity).isNull()
        assertThat(result.unit).isNull()
        assertThat(result.name).isEqualTo("Une poignée de cerises")
    }
}
