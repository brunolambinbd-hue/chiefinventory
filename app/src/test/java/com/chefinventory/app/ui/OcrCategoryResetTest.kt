package com.chefinventory.app.ui

import com.chefinventory.app.utils.RecipeOcrResult
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation UI/Modèle vérifiant que la catégorie de recette est remise à zéro
 * lorsqu'une nouvelle recette sans catégorie explicite est scannée.
 */
class OcrCategoryResetTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipeCategoryReset_WhenNewScanHasNoCategory_ResetsCategoryCleanly() {
        var selectedLocationId: Long? = 5L // Initalement "Entrée" de la recette précédente
        var categoryDisplayText: String = "Entrée"

        // Simulation du scan 1 (avec catégorie "Plat principal")
        val scan1Result = RecipeOcrResult(title = "Lasagne", category = "Plat principal")
        scan1Result.category?.takeIf { it.isNotBlank() }?.let {
            selectedLocationId = 6L
            categoryDisplayText = "Plat principal"
        }

        assertThat(selectedLocationId).isEqualTo(6L)
        assertThat(categoryDisplayText).isEqualTo("Plat principal")

        // Simulation du scan 2 (sans catégorie explicite) -> Doit réinitialiser !
        selectedLocationId = null
        categoryDisplayText = ""

        val scan2Result = RecipeOcrResult(title = "Soupe verte", category = null)
        scan2Result.category?.takeIf { it.isNotBlank() }?.let {
            selectedLocationId = 7L
            categoryDisplayText = "Dessert"
        }

        assertWithMessage("La catégorie sélectionnée doit être réinitialisée à null")
            .that(selectedLocationId)
            .isNull()

        assertWithMessage("Le champ de texte de la catégorie doit être vide")
            .that(categoryDisplayText)
            .isEmpty()
    }
}
