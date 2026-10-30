package com.chefinventory.app.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.dao.RecipeCategoryInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [RecipeCategoryAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeCategoryAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `RecipeCategoryAdapter should bind category info and trigger click listener`() {
        var clickedCategory: RecipeCategoryInfo? = null
        val adapter = RecipeCategoryAdapter { clickedCategory = it }

        val info = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 8)
        adapter.submitList(listOf(info))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvCategoryName.text.toString()).isEqualTo("Desserts")
        assertThat(viewHolder.binding.tvRecipeCount.text.toString()).isEqualTo("8")

        viewHolder.binding.root.performClick()
        assertThat(clickedCategory).isEqualTo(info)
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should compare items correctly`() {
        val diffCallback = RecipeCategoryAdapter.DiffCallback()
        val info1 = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 8)
        val info1Same = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 8)
        val info2 = RecipeCategoryInfo(categoryId = 20L, categoryName = "Entrées", recipeCount = 3)

        assertThat(diffCallback.areItemsTheSame(info1, info1Same)).isTrue()
        assertThat(diffCallback.areItemsTheSame(info1, info2)).isFalse()
        assertThat(diffCallback.areContentsTheSame(info1, info1Same)).isTrue()
        assertThat(diffCallback.areContentsTheSame(info1, info2)).isFalse()
    }
}
