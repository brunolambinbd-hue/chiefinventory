package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.StandardCatalogLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant le parsing et le chargement du catalogue d'ingrédients types
 * d'après la provenance Open Food Facts & ANSES CIQUAL.
 */
class StandardCatalogLoaderRuleTest {

    @Test
    fun `parseRawCatalogLine should parse pipe separated line into StandardCatalogItem`() {
        val raw = "Chicons / Endives|1|kg|Légumes frais"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.name).isEqualTo("Chicons / Endives")
        assertThat(item.defaultQuantity).isEqualTo(1.0)
        assertThat(item.defaultUnit).isEqualTo("kg")
        assertThat(item.category).isEqualTo("Légumes frais")
        assertThat(item.defaultLocationName).isEqualTo("Frigo")
    }

    @Test
    fun `parseRawCatalogLine should handle missing optional fields with defaults`() {
        val raw = "Beurre doux"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.name).isEqualTo("Beurre doux")
        assertThat(item.defaultQuantity).isEqualTo(1.0)
        assertThat(item.defaultUnit).isNull()
        assertThat(item.category).isEqualTo("Épicerie")
        assertThat(item.defaultLocationName).isEqualTo("Frigo")
    }
}
