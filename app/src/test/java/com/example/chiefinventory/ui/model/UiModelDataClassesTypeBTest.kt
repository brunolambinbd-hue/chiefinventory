package com.example.chiefinventory.ui.model

import android.os.Parcel
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires saturant à 100% les branches de toutes les data classes du package [com.example.chiefinventory.ui.model]
 * ([CategoryInfo], [DisplayLocation], [ExpandableLocation], [SearchResultItem]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiModelDataClassesTypeBTest {

    @Test
    fun `CategoryInfo all equals branches hashCode copy componentN and toString`() {
        val c1 = CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 100)
        val c1Same = CategoryInfo(name = "BD", possessedCount = 10, totalCount = 20, statusColorRes = 100)

        // Getters
        assertThat(c1.name).isEqualTo("BD")
        assertThat(c1.possessedCount).isEqualTo(10)
        assertThat(c1.totalCount).isEqualTo(20)
        assertThat(c1.statusColorRes).isEqualTo(100)

        // Destructuring / componentN
        val (n, p, t, col) = c1
        assertThat(n).isEqualTo("BD")
        assertThat(p).isEqualTo(10)
        assertThat(t).isEqualTo(20)
        assertThat(col).isEqualTo(100)

        // Exhaustive Equals branches
        assertThat(c1.equals(c1)).isTrue()
        assertThat(c1.equals(null)).isFalse()
        assertThat(c1.equals("NotACategoryInfo")).isFalse()
        assertThat(c1).isEqualTo(c1Same)

        // Field discrepancy branches for equals
        assertThat(c1).isNotEqualTo(c1.copy(name = "Comics"))
        assertThat(c1).isNotEqualTo(c1.copy(possessedCount = 99))
        assertThat(c1).isNotEqualTo(c1.copy(totalCount = 99))
        assertThat(c1).isNotEqualTo(c1.copy(statusColorRes = 999))

        // HashCode
        assertThat(c1.hashCode()).isEqualTo(c1Same.hashCode())
        assertThat(c1.hashCode()).isNotEqualTo(c1.copy(name = "Comics").hashCode())

        // ToString
        assertThat(c1.toString()).contains("BD")
    }

    @Test
    fun `DisplayLocation all equals branches hashCode copy componentN and toString`() {
        val loc1 = Location(id = 1L, name = "Frigo", parentId = null, type = LocationType.INGREDIENT)
        val loc2 = Location(id = 2L, name = "Etagère 1", parentId = 1L, type = LocationType.INGREDIENT)

        val d1 = DisplayLocation(location = loc1, depth = 0)
        val d1Same = DisplayLocation(location = loc1, depth = 0)

        // Getters
        assertThat(d1.location).isEqualTo(loc1)
        assertThat(d1.depth).isEqualTo(0)

        // Destructuring / componentN
        val (l, d) = d1
        assertThat(l).isEqualTo(loc1)
        assertThat(d).isEqualTo(0)

        // Exhaustive Equals branches
        assertThat(d1.equals(d1)).isTrue()
        assertThat(d1.equals(null)).isFalse()
        assertThat(d1.equals("NotADisplayLocation")).isFalse()
        assertThat(d1).isEqualTo(d1Same)

        // Field discrepancy branches for equals
        assertThat(d1).isNotEqualTo(d1.copy(location = loc2))
        assertThat(d1).isNotEqualTo(d1.copy(depth = 5))

        // HashCode
        assertThat(d1.hashCode()).isEqualTo(d1Same.hashCode())
        assertThat(d1.hashCode()).isNotEqualTo(d1.copy(depth = 5).hashCode())

        // ToString
        assertThat(d1.toString()).contains("Frigo")
    }

    @Test
    fun `ExpandableLocation all equals branches default parameters hashCode copy componentN and toString`() {
        val loc1 = Location(id = 1L, name = "Placard", parentId = null, type = LocationType.INGREDIENT)
        val loc2 = Location(id = 2L, name = "Tiroir", parentId = 1L, type = LocationType.INGREDIENT)

        // Default constructor value test
        val eDefault = ExpandableLocation(location = loc1, depth = 0, isExpanded = false, hasChildren = true)
        assertThat(eDefault.itemCount).isEqualTo(0)

        val e1 = ExpandableLocation(location = loc1, depth = 0, isExpanded = false, hasChildren = true, itemCount = 5)
        val e1Same = ExpandableLocation(location = loc1, depth = 0, isExpanded = false, hasChildren = true, itemCount = 5)

        // Getters
        assertThat(e1.location).isEqualTo(loc1)
        assertThat(e1.depth).isEqualTo(0)
        assertThat(e1.isExpanded).isFalse()
        assertThat(e1.hasChildren).isTrue()
        assertThat(e1.itemCount).isEqualTo(5)

        // Destructuring / componentN
        val (l, d, exp, child, count) = e1
        assertThat(l).isEqualTo(loc1)
        assertThat(d).isEqualTo(0)
        assertThat(exp).isFalse()
        assertThat(child).isTrue()
        assertThat(count).isEqualTo(5)

        // Exhaustive Equals branches
        assertThat(e1.equals(e1)).isTrue()
        assertThat(e1.equals(null)).isFalse()
        assertThat(e1.equals("NotAnExpandableLocation")).isFalse()
        assertThat(e1).isEqualTo(e1Same)

        // Field discrepancy branches for equals
        assertThat(e1).isNotEqualTo(e1.copy(location = loc2))
        assertThat(e1).isNotEqualTo(e1.copy(depth = 2))
        assertThat(e1).isNotEqualTo(e1.copy(isExpanded = true))
        assertThat(e1).isNotEqualTo(e1.copy(hasChildren = false))
        assertThat(e1).isNotEqualTo(e1.copy(itemCount = 99))

        // HashCode
        assertThat(e1.hashCode()).isEqualTo(e1Same.hashCode())
        assertThat(e1.hashCode()).isNotEqualTo(e1.copy(itemCount = 99).hashCode())

        // ToString
        assertThat(e1.toString()).contains("Placard")
    }

    @Test
    fun `SearchResultItem all equals branches nullable similarity default parameters hashCode copy componentN parcelable and toString`() {
        val item1 = CollectionItem(id = 10L, titre = "Tintin au Tibet")
        val item2 = CollectionItem(id = 20L, titre = "Asterix le Gaulois")

        // Default constructor value
        val sDefault = SearchResultItem(item = item1)
        assertThat(sDefault.similarity).isNull()

        val s1 = SearchResultItem(item = item1, similarity = 0.85)
        val s1Same = SearchResultItem(item = item1, similarity = 0.85)
        val sNullSim1 = SearchResultItem(item = item1, similarity = null)
        val sNullSim2 = SearchResultItem(item = item1, similarity = null)

        // Getters
        assertThat(s1.item).isEqualTo(item1)
        assertThat(s1.similarity).isEqualTo(0.85)

        // Destructuring / componentN
        val (it, sim) = s1
        assertThat(it).isEqualTo(item1)
        assertThat(sim).isEqualTo(0.85)

        // Exhaustive Equals branches (including null vs non-null similarity)
        assertThat(s1.equals(s1)).isTrue()
        assertThat(s1.equals(null)).isFalse()
        assertThat(s1.equals("NotASearchResultItem")).isFalse()
        assertThat(s1).isEqualTo(s1Same)
        assertThat(sNullSim1).isEqualTo(sNullSim2)

        // Discrepancy branches
        assertThat(s1).isNotEqualTo(s1.copy(item = item2))
        assertThat(s1).isNotEqualTo(s1.copy(similarity = 0.99))
        assertThat(s1).isNotEqualTo(sNullSim1) // Non-null vs null
        assertThat(sNullSim1).isNotEqualTo(s1) // Null vs non-null

        // HashCode
        assertThat(s1.hashCode()).isEqualTo(s1Same.hashCode())
        assertThat(sNullSim1.hashCode()).isEqualTo(sNullSim2.hashCode())
        assertThat(s1.hashCode()).isNotEqualTo(s1.copy(similarity = 0.99).hashCode())

        // ToString
        assertThat(s1.toString()).contains("Tintin au Tibet")

        // DescribeContents
        assertThat(s1.describeContents()).isEqualTo(0)
        assertThat(sNullSim1.describeContents()).isEqualTo(0)

        @Suppress("UNCHECKED_CAST")
        val creator = SearchResultItem::class.java.getField("CREATOR").get(null) as android.os.Parcelable.Creator<SearchResultItem>

        // Parcelable testing with NON-NULL similarity
        val parcelNonNull = Parcel.obtain()
        s1.writeToParcel(parcelNonNull, 0)
        parcelNonNull.setDataPosition(0)

        val createdNonNull = creator.createFromParcel(parcelNonNull)
        assertThat(createdNonNull).isEqualTo(s1)
        assertThat(createdNonNull.similarity).isEqualTo(0.85)
        parcelNonNull.recycle()

        // Parcelable testing with NULL similarity (covers null branch in generated Parcelable code)
        val parcelNull = Parcel.obtain()
        sNullSim1.writeToParcel(parcelNull, 1)
        parcelNull.setDataPosition(0)

        val createdNull = creator.createFromParcel(parcelNull)
        assertThat(createdNull).isEqualTo(sNullSim1)
        assertThat(createdNull.similarity).isNull()
        parcelNull.recycle()

        val array = creator.newArray(3)
        assertThat(array.size).isEqualTo(3)
    }
}
