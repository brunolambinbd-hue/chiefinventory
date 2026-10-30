package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.utils.RecipeMatchResult
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [MatchRecipeAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MatchRecipeAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `MatchRecipeAdapter should bind 100 percent match recipe and trigger click`() {
        var clickedResult: RecipeMatchResult? = null
        val adapter = MatchRecipeAdapter { clickedResult = it }

        val recipe = Recipe(id = 10L, title = "Crêpes")
        val matchResult = RecipeMatchResult(
            recipe = recipe,
            matchPercentage = 100.0,
            totalInStock = 3,
            totalRequired = 3,
            missingIngredients = emptyList()
        )

        adapter.submitList(listOf(matchResult))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvRecipeTitle.text.toString()).isEqualTo("Crêpes")
        assertThat(viewHolder.binding.tvMatchPercentage.text.toString()).isEqualTo("100% Stock")
        assertThat(viewHolder.binding.tvStockDetails.text.toString()).contains("100%")
        assertThat(viewHolder.binding.tvMissingDetails.visibility).isEqualTo(View.GONE)

        viewHolder.binding.root.performClick()
        assertThat(clickedResult).isEqualTo(matchResult)
    }

    @Test
    fun `MatchRecipeAdapter should bind partial match recipe over 50 percent`() {
        val adapter = MatchRecipeAdapter {}
        val recipe = Recipe(id = 20L, title = "Gaufres")
        val matchResult = RecipeMatchResult(
            recipe = recipe,
            matchPercentage = 75.0,
            totalInStock = 3,
            totalRequired = 4,
            missingIngredients = listOf("Farine")
        )

        adapter.submitList(listOf(matchResult))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvRecipeTitle.text.toString()).isEqualTo("Gaufres")
        assertThat(viewHolder.binding.tvMatchPercentage.text.toString()).isEqualTo("75% Stock")
        assertThat(viewHolder.binding.tvMissingDetails.visibility).isEqualTo(View.VISIBLE)
        assertThat(viewHolder.binding.tvMissingDetails.text.toString()).contains("Farine")
    }

    @Test
    fun `MatchRecipeAdapter should bind low match recipe under 50 percent`() {
        val adapter = MatchRecipeAdapter {}
        val recipe = Recipe(id = 30L, title = "Tarte")
        val matchResult = RecipeMatchResult(
            recipe = recipe,
            matchPercentage = 25.0,
            totalInStock = 1,
            totalRequired = 4,
            missingIngredients = listOf("Pommes", "Beurre", "Sucre")
        )

        adapter.submitList(listOf(matchResult))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvRecipeTitle.text.toString()).isEqualTo("Tarte")
        assertThat(viewHolder.binding.tvMatchPercentage.text.toString()).isEqualTo("25% Stock")
        assertThat(viewHolder.binding.tvMissingDetails.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should compare items correctly`() {
        val recipe1 = Recipe(id = 10L, title = "Crêpes", updatedAt = 1000L)
        val recipe2 = Recipe(id = 20L, title = "Gaufres", updatedAt = 1000L)

        val match1 = RecipeMatchResult(recipe = recipe1, matchPercentage = 100.0, totalInStock = 2, totalRequired = 2, missingIngredients = emptyList())
        val match1Same = RecipeMatchResult(recipe = recipe1, matchPercentage = 100.0, totalInStock = 2, totalRequired = 2, missingIngredients = emptyList())
        val match2 = RecipeMatchResult(recipe = recipe2, matchPercentage = 50.0, totalInStock = 1, totalRequired = 2, missingIngredients = listOf("Sucre"))

        assertThat(MatchRecipeAdapter.DiffCallback.areItemsTheSame(match1, match1Same)).isTrue()
        assertThat(MatchRecipeAdapter.DiffCallback.areItemsTheSame(match1, match2)).isFalse()
        assertThat(MatchRecipeAdapter.DiffCallback.areContentsTheSame(match1, match1Same)).isTrue()
        assertThat(MatchRecipeAdapter.DiffCallback.areContentsTheSame(match1, match2)).isFalse()
    }
}
