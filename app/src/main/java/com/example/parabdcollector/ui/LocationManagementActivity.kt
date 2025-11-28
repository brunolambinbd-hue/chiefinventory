package com.example.parabdcollector.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityLocationManagementBinding
import com.example.parabdcollector.model.Location

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

        viewModel.displayLocations.observe(this) {
            locationAdapter.submitList(it)
        }

        binding.fabAddLocation.setOnClickListener {
            showAddLocationDialog(null) // Pas de parent pour un emplacement racine
        }
    }

    private fun showLocationOptionsDialog(location: Location) {
        val options = arrayOf("Modifier le nom", "Ajouter un sous-emplacement", "Changer le parent", "Supprimer")

        AlertDialog.Builder(this)
            .setTitle(location.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditLocationDialog(location)
                    1 -> showAddLocationDialog(location) // On passe l'emplacement actuel comme parent
                    2 -> showChangeParentDialog(location)
                    3 -> showDeleteConfirmationDialog(location)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showChangeParentDialog(locationToMove: Location) {
        val allDisplayLocations = viewModel.displayLocations.value ?: return
        val allLocations = allDisplayLocations.map { it.location }

        val descendants = mutableSetOf<Long>()
        fun findDescendants(parentId: Long) {
            descendants.add(parentId)
            allLocations.filter { it.parentLocationId == parentId }.forEach { child ->
                if (child.id !in descendants) findDescendants(child.id)
            }
        }
        findDescendants(locationToMove.id)

        val validParents = allLocations.filter { it.id !in descendants }

        val rootOption = "Aucun parent (Racine)"
        val parentNames = mutableListOf(rootOption)
        parentNames.addAll(validParents.map { it.name })

        AlertDialog.Builder(this)
            .setTitle("Changer le parent de \"${locationToMove.name}\"")
            .setItems(parentNames.toTypedArray()) { dialog, which ->
                val updatedLocation = when (which) {
                    0 -> locationToMove.copy(parentLocationId = null)
                    else -> {
                        val selectedParent = validParents[which - 1]
                        locationToMove.copy(parentLocationId = selectedParent.id)
                    }
                }
                viewModel.update(updatedLocation)
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showAddLocationDialog(parentLocation: Location?) {
        val editText = EditText(this)
        val title = if (parentLocation == null) "Nouvel Emplacement" else "Nouveau sous-emplacement pour \"${parentLocation.name}\""

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
            .setTitle("Modifier l'emplacement")
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
            .setTitle("Supprimer l'emplacement")
            .setMessage("Êtes-vous sûr de vouloir supprimer \"${location.name}\"? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { dialog, _ ->
                viewModel.delete(location)
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter { displayLocation ->
            showLocationOptionsDialog(displayLocation.location)
        }
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
