package com.chefinventory.app.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OpenFoodFactsCatalogAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OpenFoodFactsCatalogAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDiffCallback_AreItemsAndContentsTheSame() {
        val item1 = OpenFoodFactsTaxonomy(id = 1L, cleanName = "haricots verts", category = "Légumes")
        val item1Same = OpenFoodFactsTaxonomy(id = 1L, cleanName = "haricots verts", category = "Légumes")
        val item2 = OpenFoodFactsTaxonomy(id = 2L, cleanName = "pommes de terre", category = "Féculents")

        assertThat(OpenFoodFactsCatalogAdapter.DiffCallback.areItemsTheSame(item1, item1Same)).isTrue()
        assertThat(OpenFoodFactsCatalogAdapter.DiffCallback.areItemsTheSame(item1, item2)).isFalse()
        assertThat(OpenFoodFactsCatalogAdapter.DiffCallback.areContentsTheSame(item1, item1Same)).isTrue()
        assertThat(OpenFoodFactsCatalogAdapter.DiffCallback.areContentsTheSame(item1, item2)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAdapter_ViewHolderBindingAndClick() {
        var clickedItem: OpenFoodFactsTaxonomy? = null
        var addedItem: OpenFoodFactsTaxonomy? = null

        val adapter = OpenFoodFactsCatalogAdapter(
            onItemClick = { clickedItem = it },
            onAddToPantryClick = { addedItem = it }
        )

        val item1 = OpenFoodFactsTaxonomy(id = 1L, cleanName = "haricots verts", category = "Légumes", synonyms = "princesses")
        val item2 = OpenFoodFactsTaxonomy(id = 2L, cleanName = "sucre", category = null, synonyms = null)

        adapter.submitList(listOf(item1, item2)) {
            val vh0 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh0, 0)

            assertThat(vh0.binding.tvCleanName.text.toString()).isEqualTo("Haricots verts")
            assertThat(vh0.binding.tvCategory.text.toString()).isEqualTo("Légumes")
            assertThat(vh0.binding.tvSynonyms.text.toString()).isEqualTo("princesses")

            vh0.binding.root.performClick()
            assertThat(clickedItem?.cleanName).isEqualTo("haricots verts")

            vh0.binding.btnAddToPantry.performClick()
            assertThat(addedItem?.cleanName).isEqualTo("haricots verts")

            val vh1 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh1, 1)

            assertThat(vh1.binding.tvCleanName.text.toString()).isEqualTo("Sucre")
        }
    }
}
