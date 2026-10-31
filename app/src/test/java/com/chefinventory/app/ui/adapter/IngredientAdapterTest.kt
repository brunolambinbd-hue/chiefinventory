package com.chefinventory.app.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
import com.chefinventory.app.model.Ingredient
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests unitaires dédiés (1-à-1) pour [IngredientAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IngredientAdapterTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSetLocationNamesMap_UpdatesMap() {
        val adapter = IngredientAdapter(onClick = {})
        val mappedLocationName = context.resources.getStringArray(R.array.keywords_location_frigo).firstOrNull() ?: "Frigo"
        val ingr = Ingredient(id = 1L, name = "Lait", locationId = 100L)

        // Test 1: setLocationNamesMap when itemCount > 0
        adapter.submitList(listOf(ingr)) {
            adapter.setLocationNamesMap(mapOf(100L to mappedLocationName))
            val h0 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(h0, 0)
            assertThat(h0.binding.tvLocation.text.toString()).contains(mappedLocationName)
        }

        // Test 2: setLocationNamesMap when itemCount == 0
        val emptyAdapter = IngredientAdapter(onClick = {})
        emptyAdapter.setLocationNamesMap(mapOf(1L to mappedLocationName))
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testViewHolder_BindsStockStylesAndUnitsAndInfo() {
        val adapter = IngredientAdapter(onClick = {})

        val healthy = Ingredient(id = 1L, name = "Beurre", quantity = 250.0, minThreshold = 100.0, unit = "g", supplementalInfo = "Bio")
        val low = Ingredient(id = 2L, name = "Lait", quantity = 50.0, minThreshold = 100.0, unit = null, supplementalInfo = null)
        val outOfStock = Ingredient(id = 3L, name = "Sel", quantity = 0.0, minThreshold = 100.0, unit = "   ", supplementalInfo = "  ")

        adapter.submitList(listOf(healthy, low, outOfStock)) {
            // Healthy Stock with non-empty unit and supplementalInfo
            val h0 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(h0, 0)
            assertThat(h0.binding.tvQuantity.text.toString()).contains("g")
            assertThat(h0.binding.tvSupplementalInfo.text.toString()).contains("Bio")

            // Low Stock with null unit and null supplementalInfo
            val h1 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(h1, 1)
            assertThat(h1.binding.tvStockStatus.text.toString()).contains("100")

            // Out of Stock with blank unit and blank supplementalInfo
            val h2 = adapter.onCreateViewHolder(parentContainer, 0)
            adapter.onBindViewHolder(h2, 2)
            assertThat(h2.binding.tvStockStatus.text.toString()).contains("100")
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testViewHolder_BindsEditClickListeners() {
        var clickedIngr: Ingredient? = null
        var editedIngr: Ingredient? = null

        val ingr = Ingredient(id = 10L, name = "Pommes", quantity = 2.0)

        // Non-null edit listener
        val adapterEdit = IngredientAdapter(
            onClick = { clickedIngr = it },
            onEditClick = { editedIngr = it }
        )

        adapterEdit.submitList(listOf(ingr)) {
            val h0 = adapterEdit.onCreateViewHolder(parentContainer, 0)
            adapterEdit.onBindViewHolder(h0, 0)

            h0.binding.root.performClick()
            assertThat(clickedIngr?.id).isEqualTo(10L)

            org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(1000))

            h0.binding.btnEditIngredient.performClick()
            assertThat(editedIngr?.id).isEqualTo(10L)
        }

        // Null edit listener
        val adapterNullEdit = IngredientAdapter(onClick = {}, onEditClick = null)
        adapterNullEdit.submitList(listOf(ingr)) {
            val h1 = adapterNullEdit.onCreateViewHolder(parentContainer, 0)
            adapterNullEdit.onBindViewHolder(h1, 0)

            h1.binding.btnEditIngredient.performClick()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testResolveHomeLocationIcon_AllValidBranches() {
        val iconFrigo = context.getString(R.string.icon_location_frigo)
        val iconPlacard = context.getString(R.string.icon_location_placard)
        val iconFreezer = context.getString(R.string.icon_location_freezer)
        val iconCave = context.getString(R.string.icon_location_cave)

        val kwFrigo = context.resources.getStringArray(R.array.keywords_location_frigo).first()
        val kwPlacard = context.resources.getStringArray(R.array.keywords_location_placard).first()
        val kwSurgele = context.resources.getStringArray(R.array.keywords_location_surgele).first()
        val kwCave = context.resources.getStringArray(R.array.keywords_location_cave).first()

        assertThat(IngredientAdapter.resolveHomeLocationIcon(kwFrigo, context)).isEqualTo(iconFrigo)
        assertThat(IngredientAdapter.resolveHomeLocationIcon(kwPlacard, context)).isEqualTo(iconPlacard)
        assertThat(IngredientAdapter.resolveHomeLocationIcon(kwSurgele, context)).isEqualTo(iconFreezer)
        assertThat(IngredientAdapter.resolveHomeLocationIcon(kwCave, context)).isEqualTo(iconCave)
        assertThat(IngredientAdapter.resolveHomeLocationIcon(null, context)).isEqualTo(iconFrigo)
        assertThat(IngredientAdapter.resolveHomeLocationIcon("", context)).isEqualTo(iconFrigo)
        assertThat(IngredientAdapter.resolveHomeLocationIcon("   ", context)).isEqualTo(iconFrigo)
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testResolveHomeLocationIcon_UnknownLocation_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException::class.java) {
            IngredientAdapter.resolveHomeLocationIcon("CategorieInexistante123", context)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testResolveLocationName_AllValidBranches() {
        val locFrigo = context.getString(R.string.location_default_frigo)
        val locSurgele = context.getString(R.string.location_default_surgele)
        val locCave = context.getString(R.string.location_default_cave)
        val locPlacard = context.getString(R.string.location_default_placard)

        val kwFrigo = context.resources.getStringArray(R.array.keywords_location_frigo).first()
        val kwPlacard = context.resources.getStringArray(R.array.keywords_location_placard).first()
        val kwSurgele = context.resources.getStringArray(R.array.keywords_location_surgele).first()
        val kwCave = context.resources.getStringArray(R.array.keywords_location_cave).first()

        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = kwFrigo, context = context)).isEqualTo(locFrigo)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = kwSurgele, context = context)).isEqualTo(locSurgele)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = kwCave, context = context)).isEqualTo(locCave)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = kwPlacard, context = context)).isEqualTo(locPlacard)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = null, context = context)).isEqualTo(locPlacard)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = "", context = context)).isEqualTo(locPlacard)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), category = "   ", context = context)).isEqualTo(locPlacard)
        assertThat(IngredientAdapter.resolveLocationName(null, emptyMap(), context = context)).isEqualTo(locPlacard)

        val customLocName = "Local Inox"
        assertThat(IngredientAdapter.resolveLocationName(1L, mapOf(1L to customLocName), category = kwCave, context = context)).isEqualTo(customLocName)
        assertThat(IngredientAdapter.resolveLocationName(1L, mapOf(1L to "   "), category = kwFrigo, context = context)).isEqualTo(locFrigo)
        assertThat(IngredientAdapter.resolveLocationName(2L, mapOf(1L to customLocName), category = kwFrigo, context = context)).isEqualTo(locFrigo)
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testResolveLocationName_NullContext_ThrowsIllegalStateException() {
        val kwFrigo = context.resources.getStringArray(R.array.keywords_location_frigo).first()
        assertThrows(IllegalStateException::class.java) {
            IngredientAdapter.resolveLocationName(null, emptyMap(), category = kwFrigo, context = null)
        }
        assertThrows(IllegalStateException::class.java) {
            IngredientAdapter.resolveLocationName(null, emptyMap())
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testResolveLocationName_UnknownCategory_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException::class.java) {
            IngredientAdapter.resolveLocationName(null, emptyMap(), category = "CategorieInexistante123", context = context)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHelperFunctions_DoublePackCategorySupplementalInfo() {
        val defaultCategory = context.getString(R.string.category_default_epicerie)

        assertThat(IngredientAdapter.formatDoubleValue(2.0)).isEqualTo("2")
        assertThat(IngredientAdapter.formatDoubleValue(2.5)).isEqualTo("2.5")

        assertThat(IngredientAdapter.formatPackInfo(0.0, context)).isEmpty()
        assertThat(IngredientAdapter.formatPackInfo(1.0, context)).isEmpty()
        assertThat(IngredientAdapter.formatPackInfo(6.0, context)).contains("6")

        assertThat(IngredientAdapter.resolveCategoryName(null, context)).isEqualTo(defaultCategory)
        assertThat(IngredientAdapter.resolveCategoryName("   ", context)).isEqualTo(defaultCategory)
        assertThat(IngredientAdapter.resolveCategoryName("Crémerie", context)).isEqualTo("Crémerie")

        assertThat(IngredientAdapter.resolveSupplementalInfoText(null, context)).isNull()
        assertThat(IngredientAdapter.resolveSupplementalInfoText("   ", context)).isNull()
        assertThat(IngredientAdapter.resolveSupplementalInfoText("Bio", context)).contains("Bio")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDiffCallback_ComparesItemsCorrectly() {
        val ingr1 = Ingredient(id = 10L, name = "Lait", quantity = 0.0, updatedAt = 1000L)
        val ingr1Same = Ingredient(id = 10L, name = "Lait", quantity = 0.0, updatedAt = 1000L)
        val ingr2 = Ingredient(id = 20L, name = "Beurre", quantity = 100.0, updatedAt = 1000L)

        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areItemsTheSame(ingr1, ingr2)).isFalse()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr1Same)).isTrue()
        assertThat(IngredientAdapter.DiffCallback.areContentsTheSame(ingr1, ingr2)).isFalse()
    }
}
