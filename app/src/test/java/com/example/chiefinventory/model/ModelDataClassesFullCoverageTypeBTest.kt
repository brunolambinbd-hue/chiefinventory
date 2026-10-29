package com.example.chiefinventory.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B assurant la couverture à 100% de toutes les Data Classes
 * du paquetage [com.example.chiefinventory.model].
 */
class ModelDataClassesFullCoverageTypeBTest {

    @Test
    fun `CollectionItem properties equals hashCode copy and componentN should function properly`() {
        val bytes1 = byteArrayOf(10, 20, 30)
        val bytes2 = byteArrayOf(10, 20, 30)
        val time = 123456789L

        val item1 = CollectionItem(
            id = 1L, remoteId = 10, titre = "BD Spirou", editeur = "Dupuis", annee = 1980, mois = 5,
            categorie = "Album", superCategorie = "BD", materiau = "Papier", tirage = "1000",
            dimensions = "A4", prixAchat = 15.0, valeurEstimee = 30.0, lieuAchat = "Brocante",
            description = "TBE", imageUri = "uri1", imageEmbedding = bytes1, ocrText = "OCR",
            locationId = 2L, isPossessed = true, updatedAt = time
        )

        val item1Same = CollectionItem(
            id = 1L, remoteId = 10, titre = "BD Spirou", editeur = "Dupuis", annee = 1980, mois = 5,
            categorie = "Album", superCategorie = "BD", materiau = "Papier", tirage = "1000",
            dimensions = "A4", prixAchat = 15.0, valeurEstimee = 30.0, lieuAchat = "Brocante",
            description = "TBE", imageUri = "uri1", imageEmbedding = bytes2, ocrText = "OCR",
            locationId = 2L, isPossessed = true, updatedAt = time
        )

        val item2 = CollectionItem(id = 2L, titre = "BD Tintin", updatedAt = time)

        assertThat(item1.id).isEqualTo(1L)
        assertThat(item1.remoteId).isEqualTo(10)
        assertThat(item1.titre).isEqualTo("BD Spirou")
        assertThat(item1.editeur).isEqualTo("Dupuis")
        assertThat(item1.annee).isEqualTo(1980)
        assertThat(item1.mois).isEqualTo(5)
        assertThat(item1.categorie).isEqualTo("Album")
        assertThat(item1.superCategorie).isEqualTo("BD")
        assertThat(item1.materiau).isEqualTo("Papier")
        assertThat(item1.tirage).isEqualTo("1000")
        assertThat(item1.dimensions).isEqualTo("A4")
        assertThat(item1.prixAchat).isEqualTo(15.0)
        assertThat(item1.valeurEstimee).isEqualTo(30.0)
        assertThat(item1.lieuAchat).isEqualTo("Brocante")
        assertThat(item1.description).isEqualTo("TBE")
        assertThat(item1.imageUri).isEqualTo("uri1")
        assertThat(item1.imageEmbedding).isEqualTo(bytes1)
        assertThat(item1.ocrText).isEqualTo("OCR")
        assertThat(item1.locationId).isEqualTo(2L)
        assertThat(item1.isPossessed).isTrue()
        assertThat(item1.updatedAt).isEqualTo(time)

        // Test equals & hashCode
        assertThat(item1).isEqualTo(item1Same)
        assertThat(item1).isNotEqualTo(item2)
        assertThat(item1.hashCode()).isEqualTo(item1Same.hashCode())

        // Test branches equals()
        assertThat(item1.equals(item1)).isTrue()
        assertThat(item1.equals("Other")).isFalse()
        assertThat(item1.equals(item1.copy(id = 99L))).isFalse()
        assertThat(item1.equals(item1.copy(titre = "Autre"))).isFalse()
        assertThat(item1.equals(item1.copy(imageEmbedding = null))).isFalse()
        assertThat(item1.equals(item1.copy(ocrText = "Autre OCR"))).isFalse()
        assertThat(item1.equals(item1.copy(updatedAt = 999L))).isFalse()
        assertThat(item2.equals(item1)).isFalse()

        // Test copy & toString
        val copyItem = item1.copy(titre = "Copie")
        assertThat(copyItem.titre).isEqualTo("Copie")
        assertThat(item1.toString()).contains("BD Spirou")

        // Destructuring / ComponentN
        val (id, remoteId, titre) = item1
        assertThat(id).isEqualTo(1L)
        assertThat(remoteId).isEqualTo(10)
        assertThat(titre).isEqualTo("BD Spirou")
    }

