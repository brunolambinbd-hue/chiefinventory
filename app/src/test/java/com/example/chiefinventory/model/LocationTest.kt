package com.example.chiefinventory.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés pour la classe de données [Location] et l'énumération [LocationType].
 */
class LocationTest {

    @Test
    fun `Location default parameters getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        // Default constructor invocation ($default synthetic constructor)
        val lDefault = Location(name = "Frigo", parentId = null)
        assertThat(lDefault.id).isEqualTo(0L)
        assertThat(lDefault.name).isEqualTo("Frigo")
        assertThat(lDefault.parentId).isNull()
        assertThat(lDefault.type).isEqualTo(LocationType.INGREDIENT)

        // Explicit constructor invocation
        val l1 = Location(id = 10L, name = "Placard", parentId = 1L, type = LocationType.RECIPE)
        val l1Same = Location(id = 10L, name = "Placard", parentId = 1L, type = LocationType.RECIPE)

        // Getters
        assertThat(l1.id).isEqualTo(10L)
        assertThat(l1.name).isEqualTo("Placard")
        assertThat(l1.parentId).isEqualTo(1L)
        assertThat(l1.type).isEqualTo(LocationType.RECIPE)

        // Destructuring / componentN (1 à 4)
        val (id, name, parentId, type) = l1
        assertThat(id).isEqualTo(10L)
        assertThat(name).isEqualTo("Placard")
        assertThat(parentId).isEqualTo(1L)
        assertThat(type).isEqualTo(LocationType.RECIPE)

        // Equals
        assertThat(l1.equals(l1)).isTrue()
        assertThat(l1.equals(null)).isFalse()
        assertThat(l1.equals("NotALocation")).isFalse()
        assertThat(l1).isEqualTo(l1Same)

        // Copy discrepancies
        assertThat(l1).isNotEqualTo(l1.copy(id = 99L))
        assertThat(l1).isNotEqualTo(l1.copy(name = "Cave"))
        assertThat(l1).isNotEqualTo(l1.copy(parentId = null))
        assertThat(l1).isNotEqualTo(l1.copy(type = LocationType.INGREDIENT))

        // HashCode & ToString
        assertThat(l1.hashCode()).isEqualTo(l1Same.hashCode())
        assertThat(lDefault.hashCode()).isNotEqualTo(l1.hashCode())
        assertThat(l1.toString()).contains("Placard")
    }

    @Test
    fun `LocationType enum entries and valueOf should be covered`() {
        val entries = LocationType.entries
        assertThat(entries).containsExactly(LocationType.INGREDIENT, LocationType.RECIPE)

        assertThat(LocationType.valueOf("INGREDIENT")).isEqualTo(LocationType.INGREDIENT)
        assertThat(LocationType.valueOf("RECIPE")).isEqualTo(LocationType.RECIPE)
    }
}
