package com.example.chiefinventory.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B saturant à 100% les méthodes autogénérées Kotlin
 * (`equals`, `hashCode`, `copy`, `toString`, `componentN`) pour [Recipe], [Ingredient], [SearchCriteria], [StandardCatalogItem].
 */
class ModelDataClassesCoverageExpansionTypeBTest {

    @Test
    fun `Recipe equals hashCode copy componentN and toString should reach 100 percent coverage`() {
        val r1 = Recipe(
            id = 1L, title = "A", instructions = "Inst", preparationTimeMinutes = 10,
            cookingTimeMinutes = 20, restingTimeMinutes = 5, servings = 2, kcalPerServing = 300,
            difficulty = "Facile", category = "Entrée", imageUri = "uri", locationId = 1L,
            wineRecommendation = "Vin", source = "Source", nutritionInfo = "Nutr", priceRating = "€",
            updatedAt = 1000L
        )
        val r1Same = Recipe(
            id = 1L, title = "A", instructions = "Inst", preparationTimeMinutes = 10,
            cookingTimeMinutes = 20, restingTimeMinutes = 5, servings = 2, kcalPerServing = 300,
            difficulty = "Facile", category = "Entrée", imageUri = "uri", locationId = 1L,
            wineRecommendation = "Vin", source = "Source", nutritionInfo = "Nutr", priceRating = "€",
            updatedAt = 1000L
        )
        val rNull = Recipe(id = 0L, title = "")

        // Destructuring / componentN
        val (id, title, instructions, prep, cook, rest, serv, kcal, diff, cat, imgUri) = r1
        assertThat(id).isEqualTo(1L)
        assertThat(title).isEqualTo("A")
        assertThat(instructions).isEqualTo("Inst")
        assertThat(prep).isEqualTo(10)
        assertThat(cook).isEqualTo(20)
        assertThat(rest).isEqualTo(5)
        assertThat(serv).isEqualTo(2)
        assertThat(kcal).isEqualTo(300)
        assertThat(diff).isEqualTo("Facile")
        assertThat(cat).isEqualTo("Entrée")
        assertThat(imgUri).isEqualTo("uri")

        // Component 12 to 17
        assertThat(r1.component12()).isEqualTo(1L)
        assertThat(r1.component13()).isEqualTo("Vin")
        assertThat(r1.component14()).isEqualTo("Source")
        assertThat(r1.component15()).isEqualTo("Nutr")
        assertThat(r1.component16()).isEqualTo("€")
        assertThat(r1.component17()).isEqualTo(1000L)

        // Equals branches
        assertThat(r1.equals(r1)).isTrue()
        assertThat(r1.equals("NotARecipe")).isFalse()
        assertThat(r1).isEqualTo(r1Same)
        assertThat(r1.hashCode()).isEqualTo(r1Same.hashCode())
        assertThat(rNull.hashCode()).isNotEqualTo(r1.hashCode())

        // Testing copy with every single parameter changed to trigger copy() branches
        assertThat(r1.copy(id = 2L)).isNotEqualTo(r1)
        assertThat(r1.copy(title = "B")).isNotEqualTo(r1)
        assertThat(r1.copy(instructions = "Other")).isNotEqualTo(r1)
        assertThat(r1.copy(preparationTimeMinutes = 99)).isNotEqualTo(r1)
        assertThat(r1.copy(cookingTimeMinutes = 99)).isNotEqualTo(r1)
        assertThat(r1.copy(restingTimeMinutes = 99)).isNotEqualTo(r1)
        assertThat(r1.copy(servings = 99)).isNotEqualTo(r1)
        assertThat(r1.copy(kcalPerServing = 99)).isNotEqualTo(r1)
        assertThat(r1.copy(difficulty = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(category = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(imageUri = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(locationId = 99L)).isNotEqualTo(r1)
        assertThat(r1.copy(wineRecommendation = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(source = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(nutritionInfo = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(priceRating = "Autre")).isNotEqualTo(r1)
        assertThat(r1.copy(updatedAt = 9999L)).isNotEqualTo(r1)

        assertThat(r1.toString()).contains("A")
    }

    @Test
    fun `Ingredient equals hashCode copy componentN and toString should reach 100 percent coverage`() {
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
    fun `SearchCriteria and StandardCatalogItem equals hashCode copy componentN should reach 100 percent coverage`() {
        val s1 = SearchCriteria(
            titre = "Titre", editeur = "Edit", annee = 2000, mois = 1,
            superCategorie = "Super", categorie = "Cat", description = "Desc",
            tirage = "Tirage", dimensions = "Dim", isPossessed = true
        )
        val s1Same = SearchCriteria(
            titre = "Titre", editeur = "Edit", annee = 2000, mois = 1,
            superCategorie = "Super", categorie = "Cat", description = "Desc",
            tirage = "Tirage", dimensions = "Dim", isPossessed = true
        )
        val sNull = SearchCriteria()

        val (t, _, a, _, sC, c, d, tr, dim, p) = s1
        assertThat(t).isEqualTo("Titre")
        assertThat(s1).isEqualTo(s1Same)
        assertThat(s1.hashCode()).isEqualTo(s1Same.hashCode())
        assertThat(sNull.hashCode()).isNotEqualTo(s1.hashCode())

        assertThat(s1.copy(titre = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(editeur = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(annee = 99)).isNotEqualTo(s1)
        assertThat(s1.copy(mois = 99)).isNotEqualTo(s1)
        assertThat(s1.copy(superCategorie = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(categorie = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(description = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(tirage = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(dimensions = "X")).isNotEqualTo(s1)
        assertThat(s1.copy(isPossessed = false)).isNotEqualTo(s1)

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
}
