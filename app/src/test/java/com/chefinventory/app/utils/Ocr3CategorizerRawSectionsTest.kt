package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour le conteneur de données [RawSections].
 */
class Ocr3CategorizerRawSectionsTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRawSections_DefaultValuesAndGeneratedMethodsCoverage() {
        val defaultSections = RawSections()

        assertWithMessage("Les listes brutes par défaut doivent être vides")
            .that(defaultSections.rawIngredientsList)
            .isEmpty()

        assertWithMessage("Le titre par défaut doit être null")
            .that(defaultSections.detectedTitle)
            .isNull()

        val sections1 = RawSections(
            detectedTitle = "Salade de surimi",
            detectedServings = "4",
            detectedPrepTime = "15",
            detectedCookTime = "20",
            detectedRestingTime = "10",
            detectedKcal = "300",
            detectedDifficulty = "Facile",
            detectedPriceRating = "2",
            detectedNutritionInfo = "10g lipides",
            detectedNutriScore = "A",
            ingredientKeywords = setOf("surimi", "citron")
        )

        val sections2 = sections1.copy()
        val sections3 = sections1.copy(detectedTitle = "Autre titre")

        assertWithMessage("Les objets RawSections identiques doivent être égaux")
            .that(sections1)
            .isEqualTo(sections2)

        assertWithMessage("Les objets RawSections identiques doivent avoir le même hashCode")
            .that(sections1.hashCode())
            .isEqualTo(sections2.hashCode())

        assertWithMessage("Les objets RawSections avec titre différent ne doivent pas être égaux")
            .that(sections1)
            .isNotEqualTo(sections3)

        assertWithMessage("La méthode toString() doit contenir le nom de la classe et le titre")
            .that(sections1.toString())
            .contains("Salade de surimi")

        // Destructuring / Component methods test
        val (ingList, instList, wineList, sourceList, title, servings, prepTime, cookTime, restTime, kcal, diff, price, nutri, score, keywords) = sections1
        assertThat(ingList).isEmpty()
        assertThat(instList).isEmpty()
        assertThat(wineList).isEmpty()
        assertThat(sourceList).isEmpty()
        assertThat(title).isEqualTo("Salade de surimi")
        assertThat(servings).isEqualTo("4")
        assertThat(prepTime).isEqualTo("15")
        assertThat(cookTime).isEqualTo("20")
        assertThat(restTime).isEqualTo("10")
        assertThat(kcal).isEqualTo("300")
        assertThat(diff).isEqualTo("Facile")
        assertThat(price).isEqualTo("2")
        assertThat(nutri).isEqualTo("10g lipides")
        assertThat(score).isEqualTo("A")
        assertThat(keywords).contains("surimi")
    }
}
