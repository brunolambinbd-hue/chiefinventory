package com.example.parabdcollector.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem

class EditItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditItemBinding

    // On initialise le ViewModel de la bonne manière, comme dans MainActivity.
    private val viewModel: MainViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            saveItem()
        }
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString()
        val universe = binding.etUniverse.text.toString()
        val fabricant = binding.etFabricant.text.toString()
        val annee = binding.etAnnee.text.toString()
        val categorie = binding.etCategorie.text.toString()
        val materiau = binding.etMateriau.text.toString()
        val tirage = binding.etTirage.text.toString()
        val dimensions = binding.etDimensions.text.toString()
        val prixAchat = binding.etPrixAchat.text.toString()
        val valeurEstimee = binding.etValeurEstimee.text.toString()
        val lieuAchat = binding.etLieuAchat.text.toString()
        val notes = binding.etNotes.text.toString()
        val imageUri = binding.etImageUri.text.toString()
        val localisation = binding.etLocalisation.text.toString()

        if (title.isNotBlank()) {
            val newItem = CollectionItem(
                titre = title,
                univers = universe.takeIf { it.isNotBlank() },
                fabricant = fabricant.takeIf { it.isNotBlank() },
                annee = annee.toIntOrNull(),
                categorie = categorie.takeIf { it.isNotBlank() },
                materiau = materiau.takeIf { it.isNotBlank() },
                tirage = tirage.takeIf { it.isNotBlank() },
                dimensions = dimensions.takeIf { it.isNotBlank() },
                prixAchat = prixAchat.toDoubleOrNull(),
                valeurEstimee = valeurEstimee.toDoubleOrNull(),
                lieuAchat = lieuAchat.takeIf { it.isNotBlank() },
                notes = notes.takeIf { it.isNotBlank() },
                imageUri = imageUri.takeIf { it.isNotBlank() },
                localisation = localisation.takeIf { it.isNotBlank() }
            )
            viewModel.insert(newItem)
            finish() // Close the activity and return to the main list
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