package com.chefinventory.app.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [RecipeAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `RecipeAdapter should bind recipe info imageUri and handle click`() {
        var clickedRecipe: Recipe? = null
        val adapter = RecipeAdapter { clickedRecipe = it }

        val recipe = Recipe(
            id = 10L,
            title = "Tarte aux pommes",
            preparationTimeMinutes = 20,
            cookingTimeMinutes = 40,
            kcalPerServing = 350,
            category = "Dessert",
            nutritionInfo = "Glucides: 45g",
            imageUri = "file:///tmp/tarte.jpg",
            updatedAt = 1000L
        )

        adapter.submitList(listOf(recipe))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvRecipeTitle.text.toString()).isEqualTo("Tarte aux pommes")
        assertThat(viewHolder.binding.tvRecipeInfo.text.toString()).contains("20")
        assertThat(viewHolder.binding.tvRecipeInfo.text.toString()).contains("40")
        assertThat(viewHolder.binding.tvRecipeInfo.text.toString()).contains("350")
        assertThat(viewHolder.binding.tvRecipeCategory.text.toString()).contains("Dessert")
        assertThat(viewHolder.binding.tvRecipeCategory.text.toString()).contains("Glucides: 45g")

        viewHolder.binding.root.performClick()
        assertThat(clickedRecipe).isEqualTo(recipe)
    }

    @Test
    fun `RecipeAdapter should bind category only or nutritionInfo only or blank category and prep cook elvis`() {
        val adapter = RecipeAdapter {}

        // 1. Category only, prep only
        val r1 = Recipe(id = 1L, title = "Soupe", category = "Entrée", nutritionInfo = null, preparationTimeMinutes = 15, cookingTimeMinutes = null)
        // 2. NutritionInfo only, cook only
        val r2 = Recipe(id = 2L, title = "Rôti", category = null, nutritionInfo = "Protéines: 30g", preparationTimeMinutes = null, cookingTimeMinutes = 45)
        // 3. Spaces category and spaces nutritionInfo
        val r3 = Recipe(id = 3L, title = "Eau", category = "   ", nutritionInfo = "   ", preparationTimeMinutes = null, cookingTimeMinutes = null)

        adapter.submitList(listOf(r1, r2, r3)) {
            val vh0 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh0, 0)
            assertThat(vh0.binding.tvRecipeCategory.text.toString()).isEqualTo("Entrée")

            val vh1 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh1, 1)
            assertThat(vh1.binding.tvRecipeCategory.text.toString()).isEqualTo("Protéines: 30g")

            val vh2 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh2, 2)
            assertThat(vh2.binding.tvRecipeCategory.text.toString()).isEmpty()
        }
    }

    @Test
    fun `RecipeDiffCallback should compare items correctly`() {
        val diffCallback = RecipeAdapter.RecipeDiffCallback()
        val r1 = Recipe(id = 10L, title = "Crêpes", updatedAt = 1000L)
        val r1Same = Recipe(id = 10L, title = "Crêpes", updatedAt = 1000L)
        val r2 = Recipe(id = 20L, title = "Gaufres", updatedAt = 1000L)

        assertThat(diffCallback.areItemsTheSame(r1, r1Same)).isTrue()
        assertThat(diffCallback.areItemsTheSame(r1, r2)).isFalse()
        assertThat(diffCallback.areContentsTheSame(r1, r1Same)).isTrue()
        assertThat(diffCallback.areContentsTheSame(r1, r2)).isFalse()
    }
}
