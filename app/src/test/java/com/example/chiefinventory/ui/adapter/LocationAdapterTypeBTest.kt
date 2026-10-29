package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.ui.model.DisplayLocation
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fichier de test spécifique pour [LocationAdapter] et [LocationAdapter.DiffCallback]
 * assurant la couverture à 100% des lignes 68 et 71.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationAdapterTypeBTest {

    private lateinit var context: Context
    private lateinit var parentContainer: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.example.chiefinventory.R.style.Theme_ChefInventory)
        parentContainer = FrameLayout(context)
    }

    @Test
    fun `DiffCallback areItemsTheSame and areContentsTheSame should reach 100 percent coverage`() {
        val loc1 = Location(id = 1L, name = "Cuisine", parentId = null, type = LocationType.INGREDIENT)
        val loc1Same = Location(id = 1L, name = "Cuisine", parentId = null, type = LocationType.INGREDIENT)
        val loc2 = Location(id = 2L, name = "Frigo", parentId = 1L, type = LocationType.INGREDIENT)

        val d1 = DisplayLocation(location = loc1, depth = 0)
        val d1Same = DisplayLocation(location = loc1Same, depth = 0)
        val d2 = DisplayLocation(location = loc2, depth = 1)

        // Line 68: areItemsTheSame
        assertThat(LocationAdapter.DiffCallback.areItemsTheSame(d1, d1Same)).isTrue()
        assertThat(LocationAdapter.DiffCallback.areItemsTheSame(d1, d2)).isFalse()

        // Line 71: areContentsTheSame
        assertThat(LocationAdapter.DiffCallback.areContentsTheSame(d1, d1Same)).isTrue()
        assertThat(LocationAdapter.DiffCallback.areContentsTheSame(d1, d2)).isFalse()
    }

    @Test
    fun `LocationAdapter should bind root location and handle all click listeners`() {
        var toggledId: Long? = null
        var editedId: Long? = null
        var clickedCountId: Long? = null

        val adapter = LocationAdapter(
            onToggleExpand = { toggledId = it },
            onEdit = { editedId = it },
            onItemCountClick = { clickedCountId = it }
        )

        val loc = Location(id = 10L, name = "Garde-manger", parentId = null, type = LocationType.INGREDIENT)
        val displayLoc = DisplayLocation(location = loc, depth = 0)

        adapter.submitList(listOf(displayLoc))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.locationName.text.toString()).isEqualTo("Garde-manger")
        assertThat(holder.binding.childIndicator.visibility).isEqualTo(android.view.View.GONE)

        // Clicks
        holder.binding.expandIcon.performClick()
        assertThat(toggledId).isEqualTo(10L)

        holder.binding.locationName.performClick()
        assertThat(clickedCountId).isEqualTo(10L)

        holder.binding.locationItemCount.performClick()
        assertThat(clickedCountId).isEqualTo(10L)

        holder.binding.editIcon.performClick()
        assertThat(editedId).isEqualTo(10L)
    }

    @Test
    fun `LocationAdapter should bind child location with depth indentation and child indicator`() {
        val adapter = LocationAdapter(onToggleExpand = {}, onEdit = {}, onItemCountClick = {})

        val loc = Location(id = 15L, name = "Etagère 2", parentId = 10L, type = LocationType.INGREDIENT)
        val displayLoc = DisplayLocation(location = loc, depth = 2)

        adapter.submitList(listOf(displayLoc))

        val holder = adapter.onCreateViewHolder(parentContainer, 0)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.binding.childIndicator.visibility).isEqualTo(android.view.View.VISIBLE)
        assertThat(holder.binding.indentation.layoutParams.width).isEqualTo(100) // depth 2 * 50 = 100
    }
}
