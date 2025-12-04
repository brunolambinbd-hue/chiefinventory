package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityLocationManagementBinding
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.ui.adapter.LocationAdapter
import com.example.parabdcollector.ui.model.DisplayLocation
import com.example.parabdcollector.ui.viewmodel.LocationViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import com.example.parabdcollector.utils.observeOnce

/**
 * An activity for managing the hierarchical structure of storage locations.
 *
 * This screen displays locations in a tree-like structure and allows users to add,
 * edit, move, and delete locations through various dialogs.
 */
class LocationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationManagementBinding
    private lateinit var locationAdapter: LocationAdapter

    private val viewModel: LocationViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    /**
     * Initializes the activity, toolbar, RecyclerView, and observers.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_locations_title)

        setupRecyclerView()

        viewModel.visibleLocations.observe(this) {
            locationAdapter.submitList(it)
        }

        binding.fabAddLocation.setOnClickListener {
            showAddLocationDialog(null) // No parent for a root location
        }
    }

    /**
     * Displays a context menu with options for a specific location (Edit, Add Sub-location, etc.).
     * @param location The location for which to show options.
     */
    private fun showLocationOptionsDialog(location: Location) {
        val options = arrayOf(
            getString(R.string.scan_inventory),
            getString(R.string.modifier_le_nom),
            getString(R.string.ajouter_un_sous_emplacement),
            getString(R.string.changer_de_parent),
            getString(R.string.supprimer)
        )

        AlertDialog.Builder(this)
            .setTitle(location.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> startInventoryScan(location)
                    1 -> showEditLocationDialog(location)
                    2 -> showAddLocationDialog(location)
                    3 -> showChangeParentDialog(location)
                    4 -> showDeleteConfirmationDialog(location)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler), null)
            .show()
    }

    /**
     * Starts the inventory scanner activity for the given location.
     * @param location The location to be inventoried.
     */
    private fun startInventoryScan(location: Location) {
        val intent = Intent(this, InventoryScannerActivity::class.java).apply {
            putExtra(InventoryScannerActivity.EXTRA_LOCATION_ID, location.id)
            putExtra(InventoryScannerActivity.EXTRA_LOCATION_NAME, location.name)
        }
        startActivity(intent)
    }

    /**
     * Shows a dialog that allows the user to move a location to a new parent.
     * @param locationToMove The location that is being moved.
     */
    private fun showChangeParentDialog(locationToMove: Location) {
        viewModel.displayLocations.observeOnce(this) { allLocations ->
            val locationMap = allLocations.associateBy { it.location.id }
            // A location cannot be moved into itself or one of its own descendants.
            val possibleParents = allLocations.filter { 
                it.location.id != locationToMove.id && !isDescendant(it.location, locationToMove, locationMap)
            }

            // Add the option to move to root at the top.
            val displayItems = mutableListOf(getString(R.string.la_racine))
            displayItems.addAll(possibleParents.map { "    ".repeat(it.depth) + it.location.name })

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)

            AlertDialog.Builder(this)
                .setTitle("Déplacer \"${locationToMove.name}\" vers...")
                .setAdapter(adapter) { dialog, which ->
                    val newParentId = if (which == 0) {
                        null // The "Root" option
                    } else {
                        possibleParents[which - 1].location.id
                    }
                    val updatedLocation = locationToMove.copy(parentLocationId = newParentId)
                    viewModel.update(updatedLocation)
                    dialog.dismiss()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    /**
     * Recursively checks if a location is a descendant of another location.
     * @param potentialChild The location to check.
     * @param locationToMove The potential ancestor.
     * @param locationMap A map of all locations for efficient lookup.
     * @return True if [potentialChild] is a descendant of [locationToMove], false otherwise.
     */
    private fun isDescendant(potentialChild: Location, locationToMove: Location, locationMap: Map<Long, DisplayLocation>): Boolean {
        var current: Location? = potentialChild
        while (current?.parentLocationId != null) {
            if (current.parentLocationId == locationToMove.id) {
                return true
            }
            // Traverse up the tree to find the next parent.
            current = locationMap[current.parentLocationId]?.location
        }
        return false
    }

    /**
     * Shows a dialog for adding a new location.
     * @param parentLocation The parent for the new location, or null to create a root location.
     */
    private fun showAddLocationDialog(parentLocation: Location?) {
        val editText = EditText(this)
        val title = if (parentLocation == null) getString(R.string.nouvel_emplacement) else getString(
            R.string.nouveau_sous_emplacement_pour, parentLocation.name
        )

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Ajouter") { dialog, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    val newLocation = Location(name = name, parentLocationId = parentLocation?.id)
                    viewModel.insert(newLocation)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    /**
     * Shows a dialog for renaming an existing location.
     * @param location The location to edit.
     */
    private fun showEditLocationDialog(location: Location) {
        val editText = EditText(this)
        editText.setText(location.name)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.modifier_l_emplacement))
            .setView(editText)
            .setPositiveButton("Modifier") { dialog, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    val updatedLocation = location.copy(name = newName)
                    viewModel.update(updatedLocation)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    /**
     * Shows a confirmation dialog before deleting a location.
     * @param location The location to delete.
     */
    private fun showDeleteConfirmationDialog(location: Location) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.supprimer_l_emplacement))
            .setMessage("Êtes-vous sûr de vouloir supprimer \"${location.name}\"? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { dialog, _ ->
                viewModel.delete(location)
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    /**
     * Initializes the RecyclerView and its adapter, and defines click handling callbacks.
     */
    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter(
            onToggleExpand = { locationId ->
                viewModel.toggleExpansion(locationId)
            },
            onEdit = { locationId ->
                val location =
                    locationAdapter.currentList.find { it.location.id == locationId }?.location
                location?.let { showLocationOptionsDialog(it) }
            }
        )
        binding.rvLocations.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(this@LocationManagementActivity)
        }
    }

    /**
     * Handles the back arrow click in the toolbar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
