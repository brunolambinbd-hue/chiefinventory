package com.example.parabdcollector.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem

class EditItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditItemBinding
    private var currentItemId: Long = 0

    private val viewModel: MainViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // On récupère l'ID de l'item. Si c'est 0, c'est un nouvel item.
        currentItemId = intent.getLongExtra("itemId", 0)

        if (currentItemId != 0L) {
            // On est en mode édition : on observe l'item.
            viewModel.getById(currentItemId).observe(this) { item ->
                item?.let { populateUi(it) }
            }
        } else {
            // On est en mode création.
            supportActionBar?.title = "Nouvel Objet"
        }

        binding.btnSave.setOnClickListener {
            saveItem()
        }
    }

    private fun populateUi(item: CollectionItem) {
        binding.etTitle.setText(item.titre)
        binding.etUniverse.setText(item.univers)
        binding.etFabricant.setText(item.fabricant)
        binding.etAnnee.setText(item.annee?.toString())
        binding.etCategorie.setText(item.categorie)
        binding.etMateriau.setText(item.materiau)
        binding.etTirage.setText(item.tirage)
        binding.etDimensions.setText(item.dimensions)
        binding.etPrixAchat.setText(item.prixAchat?.toString())
        binding.etValeurEstimee.setText(item.valeurEstimee?.toString())
        binding.etLieuAchat.setText(item.lieuAchat)
        binding.etNotes.setText(item.notes)
        binding.etImageUri.setText(item.imageUri)
        binding.etLocalisation.setText(item.localisation)

        supportActionBar?.title = "Édition: ${item.titre}"
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        val item = CollectionItem(
            id = currentItemId, // On utilise l'ID actuel.
            titre = title,
            univers = binding.etUniverse.text.toString().takeIf { it.isNotBlank() },
            fabricant = binding.etFabricant.text.toString().takeIf { it.isNotBlank() },
            annee = binding.etAnnee.text.toString().toIntOrNull(),
            categorie = binding.etCategorie.text.toString().takeIf { it.isNotBlank() },
            materiau = binding.etMateriau.text.toString().takeIf { it.isNotBlank() },
            tirage = binding.etTirage.text.toString().takeIf { it.isNotBlank() },
            dimensions = binding.etDimensions.text.toString().takeIf { it.isNotBlank() },
            prixAchat = binding.etPrixAchat.text.toString().toDoubleOrNull(),
            valeurEstimee = binding.etValeurEstimee.text.toString().toDoubleOrNull(),
            lieuAchat = binding.etLieuAchat.text.toString().takeIf { it.isNotBlank() },
            notes = binding.etNotes.text.toString().takeIf { it.isNotBlank() },
            imageUri = binding.etImageUri.text.toString().takeIf { it.isNotBlank() },
            localisation = binding.etLocalisation.text.toString().takeIf { it.isNotBlank() }
        )

        if (currentItemId == 0L) {
            viewModel.insert(item)
        } else {
            viewModel.update(item)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}