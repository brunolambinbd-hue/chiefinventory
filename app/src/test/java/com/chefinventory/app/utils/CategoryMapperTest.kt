package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [CategoryMapper].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategoryMapperTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalizeRecipeCategory_WithNullResources_AndWithResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(R.style.Theme_ChefInventory)
        val res = context.resources

        // Null resources (fallback hardcoded keywords)
        assertThat(CategoryMapper.normalizeRecipeCategory("Entrée froide", null)).isEqualTo(CategoryMapper.CATEGORY_ENTREE)
        assertThat(CategoryMapper.normalizeRecipeCategory("Plat principal", null)).isEqualTo(CategoryMapper.CATEGORY_PLAT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Gâteau au chocolat", null)).isEqualTo(CategoryMapper.CATEGORY_DESSERT)
        assertThat(CategoryMapper.normalizeRecipeCategory("Cocktail", null)).isEqualTo(CategoryMapper.CATEGORY_BOISSON)
        assertThat(CategoryMapper.normalizeRecipeCategory("Sauce béarnaise", null)).isEqualTo(CategoryMapper.CATEGORY_ACCOMPAGNEMENT)
        assertThat(CategoryMapper.normalizeRecipeCategory(null, null)).isNull()
        assertThat(CategoryMapper.normalizeRecipeCategory("   ", null)).isNull()
        assertThat(CategoryMapper.normalizeRecipeCategory("Inconnu123", null)).isNull()

        // With Android Resources (localizes to string resources)
        assertThat(CategoryMapper.normalizeRecipeCategory("Cocktail", res)).isEqualTo(context.getString(R.string.category_recipe_boisson))
        assertThat(CategoryMapper.normalizeRecipeCategory("Sauce", res)).isEqualTo(context.getString(R.string.category_recipe_accompagnement))
        assertThat(CategoryMapper.normalizeRecipeCategory("Starter", res)).isEqualTo(context.getString(R.string.category_recipe_entree))
        assertThat(CategoryMapper.normalizeRecipeCategory("Inconnu123", res)).isNull()
    }
}
