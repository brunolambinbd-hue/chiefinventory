package com.example.chiefinventory.model

import android.os.Parcel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés pour la classe de données [Ingredient].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IngredientTest {

    @Test
    fun `Ingredient all properties getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val bytes = byteArrayOf(1, 2)
        val i1 = Ingredient(
            id = 50L, name = "Beurre", quantity = 250.0, unit = "g", category = "Crémerie",
            expirationDate = 1000L, description = "Doux", supplementalInfo = "Extra",
            imageUri = "uri", imageEmbedding = bytes, ocrText = "Beurre 250g", locationId = 1L,
            stockPolicy = "PERMANENT", minThreshold = 100.0, packSize = 1.0, updatedAt = 5000L
        )
        val i1Same = Ingredient(
            id = 50L, name = "Beurre", quantity = 250.0, unit = "g", category = "Crémerie",
            expirationDate = 1000L, description = "Doux", supplementalInfo = "Extra",
            imageUri = "uri", imageEmbedding = bytes, ocrText = "Beurre 250g", locationId = 1L,
            stockPolicy = "PERMANENT", minThreshold = 100.0, packSize = 1.0, updatedAt = 5000L
        )
        val iNull = Ingredient(id = 0L, name = "")

        // Destructuring / componentN
        val (id, name, qty, unit, cat, exp, desc, info, uri, emb, ocr) = i1
        assertThat(id).isEqualTo(50L)
        assertThat(name).isEqualTo("Beurre")
        assertThat(qty).isEqualTo(250.0)
        assertThat(unit).isEqualTo("g")
        assertThat(cat).isEqualTo("Crémerie")
        assertThat(exp).isEqualTo(1000L)
        assertThat(desc).isEqualTo("Doux")
        assertThat(info).isEqualTo("Extra")
        assertThat(uri).isEqualTo("uri")
        assertThat(emb).isEqualTo(bytes)
        assertThat(ocr).isEqualTo("Beurre 250g")

        // Component 12 to 16
        assertThat(i1.component12()).isEqualTo(1L)
        assertThat(i1.component13()).isEqualTo("PERMANENT")
        assertThat(i1.component14()).isEqualTo(100.0)
        assertThat(i1.component15()).isEqualTo(1.0)
        assertThat(i1.component16()).isEqualTo(5000L)

        // Equals branches
        assertThat(i1.equals(i1)).isTrue()
        assertThat(i1.equals("NotAnIngredient")).isFalse()
        assertThat(i1).isEqualTo(i1Same)
        assertThat(i1.hashCode()).isEqualTo(i1Same.hashCode())
        assertThat(iNull.hashCode()).isNotEqualTo(i1.hashCode())

        // Testing copy with every parameter changed
        assertThat(i1.copy(id = 2L)).isNotEqualTo(i1)
        assertThat(i1.copy(name = "Lait")).isNotEqualTo(i1)
        assertThat(i1.copy(quantity = 99.0)).isNotEqualTo(i1)
        assertThat(i1.copy(unit = "L")).isNotEqualTo(i1)
        assertThat(i1.copy(category = "Epicerie")).isNotEqualTo(i1)
        assertThat(i1.copy(expirationDate = 99L)).isNotEqualTo(i1)
        assertThat(i1.copy(description = "Autre")).isNotEqualTo(i1)
        assertThat(i1.copy(supplementalInfo = "Autre")).isNotEqualTo(i1)
        assertThat(i1.copy(imageUri = "Autre")).isNotEqualTo(i1)
        assertThat(i1.copy(imageEmbedding = null)).isNotEqualTo(i1)
        assertThat(i1.copy(ocrText = "Autre")).isNotEqualTo(i1)
        assertThat(i1.copy(locationId = 99L)).isNotEqualTo(i1)
        assertThat(i1.copy(stockPolicy = "ON_DEMAND")).isNotEqualTo(i1)
        assertThat(i1.copy(minThreshold = 99.0)).isNotEqualTo(i1)
        assertThat(i1.copy(packSize = 6.0)).isNotEqualTo(i1)
        assertThat(i1.copy(updatedAt = 9999L)).isNotEqualTo(i1)

        assertThat(i1.toString()).contains("Beurre")
    }

    @Test
    fun `Ingredient Parcelable with non-null and null fields should cover all writeToParcel and CREATOR branches`() {
        val bytes = byteArrayOf(1, 2, 3)
        val iFull = Ingredient(
            id = 50L,
            name = "Beurre doux",
            quantity = 250.0,
            unit = "g",
            category = "Crémerie",
            expirationDate = 10000L,
            description = "Beurre AOP Charentes-Poitou",
            supplementalInfo = "Doux 82% MG",
            imageUri = "file:///tmp/beurre.jpg",
            imageEmbedding = bytes,
            ocrText = "Beurre doux 250g",
            locationId = 1L,
            stockPolicy = "PERMANENT",
            minThreshold = 100.0,
            packSize = 1.0,
            updatedAt = 50000L
        )

        val iNulls = Ingredient(
            id = 60L,
            name = "Inconnu",
            quantity = null,
            unit = null,
            category = null,
            expirationDate = null,
            description = null,
            supplementalInfo = null,
            imageUri = null,
            imageEmbedding = null,
            ocrText = null,
            locationId = null,
            stockPolicy = "ON_DEMAND",
            minThreshold = null,
            packSize = 1.0,
            updatedAt = 60000L
        )

        // DescribeContents
        assertThat(iFull.describeContents()).isEqualTo(0)
        assertThat(iNulls.describeContents()).isEqualTo(0)

        @Suppress("UNCHECKED_CAST")
        val creator = Ingredient::class.java.getField("CREATOR").get(null) as android.os.Parcelable.Creator<Ingredient>

        // Test 1 : Full Non-Null Parceling
        val parcelFull = Parcel.obtain()
        iFull.writeToParcel(parcelFull, 0)
        parcelFull.setDataPosition(0)

        val createdFull = creator.createFromParcel(parcelFull)
        assertThat(createdFull.id).isEqualTo(50L)
        assertThat(createdFull.name).isEqualTo("Beurre doux")
        assertThat(createdFull.quantity).isEqualTo(250.0)
        assertThat(createdFull.unit).isEqualTo("g")
        assertThat(createdFull.category).isEqualTo("Crémerie")
        assertThat(createdFull.expirationDate).isEqualTo(10000L)
        assertThat(createdFull.description).isEqualTo("Beurre AOP Charentes-Poitou")
        assertThat(createdFull.supplementalInfo).isEqualTo("Doux 82% MG")
        assertThat(createdFull.imageUri).isEqualTo("file:///tmp/beurre.jpg")
        assertThat(createdFull.imageEmbedding).isEqualTo(bytes)
        assertThat(createdFull.ocrText).isEqualTo("Beurre doux 250g")
        assertThat(createdFull.locationId).isEqualTo(1L)
        assertThat(createdFull.stockPolicy).isEqualTo("PERMANENT")
        assertThat(createdFull.minThreshold).isEqualTo(100.0)
        assertThat(createdFull.packSize).isEqualTo(1.0)
        assertThat(createdFull.updatedAt).isEqualTo(50000L)
        parcelFull.recycle()

        // Test 2 : All-Nulls Parceling
        val parcelNulls = Parcel.obtain()
        iNulls.writeToParcel(parcelNulls, 0)
        parcelNulls.setDataPosition(0)

        val createdNulls = creator.createFromParcel(parcelNulls)
        assertThat(createdNulls.id).isEqualTo(60L)
        assertThat(createdNulls.name).isEqualTo("Inconnu")
        assertThat(createdNulls.quantity).isNull()
        assertThat(createdNulls.unit).isNull()
        assertThat(createdNulls.category).isNull()
        assertThat(createdNulls.expirationDate).isNull()
        assertThat(createdNulls.description).isNull()
        assertThat(createdNulls.supplementalInfo).isNull()
        assertThat(createdNulls.imageUri).isNull()
        assertThat(createdNulls.imageEmbedding).isNull()
        assertThat(createdNulls.ocrText).isNull()
        assertThat(createdNulls.locationId).isNull()
        assertThat(createdNulls.stockPolicy).isEqualTo("ON_DEMAND")
        assertThat(createdNulls.minThreshold).isNull()
        assertThat(createdNulls.packSize).isEqualTo(1.0)
        assertThat(createdNulls.updatedAt).isEqualTo(60000L)
        parcelNulls.recycle()

        // Test 3 : CREATOR.newArray
        val array = creator.newArray(3)
        assertThat(array.size).isEqualTo(3)
    }

    @Test
    fun `Ingredient default parameters equals with null vs non-null ByteArray and copy with nulls`() {
        // Default constructor parameters (line 37 stockPolicy = "PERMANENT")
        val iDef = Ingredient(name = "Farine")
        assertThat(iDef.stockPolicy).isEqualTo("PERMANENT")
        assertThat(iDef.packSize).isEqualTo(1.0)
        assertThat(iDef.quantity).isNull()

        val bytes1 = byteArrayOf(1, 2)
        val bytesDiff = byteArrayOf(9, 9)

        val iWithEmb1 = Ingredient(id = 1L, name = "A", imageEmbedding = bytes1)
        val iWithEmb1Same = Ingredient(id = 1L, name = "A", imageEmbedding = bytes1) // Same ByteArray reference
        val iWithEmbDiff = Ingredient(id = 1L, name = "A", imageEmbedding = bytesDiff)
        val iNullEmb = Ingredient(id = 1L, name = "A", imageEmbedding = null)

        // ByteArray equals branches (line 42)
        assertThat(iWithEmb1).isEqualTo(iWithEmb1Same)
        assertThat(iWithEmb1).isNotEqualTo(iWithEmbDiff)
        assertThat(iWithEmb1).isNotEqualTo(iNullEmb) // non-null vs null
        assertThat(iNullEmb).isNotEqualTo(iWithEmb1) // null vs non-null

        // Copy with nulls for description and ocrText (lines 31 & 35)
        val iWithTexts = Ingredient(name = "B", description = "Desc", ocrText = "OCR")
        assertThat(iWithTexts.copy(description = null)).isNotEqualTo(iWithTexts)
        assertThat(iWithTexts.copy(ocrText = null)).isNotEqualTo(iWithTexts)
    }
}
