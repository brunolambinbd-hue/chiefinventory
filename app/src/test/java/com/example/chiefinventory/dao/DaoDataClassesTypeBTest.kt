package com.example.chiefinventory.dao

import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B couvrant à 100% l'ensemble des Data Classes du paquetage [com.example.chiefinventory.dao].
 */
class DaoDataClassesTypeBTest {

    @Test
    fun `SignatureReportItem all equals branches hashCode copy componentN and toString should reach 100 percent coverage`() {
        val bytes1 = byteArrayOf(1, 2, 3)
        val bytes1Same = byteArrayOf(1, 2, 3)
        val bytesDiff = byteArrayOf(9, 9, 9)

        val item1 = SignatureReportItem(id = 1L, titre = "Objet A", imageUri = "uri1", imageEmbedding = bytes1)
        val item1Same = SignatureReportItem(id = 1L, titre = "Objet A", imageUri = "uri1", imageEmbedding = bytes1Same)
        val itemNulls = SignatureReportItem(id = 2L, titre = "Objet B", imageUri = null, imageEmbedding = null)
        val itemNullsSame = SignatureReportItem(id = 2L, titre = "Objet B", imageUri = null, imageEmbedding = null)

        // Getters
        assertThat(item1.id).isEqualTo(1L)
        assertThat(item1.titre).isEqualTo("Objet A")
        assertThat(item1.imageUri).isEqualTo("uri1")
        assertThat(item1.imageEmbedding).isEqualTo(bytes1)

        // Destructuring / componentN
        val (id, titre, uri, emb) = item1
        assertThat(id).isEqualTo(1L)
        assertThat(titre).isEqualTo("Objet A")
        assertThat(uri).isEqualTo("uri1")
        assertThat(emb).isEqualTo(bytes1)

        // Line 17 branches: same instance, null, different class
        assertThat(item1.equals(item1)).isTrue()
        assertThat(item1.equals(null)).isFalse()
        assertThat(item1.equals("NotASignatureReportItem")).isFalse()

        // Equal items
        assertThat(item1).isEqualTo(item1Same)
        assertThat(itemNulls).isEqualTo(itemNullsSame)

        // Field discrepancies in equals
        assertThat(item1).isNotEqualTo(item1.copy(id = 99L))
        assertThat(item1).isNotEqualTo(item1.copy(titre = "Autre Titre"))
        assertThat(item1).isNotEqualTo(item1.copy(imageUri = "uri_differente"))
        assertThat(item1).isNotEqualTo(item1.copy(imageUri = null))
        assertThat(itemNulls).isNotEqualTo(itemNulls.copy(imageUri = "uri_non_nulle"))

        // Line 26 branch: this.imageEmbedding != null AND other.imageEmbedding == null
        assertThat(item1).isNotEqualTo(item1.copy(imageEmbedding = null))

        // Line 27 branch: this.imageEmbedding != null AND other.imageEmbedding != null AND !contentEquals
        assertThat(item1).isNotEqualTo(item1.copy(imageEmbedding = bytesDiff))

        // else if branch: this.imageEmbedding == null AND other.imageEmbedding != null
        assertThat(itemNulls).isNotEqualTo(itemNulls.copy(imageEmbedding = bytes1))

        // Line 35 & 36 branches in hashCode: non-null vs null imageUri and imageEmbedding
        assertThat(item1.hashCode()).isEqualTo(item1Same.hashCode())
        assertThat(itemNulls.hashCode()).isEqualTo(itemNullsSame.hashCode())
        assertThat(item1.hashCode()).isNotEqualTo(itemNulls.hashCode())

        // ToString
        assertThat(item1.toString()).contains("Objet A")
    }

