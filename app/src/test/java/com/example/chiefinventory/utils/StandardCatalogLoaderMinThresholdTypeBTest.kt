package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le parsing du 6ème champ `minThreshold`
 * et la couverture de branches JaCoCo (valeur explicite vs valeur déduite du stock non nulle).
 *
 * @see REQ-4.3.1.1
 * @see REQ-4.3.1.2
 */
class StandardCatalogLoaderMinThresholdTypeBTest {

    @Test
    fun `parseRawCatalogLine should parse explicit 6th field minThreshold`() {
        val raw = "Lait demi-écrémé|1|l|Crémerie|PERMANENT|0.25"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.name).isEqualTo("Lait demi-écrémé")
        assertThat(item.defaultQuantity).isEqualTo(1.0)
        assertThat(item.minThreshold).isEqualTo(0.25)
    }

    @Test
    fun `parseRawCatalogLine should fallback to 50 percent of defaultQuantity when 6th field is omitted`() {
        val raw = "Beurre|200|g|Crémerie|PERMANENT"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.defaultQuantity).isEqualTo(200.0)
        assertThat(item.minThreshold).isEqualTo(100.0) // 200 / 2 = 100 g (non nul)
    }

    @Test
    fun `parseRawCatalogLine should return null on blank line`() {
        val item = StandardCatalogLoader.parseRawCatalogLine("  ", "Frigo")
        assertThat(item).isNull()
    }
}
