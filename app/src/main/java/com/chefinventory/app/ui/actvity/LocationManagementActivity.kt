package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ActivityLocationManagementBinding
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.ui.adapter.LocationAdapter
import com.chefinventory.app.ui.model.DisplayLocation
import com.chefinventory.app.ui.viewmodel.LocationViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import com.chefinventory.app.utils.observeOnce

/**
 * An activity for managing the hierarchical structure of storage locations.
 * Supports different [LocationType]s (Ingredient, Recipe).
 */
class LocationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationManagementBinding
    private lateinit var locationAdapter: LocationAdapter
    private var currentType: LocationType = LocationType.INGREDIENT

    private val viewModel: LocationViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // On récupère le type d'emplacement passé par l'Intent
        val typeOrdinal = intent.getIntOf(EXTRA_LOCATION_TYPE, LocationType.INGREDIENT.ordinal)
        currentType = LocationType.values()[typeOrdinal]
        viewModel.setLocationType(currentType)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Mise à jour du titre en fonction du type
        supportActionBar?.title = when (currentType) {
            LocationType.INGREDIENT -> getString(R.string.ingredients_management_title)
            LocationType.RECIPE -> getString(R.string.recipes_management_title)
        }

        setupRecyclerView()

        binding.fabAddLocation.setOnClickListener { 
            showAddLocationDialog(null)
        }

        viewModel.visibleLocations.observe(this) {
            locationAdapter.submitList(it)
        }

        viewModel.displayLocations.observeOnce(this) { locations ->
            if (locations.isNotEmpty()) {
                viewModel.expandAll()
            }
        }
    }

    private fun showLocationOptionsDialog(location: Location) {
        val options = mutableListOf<String>()
        options.add(getString(R.string.modifier_le_nom))
        options.add(getString(R.string.ajouter_un_sous_emplacement))
        options.add(getString(R.string.changer_de_parent))
        options.add(getString(R.string.supprimer))

        AlertDialog.Builder(this)
            .setTitle(location.name)
            .setItems(options.toTypedArray()) { dialog, which ->
                when (which) {
                    0 -> showEditLocationDialog(location)
                    1 -> showAddLocationDialog(location)
                    2 -> showChangeParentDialog(location)
                    3 -> showDeleteConfirmationDialog(location)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler), null)
            .show()
    }

    private fun showChangeParentDialog(locationToMove: Location) {
        viewModel.displayLocations.observeOnce(this) { allLocations ->
            val locationMap = allLocations.associateBy { it.location.id }
            val possibleParents = allLocations.filter { 
                it.location.id != locationToMove.id && !isDescendant(it.location, locationToMove, locationMap)
            }

            val displayItems = mutableListOf(getString(R.string.la_racine))
            displayItems.addAll(possibleParents.map { "    ".repeat(it.depth) + it.location.name })

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.move_location_to_title, locationToMove.name))
                .setAdapter(adapter) { dialog, which ->
                    val newParentId = if (which == 0) null else possibleParents[which - 1].location.id
                    viewModel.updateLocationParent(locationToMove.id, newParentId)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.annuler), null)
                .show()
        }
    }

    private fun isDescendant(potentialChild: Location, locationToMove: Location, locationMap: Map<Long, DisplayLocation>): Boolean {
        var current: Location? = potentialChild
        while (current?.parentId != null) {
            if (current.parentId == locationToMove.id) return true
            current = locationMap[current.parentId]?.location
        }
        return false
    }

    private fun getFullPath(locationId: Long, allLocations: List<DisplayLocation>): String {
        val locationMap = allLocations.associateBy { it.location.id }
        val pathParts = mutableListOf<String>()
        var currentId: Long? = locationId
        while (currentId != null) {
            val currentLocation = locationMap[currentId]?.location
            pathParts.add(0, currentLocation?.name ?: "Unknown")
            currentId = currentLocation?.parentId
        }
        return pathParts.joinToString(" > ")
    }

    private fun showAddLocationDialog(parentLocation: Location?) {
        val editText = EditText(this)
        val title = if (parentLocation == null) getString(R.string.nouvel_emplacement) else getString(
            R.string.nouveau_sous_emplacement_pour, parentLocation.name
        )

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(getString(R.string.ajouter)) { dialog, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    viewModel.insert(name, parentLocation?.id)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler), null)
            .show()
    }

    private fun showEditLocationDialog(location: Location) {
        val editText = EditText(this)
        editText.setText(location.name)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.modifier_l_emplacement))
            .setView(editText)
            .setPositiveButton(getString(R.string.modifier)) { dialog, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    val updatedLocation = location.copy(name = newName)
                    viewModel.update(updatedLocation)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler), null)
            .show()
    }

    private fun showDeleteConfirmationDialog(location: Location) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.supprimer_l_emplacement))
            .setMessage(getString(R.string.delete_location_confirmation_message, location.name))
            .setPositiveButton(getString(R.string.supprimer)) { dialog, _ ->
                viewModel.delete(location)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler), null)
            .show()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter(
            onToggleExpand = { locationId -> viewModel.toggleExpansion(locationId) },
            onEdit = { locationId ->
                val location = locationAdapter.currentList.find { it.location.id == locationId }?.location
                location?.let { showLocationOptionsDialog(it) }
            },
            onItemCountClick = { locationId ->
                if (currentType == LocationType.INGREDIENT) {
                    viewModel.displayLocations.observeOnce(this) { allDisplayLocations ->
                        val fullPath = getFullPath(locationId, allDisplayLocations)
                        val intent = Intent(this, IngredientListActivity::class.java).apply {
                            putExtra(IngredientListActivity.EXTRA_LOCATION_ID, locationId)
                            putExtra(IngredientListActivity.EXTRA_LOCATION_NAME, fullPath)
                        }
                        startActivity(intent)
                    }
                }
            }
        )
        binding.rvLocations.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(this@LocationManagementActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun Intent.getIntOf(key: String, default: Int): Int = if (hasExtra(key)) getIntExtra(key, default) else default

    companion object {
        const val EXTRA_LOCATION_TYPE: String = "location_type"
    }
}
