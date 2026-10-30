package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [IngredientSelectAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IngredientSelectAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `IngredientItem getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val item1 = IngredientSelectAdapter.IngredientItem(name = "Beurre", isSelected = true)
        val item1Same = IngredientSelectAdapter.IngredientItem(name = "Beurre", isSelected = true)

        // Getters
        assertThat(item1.name).isEqualTo("Beurre")
        assertThat(item1.isSelected).isTrue()

        // Destructuring / componentN
        val (name, sel) = item1
        assertThat(name).isEqualTo("Beurre")
        assertThat(sel).isTrue()

        // Equals
        assertThat(item1.equals(item1)).isTrue()
        assertThat(item1.equals(null)).isFalse()
        assertThat(item1.equals("NotAnIngredientItem")).isFalse()
        assertThat(item1).isEqualTo(item1Same)

        // Copy discrepancies
        assertThat(item1).isNotEqualTo(item1.copy(name = "Farine"))
        assertThat(item1).isNotEqualTo(item1.copy(isSelected = false))

        // HashCode & ToString
        assertThat(item1.hashCode()).isEqualTo(item1Same.hashCode())
        assertThat(item1.toString()).contains("Beurre")
    }

    @Test
    fun `IngredientSelectAdapter should bind item and handle root and checkbox clicks`() {
        val toggledNames = mutableListOf<String>()
        val adapter = IngredientSelectAdapter { toggledNames.add(it) }

        val item = IngredientSelectAdapter.IngredientItem(name = "Sucre", isSelected = false)
        adapter.submitList(listOf(item))

        val viewHolder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.binding.tvIngredientName.text.toString()).isEqualTo("Sucre")
        assertThat(viewHolder.binding.checkbox.isChecked).isFalse()

        // Root click
        viewHolder.binding.root.performClick()
        assertThat(toggledNames).containsExactly("Sucre")

        // Checkbox click
        viewHolder.binding.checkbox.performClick()
        assertThat(toggledNames).containsExactly("Sucre", "Sucre")
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should compare items correctly`() {
        val diffCallback = IngredientSelectAdapter.DiffCallback()
        val item1 = IngredientSelectAdapter.IngredientItem(name = "Sucre", isSelected = true)
        val item1Same = IngredientSelectAdapter.IngredientItem(name = "Sucre", isSelected = true)
        val item2 = IngredientSelectAdapter.IngredientItem(name = "Sel", isSelected = false)

        assertThat(diffCallback.areItemsTheSame(item1, item1Same)).isTrue()
        assertThat(diffCallback.areItemsTheSame(item1, item2)).isFalse()
        assertThat(diffCallback.areContentsTheSame(item1, item1Same)).isTrue()
        assertThat(diffCallback.areContentsTheSame(item1, item2)).isFalse()
    }
}
