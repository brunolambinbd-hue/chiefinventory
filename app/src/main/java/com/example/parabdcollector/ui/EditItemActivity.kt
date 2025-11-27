package com.example.parabdcollector.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import coil.load
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityEditItemBinding
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.CategoryMapper
import kotlinx.coroutines.launch
import java.io.File

class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private var currentItem: CollectionItem? = null
    private var photoUri: Uri? = null
    private val isNewItem: Boolean by lazy { intent.getLongExtra("itemId", -1L) == -1L }

    private val viewModel: EditItemViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    private var displayLocations: List<DisplayLocation> = emptyList()
    private var selectedLocationId: Long? = null

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                binding.itemImage.visibility = View.VISIBLE
                binding.itemImage.load(croppedUri)
                viewModel.setImageUri(croppedUri)
            }
        } else {
            val exception = result.error
            Toast.makeText(this, "Erreur de recadrage: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                val cropOptions = CropImageOptions().apply {
                    allowRotation = true
                    allowFlipping = true
                }
                val cropContractOptions = CropImageContractOptions(uri, cropOptions)
                cropImageLauncher.launch(cropContractOptions)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        binding.btnTakePicture.setOnClickListener { onTakePictureClicked() }
        binding.btnSave.setOnClickListener { saveItem() }

        binding.itemImage.setOnClickListener {
            val imageUriString = viewModel.imageUri.value?.toString()
            if (imageUriString != null) {
                val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                    putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, imageUriString)
                    // ... (le reste des extras)
                }
                startActivity(intent)
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

    private fun onTakePictureClicked() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val imageFile = File(filesDir, "images/item_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)
        photoUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    private fun updateUI(item: CollectionItem) {
        supportActionBar?.title = getString(R.string.edit_item_title_editing, item.titre)
        binding.etTitle.setText(item.titre)
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
            val imageDrawable = binding.itemImage.drawable
            if (imageEmbedding == null && imageDrawable is BitmapDrawable) {
                imageEmbedding = viewModel.calculateSignature(imageDrawable.bitmap)
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
