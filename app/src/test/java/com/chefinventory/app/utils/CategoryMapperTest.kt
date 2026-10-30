package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés (1-à-1) pour [CategoryMapper].
 */
class CategoryMapperTest {

    @Test
    fun `normalizeRecipeCategory should normalize all category synonyms`() {
        // Starters
        assertThat(CategoryMapper.normalizeRecipeCategory("Entrée froide")).isEqualTo(CategoryMapper.CATEGORY_ENTREE)
        assertThat(CategoryMapper.normalizeRecipeCategory("Starter")).isEqualTo(CategoryMapper.CATEGORY_ENTREE)
        assertThat(CategoryMapper.normalizeRecipeCategory("Appetizer")).isEqualTo(CategoryMapper.CATEGORY_ENTREE)

        // Mains
        assertThat(CategoryMapper.normalizeRecipeCategory("Plat principal")).isEqualTo(CategoryMapper.CATEGORY_PLAT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Viande")).isEqualTo(CategoryMapper.CATEGORY_PLAT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Poisson")).isEqualTo(CategoryMapper.CATEGORY_PLAT)

        // Desserts
        assertThat(CategoryMapper.normalizeRecipeCategory("Dessert gourmand")).isEqualTo(CategoryMapper.CATEGORY_DESSERT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Gâteau au chocolat")).isEqualTo(CategoryMapper.CATEGORY_DESSERT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Pâtisserie")).isEqualTo(CategoryMapper.CATEGORY_DESSERT)

        // Drinks
        assertThat(CategoryMapper.normalizeRecipeCategory("Cocktail")).isEqualTo(CategoryMapper.CATEGORY_BOISSON)
        assertThat(CategoryMapper.normalizeRecipeCategory("Apéritif")).isEqualTo(CategoryMapper.CATEGORY_BOISSON)

        // Sides
        assertThat(CategoryMapper.normalizeRecipeCategory("Sauce béarnaise")).isEqualTo(CategoryMapper.CATEGORY_ACCOMPAGNEMENT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Purée de pommes de terre")).isEqualTo(CategoryMapper.CATEGORY_ACCOMPAGNEMENT)

        // Null and blank
        assertThat(CategoryMapper.normalizeRecipeCategory(null)).isNull()
        assertThat(CategoryMapper.normalizeRecipeCategory("   ")).isNull()
        assertThat(CategoryMapper.normalizeRecipeCategory("Inconnu")).isNull()
    }
}
