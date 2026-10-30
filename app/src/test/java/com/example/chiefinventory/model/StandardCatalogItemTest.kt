package com.example.chiefinventory.model

import android.os.Parcel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés pour la classe de données [StandardCatalogItem].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StandardCatalogItemTest {

    @Test
    fun `StandardCatalogItem equals hashCode copy componentN should reach 100 percent coverage`() {
        val sc1 = StandardCatalogItem(
            name = "Lait", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie",
            defaultLocationName = "Frigo", stockPolicy = "PERMANENT", minThreshold = 0.25, packSize = 6.0
        )
        val sc1Same = StandardCatalogItem(
            name = "Lait", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie",
            defaultLocationName = "Frigo", stockPolicy = "PERMANENT", minThreshold = 0.25, packSize = 6.0
        )
        val scNull = StandardCatalogItem(name = "")

        val (n, dq, du, catG, locN, sp, mt, ps) = sc1
        assertThat(n).isEqualTo("Lait")
        assertThat(dq).isEqualTo(1.0)
        assertThat(du).isEqualTo("L")
        assertThat(catG).isEqualTo("Crémerie")
        assertThat(locN).isEqualTo("Frigo")
        assertThat(sp).isEqualTo("PERMANENT")
        assertThat(mt).isEqualTo(0.25)
        assertThat(ps).isEqualTo(6.0)

        assertThat(sc1).isEqualTo(sc1Same)
        assertThat(sc1.hashCode()).isEqualTo(sc1Same.hashCode())
        assertThat(scNull.hashCode()).isNotEqualTo(sc1.hashCode())

        assertThat(sc1.copy(name = "X")).isNotEqualTo(sc1)
        assertThat(sc1.copy(defaultQuantity = 99.0)).isNotEqualTo(sc1)
        assertThat(sc1.copy(defaultUnit = "X")).isNotEqualTo(sc1)
        assertThat(sc1.copy(category = "X")).isNotEqualTo(sc1)
        assertThat(sc1.copy(defaultLocationName = "X")).isNotEqualTo(sc1)
        assertThat(sc1.copy(stockPolicy = "ON_DEMAND")).isNotEqualTo(sc1)
        assertThat(sc1.copy(minThreshold = 99.0)).isNotEqualTo(sc1)
        assertThat(sc1.copy(packSize = 99.0)).isNotEqualTo(sc1)
    }

    @Test
    fun `StandardCatalogItem Parcelable with non-null and null fields should cover all branches`() {
        val scItem = StandardCatalogItem(
            name = "Lait demi-écrémé",
            defaultQuantity = 1.0,
            defaultUnit = "L",
            category = "Crémerie",
            defaultLocationName = "Frigo",
            stockPolicy = "PERMANENT",
            minThreshold = 0.25,
            packSize = 6.0
        )

        val scItemNulls = StandardCatalogItem(
            name = "Eau",
            defaultQuantity = 1.0,
            defaultUnit = null,
            category = "Boissons",
            defaultLocationName = "Cave",
            stockPolicy = "ON_DEMAND",
            minThreshold = 0.0,
            packSize = 1.0
        )

        assertThat(scItem.describeContents()).isEqualTo(0)
        assertThat(scItemNulls.describeContents()).isEqualTo(0)

        @Suppress("UNCHECKED_CAST")
        val creator = StandardCatalogItem::class.java.getField("CREATOR").get(null) as android.os.Parcelable.Creator<StandardCatalogItem>

        val parcel1 = Parcel.obtain()
        scItem.writeToParcel(parcel1, 0)
        parcel1.setDataPosition(0)

        val created1 = creator.createFromParcel(parcel1)
        assertThat(created1).isEqualTo(scItem)
        parcel1.recycle()

        val parcel2 = Parcel.obtain()
        scItemNulls.writeToParcel(parcel2, 0)
        parcel2.setDataPosition(0)

        val created2 = creator.createFromParcel(parcel2)
        assertThat(created2).isEqualTo(scItemNulls)
        parcel2.recycle()

        val array = creator.newArray(2)
        assertThat(array.size).isEqualTo(2)
    }
}
