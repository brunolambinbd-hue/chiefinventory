package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Recipe
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
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `RecipeAdapter should bind recipe info and handle click`() {
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
    fun `RecipeAdapter should bind recipe with blank category and nutritionInfo`() {
        val adapter = RecipeAdapter {}
        val recipe = Recipe(
            id = 20L,
            title = "Eau",
            preparationTimeMinutes = null,
            cookingTimeMinutes = null,
            kcalPerServing = null,
            category = null,
            nutritionInfo = null,
            updatedAt = 1000L
        )

        adapter.submitList(listOf(recipe))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvRecipeTitle.text.toString()).isEqualTo("Eau")
        assertThat(viewHolder.binding.tvRecipeCategory.text.toString()).isEmpty()
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
