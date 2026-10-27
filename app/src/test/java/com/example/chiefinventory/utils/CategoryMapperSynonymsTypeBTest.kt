package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le mappage centralisé des synonymes de catégories
 * (JaCoCo 100% de couverture de toutes les branches).
 */
class CategoryMapperSynonymsTypeBTest {

    @Test
    fun `normalizeRecipeCategory should map all synonyms to 5 canonical course categories`() {
        // Branche 1 : Boisson / Cocktail
        assertThat(CategoryMapper.normalizeRecipeCategory("Cocktails")).isEqualTo("Boisson")
        assertThat(CategoryMapper.normalizeRecipeCategory("Jus de fruits")).isEqualTo("Boisson")
        assertThat(CategoryMapper.normalizeRecipeCategory("Apéritif")).isEqualTo("Boisson")

        // Branche 2 : Accompagnement / Sauce
        assertThat(CategoryMapper.normalizeRecipeCategory("Sauce")).isEqualTo("Accompagnement")
        assertThat(CategoryMapper.normalizeRecipeCategory("Garniture")).isEqualTo("Accompagnement")
        assertThat(CategoryMapper.normalizeRecipeCategory("Purée")).isEqualTo("Accompagnement")

        // Branche 3 : Entrée
        assertThat(CategoryMapper.normalizeRecipeCategory("Starter")).isEqualTo("Entrée")
        assertThat(CategoryMapper.normalizeRecipeCategory("Amuse-bouche")).isEqualTo("Entrée")
        assertThat(CategoryMapper.normalizeRecipeCategory("Hors d'œuvre")).isEqualTo("Entrée")

        // Branche 4 : Plat principal
        assertThat(CategoryMapper.normalizeRecipeCategory("Main course")).isEqualTo("Plat principal")
        assertThat(CategoryMapper.normalizeRecipeCategory("Viande")).isEqualTo("Plat principal")

        // Branche 5 : Dessert
        assertThat(CategoryMapper.normalizeRecipeCategory("Gâteau")).isEqualTo("Dessert")
        assertThat(CategoryMapper.normalizeRecipeCategory("Pâtisserie")).isEqualTo("Dessert")
        assertThat(CategoryMapper.normalizeRecipeCategory("Douceur")).isEqualTo("Dessert")

        // Branche 6 : Null / Blank
        assertThat(CategoryMapper.normalizeRecipeCategory(null)).isNull()
        assertThat(CategoryMapper.normalizeRecipeCategory("   ")).isNull()
    }
}
