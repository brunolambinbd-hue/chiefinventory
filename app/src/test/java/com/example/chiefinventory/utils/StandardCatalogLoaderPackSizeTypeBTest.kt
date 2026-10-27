package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le parsing du 7ème champ `packSize`
 * et la couverture de branches JaCoCo (valeur explicite vs valeur par défaut 1.0).
 */
class StandardCatalogLoaderPackSizeTypeBTest {

    @Test
    fun `parseRawCatalogLine should parse explicit 7th field packSize`() {
        val raw = "Lait demi-écrémé|1|l|Crémerie|PERMANENT|0.25|6"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.name).isEqualTo("Lait demi-écrémé")
        assertThat(item.minThreshold).isEqualTo(0.25)
        assertThat(item.packSize).isEqualTo(6.0) // Pack de 6 L !
    }

    @Test
    fun `parseRawCatalogLine should default packSize to 1 when 7th field is omitted`() {
        val raw = "Beurre|200|g|Crémerie|PERMANENT|50"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertThat(item).isNotNull()
        assertThat(item!!.minThreshold).isEqualTo(50.0)
        assertThat(item.packSize).isEqualTo(1.0)
    }
}
