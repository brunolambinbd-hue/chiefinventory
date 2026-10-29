package com.example.chiefinventory.data

import com.example.chiefinventory.model.LocationType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B couvrant à 100% l'ensemble des méthodes de [TypeConverters].
 */
class TypeConvertersTypeBTest {

    private val converter = TypeConverters()

    @Test
    fun `fromLocationType should convert enum to string name`() {
        assertThat(converter.fromLocationType(LocationType.INGREDIENT)).isEqualTo("INGREDIENT")
        assertThat(converter.fromLocationType(LocationType.RECIPE)).isEqualTo("RECIPE")
        assertThat(converter.fromLocationType(LocationType.COLLECTION)).isEqualTo("COLLECTION")
    }

    @Test
    fun `toLocationType should convert string to LocationType enum or fallback to COLLECTION on error`() {
        assertThat(converter.toLocationType("INGREDIENT")).isEqualTo(LocationType.INGREDIENT)
        assertThat(converter.toLocationType("RECIPE")).isEqualTo(LocationType.RECIPE)
        assertThat(converter.toLocationType("INVALID_ENUM_NAME")).isEqualTo(LocationType.COLLECTION)
    }
}
