package com.example.chiefinventory.data

import com.example.chiefinventory.model.LocationType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés (1-à-1) pour la classe [TypeConverters].
 */
class TypeConvertersTest {

    private val converters = TypeConverters()

    @Test
    fun `fromLocationType should return enum name string`() {
        assertThat(converters.fromLocationType(LocationType.INGREDIENT)).isEqualTo("INGREDIENT")
        assertThat(converters.fromLocationType(LocationType.RECIPE)).isEqualTo("RECIPE")
    }

    @Test
    fun `toLocationType with valid and invalid strings should cover try and catch branches`() {
        // Try branch (valid values)
        assertThat(converters.toLocationType("INGREDIENT")).isEqualTo(LocationType.INGREDIENT)
        assertThat(converters.toLocationType("RECIPE")).isEqualTo(LocationType.RECIPE)

        // Catch branch (invalid value -> fallback INGREDIENT)
        assertThat(converters.toLocationType("INVALID_LOCATION_TYPE")).isEqualTo(LocationType.INGREDIENT)
        assertThat(converters.toLocationType("COLLECTION")).isEqualTo(LocationType.INGREDIENT)
    }
}