    @Test
    fun `CategoryInfoFromDb properties equals hashCode and copy should function properly`() {
        val cat1 = CategoryInfoFromDb(name = "BD", possessedCount = 10, totalCount = 20)
        val cat1Same = CategoryInfoFromDb(name = "BD", possessedCount = 10, totalCount = 20)
        val cat2 = CategoryInfoFromDb(name = "Comics", possessedCount = 5, totalCount = 5)

        assertThat(cat1.name).isEqualTo("BD")
        assertThat(cat1.possessedCount).isEqualTo(10)
        assertThat(cat1.totalCount).isEqualTo(20)

        // Destructuring / componentN
        val (name, possessed, total) = cat1
        assertThat(name).isEqualTo("BD")
        assertThat(possessed).isEqualTo(10)
        assertThat(total).isEqualTo(20)

        assertThat(cat1.equals(cat1)).isTrue()
        assertThat(cat1.equals(null)).isFalse()
        assertThat(cat1.equals("String")).isFalse()
        assertThat(cat1).isEqualTo(cat1Same)
        assertThat(cat1).isNotEqualTo(cat2)
        assertThat(cat1).isNotEqualTo(cat1.copy(name = "Autre"))
        assertThat(cat1).isNotEqualTo(cat1.copy(possessedCount = 99))
        assertThat(cat1).isNotEqualTo(cat1.copy(totalCount = 99))
        assertThat(cat1.hashCode()).isEqualTo(cat1Same.hashCode())

        val copyCat = cat1.copy(possessedCount = 15)
        assertThat(copyCat.possessedCount).isEqualTo(15)
        assertThat(cat1.toString()).contains("BD")
    }

    @Test
    fun `ItemCountForLocation properties equals hashCode and copy should function properly`() {
        val count1 = ItemCountForLocation(locationId = 1L, count = 42)
        val count1Same = ItemCountForLocation(locationId = 1L, count = 42)
        val count2 = ItemCountForLocation(locationId = 2L, count = 10)

        assertThat(count1.locationId).isEqualTo(1L)
        assertThat(count1.count).isEqualTo(42)

        // Destructuring / componentN
        val (locId, countVal) = count1
        assertThat(locId).isEqualTo(1L)
        assertThat(countVal).isEqualTo(42)

        assertThat(count1.equals(count1)).isTrue()
        assertThat(count1.equals(null)).isFalse()
        assertThat(count1.equals("String")).isFalse()
        assertThat(count1).isEqualTo(count1Same)
        assertThat(count1).isNotEqualTo(count2)
        assertThat(count1).isNotEqualTo(count1.copy(locationId = 99L))
        assertThat(count1).isNotEqualTo(count1.copy(count = 99))
        assertThat(count1.hashCode()).isEqualTo(count1Same.hashCode())

        val copyCount = count1.copy(count = 50)
        assertThat(copyCount.count).isEqualTo(50)
        assertThat(count1.toString()).contains("1")
    }

    @Test
    fun `RecipeIngredientWithLocation properties equals hashCode and copy should function properly`() {
        val ingr = RecipeIngredient(recipeId = 1L, ingredientName = "Sucre", quantityRequired = 100.0, unit = "g")
        val loc = Location(id = 10L, name = "Placard", parentId = null, type = LocationType.INGREDIENT)

        val rel1 = RecipeIngredientWithLocation(ingredient = ingr, location = loc)
        val rel1Same = RecipeIngredientWithLocation(ingredient = ingr, location = loc)
        val rel2 = RecipeIngredientWithLocation(ingredient = ingr, location = null)

        assertThat(rel1.ingredient).isEqualTo(ingr)
        assertThat(rel1.location).isEqualTo(loc)

        // Destructuring / componentN
        val (i, l) = rel1
        assertThat(i).isEqualTo(ingr)
        assertThat(l).isEqualTo(loc)

        assertThat(rel1.equals(rel1)).isTrue()
        assertThat(rel1.equals(null)).isFalse()
        assertThat(rel1.equals("String")).isFalse()
        assertThat(rel1).isEqualTo(rel1Same)
        assertThat(rel1).isNotEqualTo(rel2)
        assertThat(rel1).isNotEqualTo(rel1.copy(location = Location(id = 99L, name = "Autre", parentId = null)))
        assertThat(rel1.hashCode()).isEqualTo(rel1Same.hashCode())

        val copyRel = rel1.copy(location = null)
        assertThat(copyRel.location).isNull()
        assertThat(rel1.toString()).contains("Sucre")
    }
}
