package com.example.parabdcollector.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import coil.load
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.CategoryMapper
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
                    binding.imagePreview.load(permanentUri)
                    binding.etImageUri.setText(permanentUri.toString())
                } else {
                    Toast.makeText(this, "Erreur lors de la sauvegarde de l\'image", Toast.LENGTH_SHORT).show()
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

        setupCategorySpinners()

        currentItemId = intent.getLongExtra("itemId", 0)

        if (currentItemId != 0L) {
            viewModel.getById(currentItemId).observe(this) { item ->
                item?.let { populateUi(it) }
            }
        } else {
            supportActionBar?.title = "Nouvel Objet"
            binding.switchPossessed.isChecked = true
        }

        binding.btnSave.setOnClickListener { saveItem() }
        binding.btnTakePicture.setOnClickListener { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }

        binding.imagePreview.setOnClickListener { 
            val imageUri = binding.etImageUri.text.toString()
            if (imageUri.isNotBlank()) {
                val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                    putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, imageUri)
                    putExtra(FullScreenImageActivity.EXTRA_TITLE, binding.etTitle.text.toString())
                    putExtra(FullScreenImageActivity.EXTRA_EDITOR, binding.etEditeur.text.toString())
                    putExtra(FullScreenImageActivity.EXTRA_YEAR, binding.etAnnee.text.toString().toIntOrNull() ?: 0)
                    putExtra(FullScreenImageActivity.EXTRA_CATEGORY, binding.actvCategory.text.toString())
                    putExtra(FullScreenImageActivity.EXTRA_MATERIAL, binding.etMateriau.text.toString())
                    putExtra(FullScreenImageActivity.EXTRA_RUN, binding.etTirage.text.toString())
                    putExtra(FullScreenImageActivity.EXTRA_DIMENSIONS, binding.etDimensions.text.toString())
                }
                startActivity(intent)
            }
        }
    }

    private fun setupCategorySpinners() {
        // Remplir le spinner des super-catégories
        val superCategories = CategoryMapper.getSuperCategories()
        val superCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, superCategories)
        binding.actvSuperCategory.setAdapter(superCategoryAdapter)

        // Le spinner des catégories est désactivé au début
        binding.categoryLayout.isEnabled = false

        // Écouteur pour le spinner des super-catégories
        binding.actvSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedSuperCategory = parent.getItemAtPosition(position) as String
            val categories = CategoryMapper.getCategoriesFor(selectedSuperCategory)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.actvCategory.setAdapter(categoryAdapter)

            // Activer le deuxième spinner et vider son contenu
            binding.categoryLayout.isEnabled = true
            binding.actvCategory.text = null
        }
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
        binding.etEditeur.setText(item.editeur)
        binding.etAnnee.setText(item.annee?.toString())
        binding.etMois.setText(item.mois?.toString())
        
        binding.actvSuperCategory.setText(item.superCategorie, false)
        if (!item.superCategorie.isNullOrBlank()) {
            val categories = CategoryMapper.getCategoriesFor(item.superCategorie)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.actvCategory.setAdapter(categoryAdapter)
            binding.categoryLayout.isEnabled = true
        }
        binding.actvCategory.setText(item.categorie, false)

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
                binding.imagePreview.load(it) {
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                }
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

        val superCategory = binding.actvSuperCategory.text.toString().takeIf { it.isNotBlank() }
        val category = binding.actvCategory.text.toString().takeIf { it.isNotBlank() }

        val item = CollectionItem(
            remoteId = null, // L'ID distant sera géré par l'import
            id = currentItemId,
            titre = title,
            isPossessed = binding.switchPossessed.isChecked,
            editeur = binding.etEditeur.text.toString().takeIf { it.isNotBlank() },
            annee = binding.etAnnee.text.toString().toIntOrNull(),
            mois = binding.etMois.text.toString().toIntOrNull(),
            categorie = category,
            superCategorie = superCategory,
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