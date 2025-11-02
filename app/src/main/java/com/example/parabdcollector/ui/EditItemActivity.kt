package com.example.parabdcollector.ui

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.ImageStorageHelper
import java.io.File

class EditItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditItemBinding
    private var currentItemId: Long = 0
    private var latestTmpUri: Uri? = null

    private val viewModel: MainViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    private val takeImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let {
                val permanentUri = ImageStorageHelper.saveImageToInternalStorage(this, it)
                if (permanentUri != null) {
                    binding.imagePreview.visibility = View.VISIBLE
                    binding.imagePreview.setImageURI(permanentUri)
                    binding.etImageUri.setText(permanentUri.toString())
                } else {
                    Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getTmpFileUri().let {
                latestTmpUri = it
                takeImageLauncher.launch(it)
            }
        } else {
            Toast.makeText(this, "Permission de la caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentItemId = intent.getLongExtra("itemId", 0)

        if (currentItemId != 0L) {
            viewModel.getById(currentItemId).observe(this) { item ->
                item?.let { populateUi(it) }
            }
        } else {
            supportActionBar?.title = "Nouvel Objet"
            // Par défaut, un nouvel objet est possédé
            binding.switchPossessed.isChecked = true
        }

        binding.btnSave.setOnClickListener { saveItem() }
        binding.btnTakePicture.setOnClickListener { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${applicationContext.packageName}.provider", tmpFile)
    }

    private fun populateUi(item: CollectionItem) {
        binding.etTitle.setText(item.titre)
        binding.switchPossessed.isChecked = item.isPossessed
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

        item.imageUri?.let {
            if (it.isNotBlank()) {
                binding.imagePreview.visibility = View.VISIBLE
                binding.imagePreview.setImageURI(Uri.parse(it))
            }
        }

        supportActionBar?.title = "Édition: ${item.titre}"
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        val item = CollectionItem(
            id = currentItemId,
            titre = title,
            isPossessed = binding.switchPossessed.isChecked,
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