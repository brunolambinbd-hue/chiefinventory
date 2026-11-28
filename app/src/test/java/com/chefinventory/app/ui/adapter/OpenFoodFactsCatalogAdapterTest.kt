package com.chefinventory.app.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.core.view.isVisible
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

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAdapter_CategoryTextAndSynonymsAndTitleCaseVariants() {
        val adapter = OpenFoodFactsCatalogAdapter(
            onItemClick = {},
            onAddToPantryClick = {}
        )

        // 1. Ecobalyse category present
        val itemEcobalyse = OpenFoodFactsTaxonomy(id = 10L, cleanName = "pommes", category = "Fruits", ecobalyseCategory = "fruits_eco")
        // 2. Category = "Alimentation" -> fallback "other_food_items"
        val itemAlimentation = OpenFoodFactsTaxonomy(id = 20L, cleanName = "Haricots", category = "Alimentation", ecobalyseCategory = null)
        // 3. Category blank -> fallback "other_food_items"
        val itemBlankCategory = OpenFoodFactsTaxonomy(id = 30L, cleanName = "poivre", category = "   ", ecobalyseCategory = "   ", synonyms = "   ")
        // 4. CleanName starting with digit (covers char.isLowerCase() == false branch)
        val itemDigitName = OpenFoodFactsTaxonomy(id = 40L, cleanName = "200 g de farine", category = "Légumes")
        // 5. Blank cleanName branch
        val itemBlankName = OpenFoodFactsTaxonomy(id = 50L, cleanName = "   ", category = "Légumes")

        adapter.submitList(listOf(itemEcobalyse, itemAlimentation, itemBlankCategory, itemDigitName, itemBlankName)) {
            val vh0 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh0, 0)
            assertThat(vh0.binding.tvCleanName.text.toString()).isEqualTo("Pommes")
            assertThat(vh0.binding.tvCategory.text.toString()).isEqualTo("fruits_eco")

            val vh1 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh1, 1)
            assertThat(vh1.binding.tvCleanName.text.toString()).isEqualTo("Haricots")
            assertThat(vh1.binding.tvCategory.text.toString()).isEqualTo("other_food_items")

            val vh2 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh2, 2)
            assertThat(vh2.binding.tvCategory.text.toString()).isEqualTo("other_food_items")
            assertThat(vh2.binding.tvSynonyms.isVisible).isFalse()

            val vh3 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh3, 3)
            assertThat(vh3.binding.tvCleanName.text.toString()).isEqualTo("200 g de farine")

            val vh4 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(vh4, 4)
            assertThat(vh4.binding.tvCleanName.text.toString()).isEmpty()
        }
    }
}
