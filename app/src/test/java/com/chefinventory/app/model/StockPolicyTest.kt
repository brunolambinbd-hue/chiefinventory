package com.chefinventory.app.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés pour l'objet de constantes [StockPolicy].
 */
class StockPolicyTest {

    @Test
    fun `StockPolicy constants should match database expected values`() {
        assertThat(StockPolicy.PERMANENT).isEqualTo("PERMANENT")
        assertThat(StockPolicy.ON_DEMAND).isEqualTo("ON_DEMAND")
    }
}
