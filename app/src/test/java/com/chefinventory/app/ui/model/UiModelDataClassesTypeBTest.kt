package com.chefinventory.app.ui.model

import com.chefinventory.app.model.Location
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B couvrant l'ensemble des Data Classes du paquetage [com.chefinventory.app.ui.model].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiModelDataClassesTypeBTest {

    @Test
    fun `CategoryInfo all equals branches hashCode copy componentN and toString should reach 100 percent coverage`() {
        val c1 = CategoryInfo(name = "Crémerie", possessedCount = 10, totalCount = 15, statusColorRes = 100)
        val c1Same = CategoryInfo(name = "Crémerie", possessedCount = 10, totalCount = 15, statusColorRes = 100)

        // Getters
        assertThat(c1.name).isEqualTo("Crémerie")
        assertThat(c1.possessedCount).isEqualTo(10)
        assertThat(c1.totalCount).isEqualTo(15)
        assertThat(c1.statusColorRes).isEqualTo(100)

        // Destructuring / componentN
        val (name, possessed, total, colorRes) = c1
        assertThat(name).isEqualTo("Crémerie")
        assertThat(possessed).isEqualTo(10)
        assertThat(total).isEqualTo(15)
        assertThat(colorRes).isEqualTo(100)

        // Equals
        assertThat(c1.equals(c1)).isTrue()
        assertThat(c1.equals(null)).isFalse()
        assertThat(c1.equals("NotACategoryInfo")).isFalse()
        assertThat(c1).isEqualTo(c1Same)

        // Discrepancies
        assertThat(c1).isNotEqualTo(c1.copy(name = "Épicerie"))
        assertThat(c1).isNotEqualTo(c1.copy(possessedCount = 5))
        assertThat(c1).isNotEqualTo(c1.copy(totalCount = 20))
        assertThat(c1).isNotEqualTo(c1.copy(statusColorRes = 200))

        // HashCode
        assertThat(c1.hashCode()).isEqualTo(c1Same.hashCode())
        assertThat(c1.hashCode()).isNotEqualTo(c1.copy(name = "Épicerie").hashCode())

        // ToString
        assertThat(c1.toString()).contains("Crémerie")
    }

    @Test
    fun `DisplayLocation all equals branches hashCode copy componentN and toString should reach 100 percent coverage`() {
        val loc = Location(id = 1L, name = "Frigo", parentId = null)
        val d1 = DisplayLocation(location = loc, depth = 0)
        val d1Same = DisplayLocation(location = loc, depth = 0)

        // Getters
        assertThat(d1.location).isEqualTo(loc)
        assertThat(d1.depth).isEqualTo(0)

        // Destructuring / componentN
        val (location, depth) = d1
        assertThat(location).isEqualTo(loc)
        assertThat(depth).isEqualTo(0)

        // Equals
        assertThat(d1.equals(d1)).isTrue()
        assertThat(d1.equals(null)).isFalse()
        assertThat(d1.equals("NotADisplayLocation")).isFalse()
        assertThat(d1).isEqualTo(d1Same)

        // Discrepancies
        val loc2 = Location(id = 2L, name = "Placard", parentId = null)
        assertThat(d1).isNotEqualTo(d1.copy(location = loc2))
        assertThat(d1).isNotEqualTo(d1.copy(depth = 1))

        // HashCode
        assertThat(d1.hashCode()).isEqualTo(d1Same.hashCode())
        assertThat(d1.hashCode()).isNotEqualTo(d1.copy(depth = 2).hashCode())

        // ToString
        assertThat(d1.toString()).contains("Frigo")
    }

    @Test
    fun `ExpandableLocation all equals branches default parameters hashCode copy componentN and toString`() {
        val loc = Location(id = 5L, name = "Placard", parentId = null)

        // Default parameters
        val eDefault = ExpandableLocation(location = loc, depth = 0, isExpanded = false, hasChildren = false)
        assertThat(eDefault.depth).isEqualTo(0)
        assertThat(eDefault.isExpanded).isFalse()
        assertThat(eDefault.hasChildren).isFalse()
        assertThat(eDefault.itemCount).isEqualTo(0)

        val e1 = ExpandableLocation(location = loc, depth = 1, isExpanded = true, hasChildren = true, itemCount = 10)
        val e1Same = ExpandableLocation(location = loc, depth = 1, isExpanded = true, hasChildren = true, itemCount = 10)

        // Getters
        assertThat(e1.location).isEqualTo(loc)
        assertThat(e1.depth).isEqualTo(1)
        assertThat(e1.isExpanded).isTrue()
        assertThat(e1.hasChildren).isTrue()
        assertThat(e1.itemCount).isEqualTo(10)

        // Destructuring / componentN
        val (l, d, exp, child, cnt) = e1
        assertThat(l).isEqualTo(loc)
        assertThat(d).isEqualTo(1)
        assertThat(exp).isTrue()
        assertThat(child).isTrue()
        assertThat(cnt).isEqualTo(10)

        // Equals
        assertThat(e1.equals(e1)).isTrue()
        assertThat(e1.equals(null)).isFalse()
        assertThat(e1.equals("NotAnExpandableLocation")).isFalse()
        assertThat(e1).isEqualTo(e1Same)

        // Discrepancies
        val loc2 = Location(id = 6L, name = "Cave", parentId = null)
        assertThat(e1).isNotEqualTo(e1.copy(location = loc2))
        assertThat(e1).isNotEqualTo(e1.copy(depth = 2))
        assertThat(e1).isNotEqualTo(e1.copy(isExpanded = false))
        assertThat(e1).isNotEqualTo(e1.copy(hasChildren = false))
        assertThat(e1).isNotEqualTo(e1.copy(itemCount = 99))

        // HashCode
        assertThat(e1.hashCode()).isEqualTo(e1Same.hashCode())
        assertThat(e1.hashCode()).isNotEqualTo(e1.copy(itemCount = 99).hashCode())

        // ToString
        assertThat(e1.toString()).contains("Placard")
    }
}