    @Test
    fun `Recipe properties equals hashCode copy and componentN should function properly`() {
        val time = 1000L
        val recipe1 = Recipe(
            id = 10L, title = "Carbonnade", instructions = "Mijoter", preparationTimeMinutes = 15,
            cookingTimeMinutes = 90, restingTimeMinutes = 0, servings = 4, kcalPerServing = 650,
            difficulty = "Facile", category = "Plat principal", imageUri = "uri", locationId = 1L,
            wineRecommendation = "Bière", source = "Grand-Mère", nutritionInfo = "Proteines",
            priceRating = "Bon marché", updatedAt = time
        )

        val recipe1Same = Recipe(
            id = 10L, title = "Carbonnade", instructions = "Mijoter", preparationTimeMinutes = 15,
            cookingTimeMinutes = 90, restingTimeMinutes = 0, servings = 4, kcalPerServing = 650,
            difficulty = "Facile", category = "Plat principal", imageUri = "uri", locationId = 1L,
            wineRecommendation = "Bière", source = "Grand-Mère", nutritionInfo = "Proteines",
            priceRating = "Bon marché", updatedAt = time
        )

        val recipe2 = Recipe(id = 20L, title = "Tiramisu", updatedAt = time)

        assertThat(recipe1.title).isEqualTo("Carbonnade")
        assertThat(recipe1.instructions).isEqualTo("Mijoter")
        assertThat(recipe1.preparationTimeMinutes).isEqualTo(15)
        assertThat(recipe1.cookingTimeMinutes).isEqualTo(90)
        assertThat(recipe1.servings).isEqualTo(4)
        assertThat(recipe1.kcalPerServing).isEqualTo(650)
        assertThat(recipe1.difficulty).isEqualTo("Facile")
        assertThat(recipe1.category).isEqualTo("Plat principal")
        assertThat(recipe1.imageUri).isEqualTo("uri")
        assertThat(recipe1.locationId).isEqualTo(1L)

        assertThat(recipe1).isEqualTo(recipe1Same)
        assertThat(recipe1).isNotEqualTo(recipe2)
        assertThat(recipe1.hashCode()).isEqualTo(recipe1Same.hashCode())

        val copyRecipe = recipe1.copy(title = "Nouveau Titre")
        assertThat(copyRecipe.title).isEqualTo("Nouveau Titre")
        assertThat(recipe1.toString()).contains("Carbonnade")

        val (id, title, instructions) = recipe1
        assertThat(id).isEqualTo(10L)
        assertThat(title).isEqualTo("Carbonnade")
        assertThat(instructions).isEqualTo("Mijoter")
    }

