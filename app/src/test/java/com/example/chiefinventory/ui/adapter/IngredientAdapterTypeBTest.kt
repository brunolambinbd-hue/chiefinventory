package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fichier de test spécifique pour [IngredientAdapter] et [IngredientAdapter.DiffCallback]
 * assurant la couverture à 100% des lignes 14, 33, 35, 43, 74, 78, et 87.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IngredientAdapterTypeBTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `IngredientAdapter default constructor parameter and DiffCallback should reach 100 percent coverage`() {
        // Line 14: Default constructor parameter (omitting locationNamesMap)
        val adapter = IngredientAdapter(onClick = {}, onEditClick = null)
        assertThat(adapter).isNotNull()

        val ingr1 = Ingredient(id = 1L, name = "Beurre", quantity = 100.0, updatedAt = 1000L)
        val ingr1Same = Ingredient(id = 1L, name = "Beurre", quantity = 100.0, updatedAt = 1000L)
        val ingr2 = Ingredient(id = 2L, name = "Lait", quantity = 1.0, updatedAt = 1000L)

        // DiffCallback tests
        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr2)).isFalse()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr2)).isFalse()
    }

    @Test
    fun `formatDoubleValue helper function should format integer and decimal double values atomically`() {
        assertThat(IngredientAdapter.formatDoubleValue(2.0)).isEqualTo("2")
        assertThat(IngredientAdapter.formatDoubleValue(2.5)).isEqualTo("2.5")
        assertThat(IngredientAdapter.formatDoubleValue(0.0)).isEqualTo("0")
        assertThat(IngredientAdapter.formatDoubleValue(10.75)).isEqualTo("10.75")
    }

    @Test
    fun `formatPackInfo helper function should format packSize values atomically`() {
        assertThat(IngredientAdapter.formatPackInfo(0.0)).isEmpty()
        assertThat(IngredientAdapter.formatPackInfo(1.0)).isEmpty()
        assertThat(IngredientAdapter.formatPackInfo(6.0)).isEqualTo(" • Pack: 6")
        assertThat(IngredientAdapter.formatPackInfo(1.5)).isEqualTo(" • Pack: 1.5")
    }

    @Test
    fun `resolveHomeLocationIcon helper function should resolve icons for all location keywords atomically`() {
        // Missing branches for locationName.isNullOrBlank()
        assertThat(IngredientAdapter.resolveHomeLocationIcon(null)).isEqualTo("❄️")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("")).isEqualTo("❄️")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("   ")).isEqualTo("❄️")

        // Non-blank location keywords
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Frigo Inox")).isEqualTo("❄️")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Placard Haut")).isEqualTo("🧺")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Surgélateur")).isEqualTo("🧊")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Congélateur")).isEqualTo("🧊")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Cave à Vin")).isEqualTo("🍷")
        assertThat(IngredientAdapter.resolveHomeLocationIcon("Garage")).isEqualTo("📍")
    }

    @Test
    fun `resolveLocationName resolveCategoryName and resolveSupplementalInfoText helper functions should be covered atomically`() {
        // resolveLocationName
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap())).isEqualTo("Frigo")
        assertThat(IngredientAdapter.resolveLocationName(1L, emptyMap())).isEqualTo("Frigo")
        assertThat(IngredientAdapter.resolveLocationName(1L, mapOf(1L to ""))).isEqualTo("Frigo")
        assertThat(IngredientAdapter.resolveLocationName(1L, mapOf(1L to "   "))).isEqualTo("Frigo")
        assertThat(IngredientAdapter.resolveLocationName(1L, mapOf(1L to "Placard"))).isEqualTo("Placard")

        // resolveCategoryName
        assertThat(IngredientAdapter.resolveCategoryName(null)).isEqualTo("Épicerie")
        assertThat(IngredientAdapter.resolveCategoryName("")).isEqualTo("Épicerie")
        assertThat(IngredientAdapter.resolveCategoryName("   ")).isEqualTo("Épicerie")
        assertThat(IngredientAdapter.resolveCategoryName("Crémerie")).isEqualTo("Crémerie")

        // resolveSupplementalInfoText
        assertThat(IngredientAdapter.resolveSupplementalInfoText(null)).isNull()
        assertThat(IngredientAdapter.resolveSupplementalInfoText("")).isNull()
        assertThat(IngredientAdapter.resolveSupplementalInfoText("   ")).isNull()
        assertThat(IngredientAdapter.resolveSupplementalInfoText("Bio")).isEqualTo("Info : Bio")
    }

    @Test
    fun `IngredientAdapter should cover line 33 unitStr branches for non-blank empty and null unit`() {
        val adapter = IngredientAdapter(onClick = {})

        val ingrUnitG = Ingredient(id = 1L, name = "Sucre", quantity = 100.0, unit = "g")
        val ingrUnitEmpty = Ingredient(id = 2L, name = "Sel", quantity = 10.0, unit = "")
        val ingrUnitNull = Ingredient(id = 3L, name = "Farine", quantity = 1.0, unit = null)

        adapter.submitList(listOf(ingrUnitG, ingrUnitEmpty, ingrUnitNull))

        val h0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h0, 0)
        assertThat(h0.binding.tvQuantity.text.toString()).isEqualTo("100 g")

        val h1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h1, 1)
        assertThat(h1.binding.tvQuantity.text.toString()).isEqualTo("10")

        val h2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h2, 2)
        assertThat(h2.binding.tvQuantity.text.toString()).isEqualTo("1")
    }

    @Test
    fun `IngredientAdapter should cover minThreshold and integer and decimal packSize branches`() {
        val adapter = IngredientAdapter(onClick = {})

        // Ingr1: decimal packSize = 1.5, minThreshold = 2.0
        val ingrDecimalPack = Ingredient(id = 1L, name = "Huile", quantity = 3.0, unit = "L", minThreshold = 2.0, packSize = 1.5)
        // Ingr2: integer packSize = 6.0, minThreshold = 1.0
        val ingrIntPack = Ingredient(id = 2L, name = "Lait", quantity = 0.0, unit = "L", minThreshold = 6.0, packSize = 6.0)
        // Ingr3: minThreshold = null (defaults to 0.0), packSize = 1.0
        val ingrNullThreshold = Ingredient(id = 3L, name = "Vinaigre", quantity = 0.0, unit = null, minThreshold = null, packSize = 1.0)

        adapter.submitList(listOf(ingrDecimalPack, ingrIntPack, ingrNullThreshold))

        val h0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h0, 0)
        assertThat(h0.binding.tvStockStatus.text.toString()).contains("Pack: 1.5")

        val h1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h1, 1)
        assertThat(h1.binding.tvStockStatus.text.toString()).contains("Pack: 6")

        val h2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h2, 2)
        assertThat(h2.binding.tvStockStatus.text.toString()).contains("0")
        assertThat(h2.binding.tvStockStatus.text.toString()).doesNotContain("Pack:")
    }

    @Test
    fun `IngredientAdapter should cover line 74 Surgelateur vs Congelateur branches and line 78 category null`() {
        val locationMap = mapOf(10L to "Surgélateur Bas", 20L to "Congélateur Haut", 30L to "Reserve")
        val adapter = IngredientAdapter(
            onClick = {},
            locationNamesMap = locationMap
        )

        val ingrSurgelateur = Ingredient(id = 1L, name = "Steak", locationId = 10L, category = "Viande")
        val ingrCongelateur = Ingredient(id = 2L, name = "Poisson", locationId = 20L, category = null)
        val ingrReserve = Ingredient(id = 3L, name = "Boite", locationId = 30L, category = "Conserve")

        adapter.submitList(listOf(ingrSurgelateur, ingrCongelateur, ingrReserve))

        val h0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h0, 0)
        assertThat(h0.binding.tvLocation.text.toString()).contains("🧊")

        val h1 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h1, 1)
        assertThat(h1.binding.tvLocation.text.toString()).contains("🧊")
        assertThat(h1.binding.tvLocation.text.toString()).contains(context.getString(com.example.chiefinventory.R.string.category_default_epicerie))

        val h2 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h2, 2)
        assertThat(h2.binding.tvLocation.text.toString()).contains("📍")
    }

    @Test
    fun `IngredientAdapter should cover line 87 onEditClick null vs non-null lambda`() {
        var clickedItem: Ingredient? = null
        var editedItem: Ingredient? = null

        val ingr = Ingredient(id = 1L, name = "Pâtes", quantity = 2.0)

        // Non-null edit listener
        val adapterWithEdit = IngredientAdapter(
            onClick = { clickedItem = it },
            onEditClick = { editedItem = it }
        )
        adapterWithEdit.submitList(listOf(ingr))

        val h0 = adapterWithEdit.onCreateViewHolder(parentContainer, 0)
        adapterWithEdit.onBindViewHolder(h0, 0)

        h0.binding.root.performClick()
        assertThat(clickedItem).isEqualTo(ingr)

        h0.binding.btnEditIngredient.performClick()
        assertThat(editedItem).isEqualTo(ingr)

        // Null edit listener (line 87 onEditClick == null)
        val adapterNullEdit = IngredientAdapter(onClick = {}, onEditClick = null)
        adapterNullEdit.submitList(listOf(ingr))

        val h1 = adapterNullEdit.onCreateViewHolder(parentContainer, 0)
        adapterNullEdit.onBindViewHolder(h1, 0)

        h1.binding.btnEditIngredient.performClick()
    }

    @Test
    fun `IngredientAdapter should cover supplementalInfo non-null blank string branch`() {
        val adapter = IngredientAdapter(onClick = {})

        val ingrBlankInfo = Ingredient(id = 1L, name = "Sel", supplementalInfo = "   ")
        adapter.submitList(listOf(ingrBlankInfo))

        val h0 = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(h0, 0)

        assertThat(h0.binding.tvSupplementalInfo.visibility).isEqualTo(android.view.View.GONE)
    }
}
