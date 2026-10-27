package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant que le filtrage sur l'onglet Boissons
 * ne conserve que les vraies boissons (ex: 20 recettes) et n'inclut pas les plats au jus de cuisson/jus de citron.
 */
class MatchRecipeBoissonTabFilterTypeBTest {

    private fun isRecipeInCourseCategory(recipe: Recipe, selectedCourse: String): Boolean {
        val sourceCat = CategoryMapper.normalizeRecipeCategory(recipe.source)
        val titleCat = CategoryMapper.normalizeRecipeCategory(recipe.title)
        val effectiveCourse = sourceCat ?: titleCat ?: "Plat principal"

        return when (selectedCourse) {
            "Entrée" -> effectiveCourse == "Entrée"
            "Plat" -> effectiveCourse == "Plat principal"
            "Dessert" -> effectiveCourse == "Dessert"
            "Boisson" -> effectiveCourse == "Boisson"
            "Accompagnement" -> effectiveCourse == "Accompagnement"
            else -> true
        }
    }

    @Test
    fun `isRecipeInCourseCategory should match drink recipes only for Boisson tab and exclude dishes with cooking juice`() {
        val drink = Recipe(id = 1L, title = "Jus de Tamarin", source = "Boisson | https://fr.wikibooks.org/wiki/Jus_de_tamarin", instructions = "Ajouter le jus")
        val dishWithJuice = Recipe(id = 2L, title = "Pintade à la normande", source = "Plat principal | https://...", instructions = "Mettre le jus de la bête et du jus de citron")
        val cassoulet = Recipe(id = 3L, title = "Cassoulet", source = "Plat principal | https://...", instructions = "Faire mijoter")

        assertThat(isRecipeInCourseCategory(drink, "Boisson")).isTrue()
        assertThat(isRecipeInCourseCategory(dishWithJuice, "Boisson")).isFalse()
        assertThat(isRecipeInCourseCategory(cassoulet, "Boisson")).isFalse()
    }
}
