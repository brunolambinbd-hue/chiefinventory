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
    fun `SignatureReportItem properties equals hashCode and copy should function properly`() {
        val bytes1 = byteArrayOf(1, 2, 3)
        val bytes2 = byteArrayOf(1, 2, 3)
        val item1 = SignatureReportItem(id = 1L, titre = "Objet A", imageUri = "uri1", imageEmbedding = bytes1)
        val item1Same = SignatureReportItem(id = 1L, titre = "Objet A", imageUri = "uri1", imageEmbedding = bytes2)
        val item2 = SignatureReportItem(id = 2L, titre = "Objet B", imageUri = null, imageEmbedding = null)

        assertThat(item1.id).isEqualTo(1L)
        assertThat(item1.titre).isEqualTo("Objet A")
        assertThat(item1.imageUri).isEqualTo("uri1")
        assertThat(item1.imageEmbedding).isEqualTo(bytes1)

        assertThat(item1).isEqualTo(item1Same)
        assertThat(item1).isNotEqualTo(item2)
        assertThat(item1.hashCode()).isEqualTo(item1Same.hashCode())

        val copyItem = item1.copy(id = 99L)
        assertThat(copyItem.id).isEqualTo(99L)

        // Tests de branches equals() pour SignatureReportItem
        assertThat(item1.equals(item1)).isTrue()
        assertThat(item1.equals("OtherType")).isFalse()
        assertThat(item1.equals(item1.copy(id = 2L))).isFalse()
        assertThat(item1.equals(item1.copy(titre = "Autre"))).isFalse()
        assertThat(item1.equals(item1.copy(imageUri = "uri2"))).isFalse()
        assertThat(item1.equals(item1.copy(imageEmbedding = null))).isFalse()
        assertThat(item2.equals(item1)).isFalse()
        assertThat(item2.equals(item2.copy())).isTrue()
    }

    @Test
    fun `CategoryInfoFromDb properties equals hashCode and copy should function properly`() {
        val cat1 = CategoryInfoFromDb(name = "BD", possessedCount = 10, totalCount = 20)
        val cat1Same = CategoryInfoFromDb(name = "BD", possessedCount = 10, totalCount = 20)
        val cat2 = CategoryInfoFromDb(name = "Comics", possessedCount = 5, totalCount = 5)

        assertThat(cat1.name).isEqualTo("BD")
        assertThat(cat1.possessedCount).isEqualTo(10)
        assertThat(cat1.totalCount).isEqualTo(20)

        assertThat(cat1).isEqualTo(cat1Same)
        assertThat(cat1).isNotEqualTo(cat2)
        assertThat(cat1.hashCode()).isEqualTo(cat1Same.hashCode())

        val copyCat = cat1.copy(possessedCount = 15)
        assertThat(copyCat.possessedCount).isEqualTo(15)
    }

    @Test
    fun `ItemCountForLocation properties equals hashCode and copy should function properly`() {
        val count1 = ItemCountForLocation(locationId = 1L, count = 42)
        val count1Same = ItemCountForLocation(locationId = 1L, count = 42)
        val count2 = ItemCountForLocation(locationId = 2L, count = 10)

        assertThat(count1.locationId).isEqualTo(1L)
        assertThat(count1.count).isEqualTo(42)

        assertThat(count1).isEqualTo(count1Same)
        assertThat(count1).isNotEqualTo(count2)
        assertThat(count1.hashCode()).isEqualTo(count1Same.hashCode())

        val copyCount = count1.copy(count = 50)
        assertThat(copyCount.count).isEqualTo(50)
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

        assertThat(rel1).isEqualTo(rel1Same)
        assertThat(rel1).isNotEqualTo(rel2)
        assertThat(rel1.hashCode()).isEqualTo(rel1Same.hashCode())

        val copyRel = rel1.copy(location = null)
        assertThat(copyRel.location).isNull()
    }
}