    @Test
    fun `Ingredient properties equals hashCode copy and componentN should function properly`() {
        val bytes = byteArrayOf(1, 2)
        val time = 2000L
        val ingr1 = Ingredient(
            id = 50L, name = "Beurre", quantity = 250.0, unit = "g", category = "Crémerie",
            expirationDate = 12345L, description = "Doux", supplementalInfo = "50% M.G.",
            imageUri = "uri", imageEmbedding = bytes, ocrText = "Beurre 250g", locationId = 1L,
            stockPolicy = "PERMANENT", minThreshold = 100.0, packSize = 1.0, updatedAt = time
        )

        val ingr1Same = Ingredient(
            id = 50L, name = "Beurre", quantity = 250.0, unit = "g", category = "Crémerie",
            expirationDate = 12345L, description = "Doux", supplementalInfo = "50% M.G.",
            imageUri = "uri", imageEmbedding = bytes, ocrText = "Beurre 250g", locationId = 1L,
            stockPolicy = "PERMANENT", minThreshold = 100.0, packSize = 1.0, updatedAt = time
        )

        val ingr2 = Ingredient(id = 60L, name = "Lait")

        assertThat(ingr1.name).isEqualTo("Beurre")
        assertThat(ingr1.quantity).isEqualTo(250.0)
        assertThat(ingr1.unit).isEqualTo("g")
        assertThat(ingr1.category).isEqualTo("Crémerie")
        assertThat(ingr1.stockPolicy).isEqualTo("PERMANENT")
        assertThat(ingr1.minThreshold).isEqualTo(100.0)

        assertThat(ingr1).isEqualTo(ingr1Same)
        assertThat(ingr1).isNotEqualTo(ingr2)
        assertThat(ingr1.hashCode()).isEqualTo(ingr1Same.hashCode())

        val copyIngr = ingr1.copy(quantity = 500.0)
        assertThat(copyIngr.quantity).isEqualTo(500.0)
        assertThat(ingr1.toString()).contains("Beurre")

        val (id, name, quantity) = ingr1
        assertThat(id).isEqualTo(50L)
        assertThat(name).isEqualTo("Beurre")
        assertThat(quantity).isEqualTo(250.0)
    }

    @Test
    fun `SearchCriteria properties equals hashCode copy and componentN should function properly`() {
        val crit1 = SearchCriteria(
            titre = "Spirou", editeur = "Dupuis", annee = 1980, mois = 5,
            superCategorie = "BD", categorie = "Album", description = "Rare",
            tirage = "1000", dimensions = "A4", isPossessed = true
        )

        val crit1Same = SearchCriteria(
            titre = "Spirou", editeur = "Dupuis", annee = 1980, mois = 5,
            superCategorie = "BD", categorie = "Album", description = "Rare",
            tirage = "1000", dimensions = "A4", isPossessed = true
        )

        val crit2 = SearchCriteria(titre = "Tintin")

        assertThat(crit1.titre).isEqualTo("Spirou")
        assertThat(crit1.editeur).isEqualTo("Dupuis")
        assertThat(crit1.annee).isEqualTo(1980)

        assertThat(crit1).isEqualTo(crit1Same)
        assertThat(crit1).isNotEqualTo(crit2)
        assertThat(crit1.hashCode()).isEqualTo(crit1Same.hashCode())

        val copyCrit = crit1.copy(titre = "Autre")
        assertThat(copyCrit.titre).isEqualTo("Autre")
        assertThat(crit1.toString()).contains("Spirou")
    }

    @Test
    fun `StandardCatalogItem properties equals hashCode copy and componentN should function properly`() {
        val item1 = StandardCatalogItem(
            name = "Lait", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie",
            defaultLocationName = "Frigo", stockPolicy = "PERMANENT", minThreshold = 0.25, packSize = 6.0
        )

        val item1Same = StandardCatalogItem(
            name = "Lait", defaultQuantity = 1.0, defaultUnit = "L", category = "Crémerie",
            defaultLocationName = "Frigo", stockPolicy = "PERMANENT", minThreshold = 0.25, packSize = 6.0
        )

        val item2 = StandardCatalogItem(name = "Beurre", defaultQuantity = 100.0)

        assertThat(item1.name).isEqualTo("Lait")
        assertThat(item1.defaultQuantity).isEqualTo(1.0)
        assertThat(item1.packSize).isEqualTo(6.0)

        assertThat(item1).isEqualTo(item1Same)
        assertThat(item1).isNotEqualTo(item2)
        assertThat(item1.hashCode()).isEqualTo(item1Same.hashCode())

        val copyItem = item1.copy(name = "Lait bio")
        assertThat(copyItem.name).isEqualTo("Lait bio")
        assertThat(item1.toString()).contains("Lait")
    }

    @Test
    fun `LocationType values and valueOf should function properly`() {
        val entries = LocationType.entries
        assertThat(entries).contains(LocationType.COLLECTION)
        assertThat(entries).contains(LocationType.INGREDIENT)
        assertThat(entries).contains(LocationType.RECIPE)

        assertThat(LocationType.valueOf("INGREDIENT")).isEqualTo(LocationType.INGREDIENT)
    }
}
