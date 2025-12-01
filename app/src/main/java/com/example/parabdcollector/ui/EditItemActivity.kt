package com.example.parabdcollector.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.BitmapUtils
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.ImageCaptureUtil
import kotlinx.coroutines.launch

class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private var currentItem: CollectionItem? = null
    private val isNewItem: Boolean by lazy { intent.getLongExtra("itemId", -1L) == -1L }
    private var newBitmap: Bitmap? = null

    private val viewModel: EditItemViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    private var displayLocations: List<DisplayLocation> = emptyList()
    private var selectedLocationId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageCaptureUtil = ImageCaptureUtil(this) { permanentUri ->
            if (permanentUri != null) {
                binding.itemImage.visibility = View.VISIBLE
                binding.itemImage.load(permanentUri)
                viewModel.setImageUri(permanentUri)
                newBitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
            }
        }

        setupCategorySpinners()
        setupLocationDropdown()

        val itemId = intent.getLongExtra("itemId", -1L)

        if (isNewItem) {
            supportActionBar?.title = getString(R.string.edit_item_title_new)
            binding.switchPossessed.isChecked = true
        } else {
            viewModel.loadItem(itemId)
            viewModel.item.observe(this) { item ->
                currentItem = item
                selectedLocationId = item.locationId
                updateUI(item)
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
        binding.btnSave.setOnClickListener { saveItem() }

        binding.itemImage.setOnClickListener {
            val imageUriString = viewModel.imageUri.value?.toString()
            currentItem?.let { item ->
                if (imageUriString != null) {
                    val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                        putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, imageUriString)
                        putExtra(FullScreenImageActivity.EXTRA_TITLE, item.titre)
                        putExtra(FullScreenImageActivity.EXTRA_EDITOR, item.editeur)
                        putExtra(FullScreenImageActivity.EXTRA_YEAR, item.annee)
                        putExtra(FullScreenImageActivity.EXTRA_MONTH, item.mois)
                        putExtra(FullScreenImageActivity.EXTRA_SUPER_CATEGORY, item.superCategorie)
                        putExtra(FullScreenImageActivity.EXTRA_CATEGORY, item.categorie)
                        putExtra(FullScreenImageActivity.EXTRA_MATERIAL, item.materiau)
                        putExtra(FullScreenImageActivity.EXTRA_RUN, item.tirage)
                        putExtra(FullScreenImageActivity.EXTRA_DIMENSIONS, item.dimensions)
                        putExtra(FullScreenImageActivity.EXTRA_DESCRIPTION, item.description)
                        putExtra(FullScreenImageActivity.EXTRA_IMAGE_SIGNATURE, item.imageEmbedding)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun getFullPathForLocation(locationId: Long?): String {
        if (locationId == null) return ""
        val locationMap = displayLocations.associateBy { it.location.id }
        val pathParts = mutableListOf<String>()
        var currentId = locationId
        while (currentId != null) {
            val currentLocation = locationMap[currentId]?.location
            if (currentLocation != null) {
                pathParts.add(0, currentLocation.name)
                currentId = currentLocation.parentLocationId
            } else {
                break
            }
        }
        return pathParts.joinToString(" > ")
    }

    private fun setupLocationDropdown() {
        viewModel.displayLocations.observe(this) { locations ->
            displayLocations = locations
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                locations.map { "    ".repeat(it.depth) + it.location.name }
            )
            binding.etLocation.setAdapter(adapter)
            updateLocationSelectionInUI()
        }

        binding.etLocation.setOnItemClickListener { _, _, position, _ ->
            selectedLocationId = displayLocations.getOrNull(position)?.location?.id
            updateLocationSelectionInUI()
        }
    }

    private fun setupCategorySpinners() {
        val superCategories = CategoryMapper.getSuperCategories()
        val superCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, superCategories)
        binding.etSuperCategory.setAdapter(superCategoryAdapter)

        binding.categoryLayout.isEnabled = false

        binding.etSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedSuperCategory = parent.getItemAtPosition(position) as String
            val categories = CategoryMapper.getCategoriesFor(selectedSuperCategory)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etCategory.setAdapter(categoryAdapter)

            binding.categoryLayout.isEnabled = true
            binding.etCategory.text = null
        }
    }

    private fun updateUI(item: CollectionItem) {
        supportActionBar?.title = getString(R.string.edit_item_title_editing, item.titre)
        binding.etTitle.setText(item.titre)

        if (item.remoteId != null) {
            binding.tvRemoteId.text = getString(R.string.report_item_id, item.remoteId)
            binding.tvRemoteId.visibility = View.VISIBLE
        } else {
            binding.tvRemoteId.visibility = View.GONE
        }

        binding.switchPossessed.isChecked = item.isPossessed
        binding.etSuperCategory.setText(item.superCategorie ?: "", false)
        binding.etCategory.setText(item.categorie ?: "", false)
        binding.etEditor.setText(item.editeur ?: "")
        binding.etYear.setText(item.annee?.toString() ?: "")
        binding.etMonth.setText(item.mois?.toString() ?: "")
        binding.etMaterial.setText(item.materiau ?: "")
        binding.etPrintRun.setText(item.tirage ?: "")
        binding.etDimensions.setText(item.dimensions ?: "")
        binding.etDescription.setText(item.description ?: "")
        binding.etPurchasePrice.setText(item.prixAchat?.toString() ?: "")
        binding.etPurchaseLocation.setText(item.lieuAchat ?: "")
        binding.etEstimatedValue.setText(item.valeurEstimee?.toString() ?: "")

        updateLocationSelectionInUI()

        item.imageUri?.let {
            binding.itemImage.visibility = View.VISIBLE
            binding.itemImage.load(it.toUri()) { placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher) }
            viewModel.setImageUri(it.toUri())
        }

        if (!item.superCategorie.isNullOrBlank()) {
            val categories = CategoryMapper.getCategoriesFor(item.superCategorie)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etCategory.setAdapter(categoryAdapter)
            binding.categoryLayout.isEnabled = true
        } else {
            binding.categoryLayout.isEnabled = false
        }
    }

    private fun updateLocationSelectionInUI() {
        if (displayLocations.isNotEmpty()) {
            val fullPath = getFullPathForLocation(selectedLocationId)
            binding.etLocation.setText(fullPath, false)
        }
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, R.string.toast_title_is_mandatory, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var imageEmbedding: ByteArray? = currentItem?.imageEmbedding
            
            if (imageEmbedding == null && newBitmap != null) {
                imageEmbedding = viewModel.calculateSignature(newBitmap!!)
            }

            val itemToSave = CollectionItem(
                id = currentItem?.id ?: 0,
                remoteId = currentItem?.remoteId,
                titre = title,
                isPossessed = binding.switchPossessed.isChecked,
                superCategorie = binding.etSuperCategory.text.toString(),
                categorie = binding.etCategory.text.toString(),
                editeur = binding.etEditor.text.toString(),
                annee = binding.etYear.text.toString().toIntOrNull(),
                mois = binding.etMonth.text.toString().toIntOrNull(),
                materiau = binding.etMaterial.text.toString(),
                tirage = binding.etPrintRun.text.toString(),
                dimensions = binding.etDimensions.text.toString(),
                prixAchat = binding.etPurchasePrice.text.toString().toDoubleOrNull(),
                valeurEstimee = binding.etEstimatedValue.text.toString().toDoubleOrNull(),
                lieuAchat = binding.etPurchaseLocation.text.toString(),
                description = binding.etDescription.text.toString(),
                locationId = selectedLocationId,
                imageUri = viewModel.imageUri.value?.toString(),
                imageEmbedding = imageEmbedding
            )

            if (isNewItem) {
                viewModel.insert(itemToSave)
            } else {
                viewModel.update(itemToSave)
            }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
