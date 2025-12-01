package com.example.parabdcollector.ui

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
import com.example.parabdcollector.utils.observeOnce

class LocationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationManagementBinding
    private lateinit var locationAdapter: LocationAdapter

    private val viewModel: LocationViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

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
            showAddLocationDialog(null) // Pas de parent pour un emplacement racine
        }
    }

    private fun showLocationOptionsDialog(location: Location) {
        val options = arrayOf(getString(R.string.modifier_le_nom),
            getString(R.string.ajouter_un_sous_emplacement),
            getString(R.string.changer_de_parent), getString(R.string.supprimer))

        AlertDialog.Builder(this)
            .setTitle(location.name)
            .setItems(options) { dialog, which ->
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
            // On ne peut pas déplacer un emplacement dans lui-même ou dans l'un de ses propres enfants.
            val possibleParents = allLocations.filter { 
                it.location.id != locationToMove.id && !isDescendant(it.location, locationToMove, locationMap)
            }

            // On ajoute l'option pour déplacer à la racine en premier.
            val displayItems = mutableListOf(getString(R.string.la_racine))
            displayItems.addAll(possibleParents.map { "    ".repeat(it.depth) + it.location.name })

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)

            AlertDialog.Builder(this)
                .setTitle("Déplacer \"${locationToMove.name}\" vers...")
                .setAdapter(adapter) { dialog, which ->
                    val newParentId = if (which == 0) {
                        null // L'option "À la racine"
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

    private fun isDescendant(potentialChild: Location, locationToMove: Location, locationMap: Map<Long, DisplayLocation>): Boolean {
        var current: Location? = potentialChild
        while (current?.parentLocationId != null) {
            if (current.parentLocationId == locationToMove.id) {
                return true
            }
            // On remonte dans l'arbre pour trouver le parent suivant.
            current = locationMap[current.parentLocationId]?.location
        }
        return false
    }

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

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter(
            onToggleExpand = { locationId ->
                viewModel.toggleExpansion(locationId)
            },
            onEdit = { locationId ->
                val location = locationAdapter.currentList.find { it.location.id == locationId }?.location
                location?.let { showLocationOptionsDialog(it) }
            }
        )
        binding.rvLocations.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(this@LocationManagementActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
