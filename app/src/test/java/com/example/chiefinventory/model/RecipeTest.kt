package com.example.chiefinventory.model

import android.os.Parcel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés pour la classe de données [Recipe].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeTest {

    @Test
    fun `Recipe all 17 properties getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val rFull = Recipe(
            id = 10L,
            title = "Pintade aux pommes",
            instructions = "Éplucher les pommes et cuire au four.",
            preparationTimeMinutes = 20,
            cookingTimeMinutes = 45,
            restingTimeMinutes = 10,
            servings = 4,
            kcalPerServing = 450,
            difficulty = "facile",
            category = "Plat principal",
            imageUri = "file:///tmp/pintade.jpg",
            locationId = 100L,
            wineRecommendation = "Bordeaux Rouge",
            source = "https://marmiton.org/recette.html",
            nutritionInfo = "Protéines: 35g, Glucides: 12g",
            priceRating = "moyen",
            updatedAt = 1000L
        )

        val rFullSame = Recipe(
            id = 10L,
            title = "Pintade aux pommes",
            instructions = "Éplucher les pommes et cuire au four.",
            preparationTimeMinutes = 20,
            cookingTimeMinutes = 45,
            restingTimeMinutes = 10,
            servings = 4,
            kcalPerServing = 450,
            difficulty = "facile",
            category = "Plat principal",
            imageUri = "file:///tmp/pintade.jpg",
            locationId = 100L,
            wineRecommendation = "Bordeaux Rouge",
            source = "https://marmiton.org/recette.html",
            nutritionInfo = "Protéines: 35g, Glucides: 12g",
            priceRating = "moyen",
            updatedAt = 1000L
        )

        // Getters
        assertThat(rFull.id).isEqualTo(10L)
        assertThat(rFull.title).isEqualTo("Pintade aux pommes")
        assertThat(rFull.instructions).isEqualTo("Éplucher les pommes et cuire au four.")
        assertThat(rFull.preparationTimeMinutes).isEqualTo(20)
        assertThat(rFull.cookingTimeMinutes).isEqualTo(45)
        assertThat(rFull.restingTimeMinutes).isEqualTo(10)
        assertThat(rFull.servings).isEqualTo(4)
        assertThat(rFull.kcalPerServing).isEqualTo(450)
        assertThat(rFull.difficulty).isEqualTo("facile")
        assertThat(rFull.category).isEqualTo("Plat principal")
        assertThat(rFull.imageUri).isEqualTo("file:///tmp/pintade.jpg")
        assertThat(rFull.locationId).isEqualTo(100L)
        assertThat(rFull.wineRecommendation).isEqualTo("Bordeaux Rouge")
        assertThat(rFull.source).isEqualTo("https://marmiton.org/recette.html")
        assertThat(rFull.nutritionInfo).isEqualTo("Protéines: 35g, Glucides: 12g")
        assertThat(rFull.priceRating).isEqualTo("moyen")
        assertThat(rFull.updatedAt).isEqualTo(1000L)

        // Destructuring / componentN (1 à 17)
        val (id, title, inst, prep, cook, rest, serv, kcal, diff, cat, img, loc, wine, src, nutr, price, update) = rFull
        assertThat(id).isEqualTo(10L)
        assertThat(title).isEqualTo("Pintade aux pommes")
        assertThat(inst).isEqualTo("Éplucher les pommes et cuire au four.")
        assertThat(prep).isEqualTo(20)
        assertThat(cook).isEqualTo(45)
        assertThat(rest).isEqualTo(10)
        assertThat(serv).isEqualTo(4)
        assertThat(kcal).isEqualTo(450)
        assertThat(diff).isEqualTo("facile")
        assertThat(cat).isEqualTo("Plat principal")
        assertThat(img).isEqualTo("file:///tmp/pintade.jpg")
        assertThat(loc).isEqualTo(100L)
        assertThat(wine).isEqualTo("Bordeaux Rouge")
        assertThat(src).isEqualTo("https://marmiton.org/recette.html")
        assertThat(nutr).isEqualTo("Protéines: 35g, Glucides: 12g")
        assertThat(price).isEqualTo("moyen")
        assertThat(update).isEqualTo(1000L)

        // Equals
        assertThat(rFull.equals(rFull)).isTrue()
        assertThat(rFull.equals(null)).isFalse()
        assertThat(rFull.equals("NotARecipe")).isFalse()
        assertThat(rFull).isEqualTo(rFullSame)

        // Discrepancy branches on every single property via copy
        assertThat(rFull).isNotEqualTo(rFull.copy(id = 99L))
        assertThat(rFull).isNotEqualTo(rFull.copy(title = "X"))
        assertThat(rFull).isNotEqualTo(rFull.copy(instructions = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(preparationTimeMinutes = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(cookingTimeMinutes = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(restingTimeMinutes = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(servings = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(kcalPerServing = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(difficulty = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(category = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(imageUri = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(locationId = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(wineRecommendation = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(source = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(nutritionInfo = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(priceRating = null))
        assertThat(rFull).isNotEqualTo(rFull.copy(updatedAt = 9999L))

        // HashCode & ToString
        assertThat(rFull.hashCode()).isEqualTo(rFullSame.hashCode())
        assertThat(rFull.hashCode()).isNotEqualTo(rFull.copy(title = "X").hashCode())
        assertThat(rFull.toString()).contains("Pintade aux pommes")
    }

    @Test
    fun `Recipe Parcelable with full non-null values and with null values should cover all writeToParcel branches`() {
        val rFull = Recipe(
            id = 10L,
            title = "Soupe à l'oignon",
            instructions = "Faire revenir les oignons",
            preparationTimeMinutes = 15,
            cookingTimeMinutes = 30,
            restingTimeMinutes = 5,
            servings = 6,
            kcalPerServing = 280,
            difficulty = "facile",
            category = "Entrée",
            imageUri = "file:///tmp/soupe.jpg",
            locationId = 200L,
            wineRecommendation = "Riesling",
            source = "https://marmiton.org/soupe",
            nutritionInfo = "Protéines: 8g",
            priceRating = "bon marché",
            updatedAt = 2000L
        )

        val rNulls = Recipe(
            id = 20L,
            title = "Eau chaude",
            instructions = null,
            preparationTimeMinutes = null,
            cookingTimeMinutes = null,
            restingTimeMinutes = null,
            servings = null,
            kcalPerServing = null,
            difficulty = null,
            category = null,
            imageUri = null,
            locationId = null,
            wineRecommendation = null,
            source = null,
            nutritionInfo = null,
            priceRating = null,
            updatedAt = 3000L
        )

        assertThat(rFull.describeContents()).isEqualTo(0)
        assertThat(rNulls.describeContents()).isEqualTo(0)

        @Suppress("UNCHECKED_CAST")
        val creator = Recipe::class.java.getField("CREATOR").get(null) as android.os.Parcelable.Creator<Recipe>

        // Test 1 : Full Non-Null Parceling
        val parcelFull = Parcel.obtain()
        rFull.writeToParcel(parcelFull, 0)
        parcelFull.setDataPosition(0)

        val createdFull = creator.createFromParcel(parcelFull)
        assertThat(createdFull).isEqualTo(rFull)
        assertThat(createdFull.instructions).isEqualTo("Faire revenir les oignons")
        assertThat(createdFull.preparationTimeMinutes).isEqualTo(15)
        assertThat(createdFull.wineRecommendation).isEqualTo("Riesling")
        assertThat(createdFull.source).isEqualTo("https://marmiton.org/soupe")
        assertThat(createdFull.nutritionInfo).isEqualTo("Protéines: 8g")
        assertThat(createdFull.priceRating).isEqualTo("bon marché")
        parcelFull.recycle()

        // Test 2 : All-Nulls Parceling
        val parcelNulls = Parcel.obtain()
        rNulls.writeToParcel(parcelNulls, 0)
        parcelNulls.setDataPosition(0)

        val createdNulls = creator.createFromParcel(parcelNulls)
        assertThat(createdNulls).isEqualTo(rNulls)
        assertThat(createdNulls.instructions).isNull()
        assertThat(createdNulls.preparationTimeMinutes).isNull()
        assertThat(createdNulls.cookingTimeMinutes).isNull()
        assertThat(createdNulls.restingTimeMinutes).isNull()
        assertThat(createdNulls.servings).isNull()
        assertThat(createdNulls.kcalPerServing).isNull()
        assertThat(createdNulls.difficulty).isNull()
        assertThat(createdNulls.category).isNull()
        assertThat(createdNulls.imageUri).isNull()
        assertThat(createdNulls.locationId).isNull()
        assertThat(createdNulls.wineRecommendation).isNull()
        assertThat(createdNulls.source).isNull()
        assertThat(createdNulls.nutritionInfo).isNull()
        assertThat(createdNulls.priceRating).isNull()
        parcelNulls.recycle()

        // Test 3 : CREATOR.newArray
        val array = creator.newArray(5)
        assertThat(array.size).isEqualTo(5)
    }
}
