package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R
import com.chefinventory.app.model.StockPolicy
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

/**
 * Suite de tests unitaires de Type B vérifiant le parsing et le chargement du catalogue d'ingrédients types par [StandardCatalogLoader].
 */
class StandardCatalogLoaderTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseRawCatalogLine_DefaultLocationOverloadAndFormats() {
        // 1. Default parameter overload (defaultLocation = "Placard")
        val item1 = StandardCatalogLoader.parseRawCatalogLine("Riz|1|kg|Épicerie")
        assertThat(item1).isNotNull()
        assertThat(item1?.name).isEqualTo("Riz")
        assertThat(item1?.defaultQuantity).isEqualTo(1.0)
        assertThat(item1?.defaultUnit).isEqualTo("kg")
        assertThat(item1?.category).isEqualTo("Épicerie")
        assertThat(item1?.defaultLocationName).isEqualTo("Placard")
        assertThat(item1?.stockPolicy).isEqualTo(StockPolicy.PERMANENT)
        assertThat(item1?.minThreshold).isEqualTo(0.5)
        assertThat(item1?.packSize).isEqualTo(1.0)

        // 2. Full 7-field format with custom location and ON_DEMAND policy
        val item2 = StandardCatalogLoader.parseRawCatalogLine(
            "Beurre|250|g|Crémerie|ON_DEMAND|100|1",
            "Frigo"
        )
        assertThat(item2).isNotNull()
        assertThat(item2?.name).isEqualTo("Beurre")
        assertThat(item2?.defaultQuantity).isEqualTo(250.0)
        assertThat(item2?.defaultUnit).isEqualTo("g")
        assertThat(item2?.category).isEqualTo("Crémerie")
        assertThat(item2?.defaultLocationName).isEqualTo("Frigo")
        assertThat(item2?.stockPolicy).isEqualTo(StockPolicy.ON_DEMAND)
        assertThat(item2?.minThreshold).isEqualTo(100.0)
        assertThat(item2?.packSize).isEqualTo(1.0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseRawCatalogLine_SinglePartAndBlankDefaults() {
        // 1. Name only line
        val item1 = StandardCatalogLoader.parseRawCatalogLine("Lait d'avoine")
        assertThat(item1).isNotNull()
        assertThat(item1?.name).isEqualTo("Lait d'avoine")
        assertThat(item1?.defaultQuantity).isEqualTo(1.0)
        assertThat(item1?.defaultUnit).isNull()
        assertThat(item1?.category).isEqualTo("Épicerie")

        // 2. Line with blank quantity and blank unit
        val item2 = StandardCatalogLoader.parseRawCatalogLine("Sel|invalid|   |   ")
        assertThat(item2).isNotNull()
        assertThat(item2?.name).isEqualTo("Sel")
        assertThat(item2?.defaultQuantity).isEqualTo(1.0)
        assertThat(item2?.defaultUnit).isNull()
        assertThat(item2?.category).isEqualTo("Épicerie")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseRawCatalogLine_BlankAndInvalidLines_ReturnNull() {
        assertThat(StandardCatalogLoader.parseRawCatalogLine("")).isNull()
        assertThat(StandardCatalogLoader.parseRawCatalogLine("   ")).isNull()
        assertThat(StandardCatalogLoader.parseRawCatalogLine("   | 100 | g")).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromResources_ExhaustiveArrayHandlingAndInvalidLines() {
        val mockRes = mock<Resources> {
            on { getStringArray(R.array.catalog_cremerie) } doReturn arrayOf("Lait|1|l|Crémerie", "   | 100 | g")
            on { getStringArray(R.array.catalog_epicerie) } doReturn arrayOf("Farine|1|kg|Épicerie")
            on { getStringArray(R.array.catalog_epices_condiments) } doReturn arrayOf("Sel|100|g|Épices")
            on { getStringArray(R.array.catalog_legumes_fruits) } doReturn arrayOf("Pommes|1|kg|Fruits")
            on { getStringArray(R.array.catalog_surgeles) } doReturn arrayOf("Glace|1|l|Surgelés")
            on { getStringArray(R.array.catalog_boissons_cave) } doThrow Resources.NotFoundException("Not found")
        }

        val items = StandardCatalogLoader.loadFromResources(mockRes)

        assertThat(items).hasSize(5)
        assertThat(items.map { it.name }).containsExactly("Lait", "Farine", "Sel", "Pommes", "Glace").inOrder()
        assertThat(items.first().defaultLocationName).isEqualTo("Frigo")
        assertThat(items.last().defaultLocationName).isEqualTo("Surgélateur")
    }
}
