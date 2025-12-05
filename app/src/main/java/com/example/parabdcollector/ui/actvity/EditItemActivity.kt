package com.example.parabdcollector.ui.actvity

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
import com.example.parabdcollector.ui.model.DisplayLocation
import com.example.parabdcollector.ui.viewmodel.EditItemViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import com.example.parabdcollector.utils.BitmapUtils
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.ImageCaptureUtil
import kotlinx.coroutines.launch

/**
 * Activity for creating a new collection item or editing an existing one.
 *
 * This screen provides a form with numerous fields to input item details.
 * Its mode (create or edit) is determined by the presence of an "itemId" in the launch Intent.
 * It also supports pre-filling certain fields (location, image) when launched from the inventory scanner.
 */
class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private var currentItem: CollectionItem? = null
    private val isNewItem: Boolean by lazy { intent.getLongExtra(EXTRA_ITEM_ID, -1L) == -1L }
    private var newBitmap: Bitmap? = null

    private val viewModel: EditItemViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    private var displayLocations: List<DisplayLocation> = emptyList()
    private var selectedLocationId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupImageCapture()
        setupCategorySpinners()
        setupLocationDropdown()

        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)

        if (isNewItem) {
            supportActionBar?.title = getString(R.string.edit_item_title_new)
            binding.switchPossessed.isChecked = true
            // Handle pre-filling from inventory scan
            handlePrefill()
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
        binding.itemImage.setOnClickListener { navigateToFullScreenImage() }
    }

    /**
     * Checks for pre-fill extras from the inventory scanner and populates the form.
     */
    private fun handlePrefill() {
        val prefillLocationId = intent.getLongExtra(EXTRA_PREFILL_LOCATION_ID, -1L)
        if (prefillLocationId != -1L) {
            selectedLocationId = prefillLocationId
        }

        val prefillImageUriString = intent.getStringExtra(EXTRA_PREFILL_IMAGE_URI)
        if (prefillImageUriString != null) {
            val imageUri = prefillImageUriString.toUri()
            binding.itemImage.visibility = View.VISIBLE
            binding.itemImage.load(imageUri)
            viewModel.setImageUri(imageUri)
            newBitmap = BitmapUtils.getBitmapFromUri(this, imageUri)
        }
    }
    /**
     * Initializes the [ImageCaptureUtil] and defines its callback.
     */
    private fun setupImageCapture() {
        imageCaptureUtil = ImageCaptureUtil(this) { permanentUri ->
            if (permanentUri != null) {
                binding.itemImage.visibility = View.VISIBLE
                binding.itemImage.load(permanentUri)
                viewModel.setImageUri(permanentUri)
                newBitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l\'image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Navigates to the [FullScreenImageActivity] to show the current item's image.
     */
    private fun navigateToFullScreenImage() {
        val imageUriString = viewModel.imageUri.value?.toString() ?: currentItem?.imageUri
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

    /**
     * Constructs the full hierarchical path for a given location ID (e.g., "Office > Shelf > Box").
     * @param locationId The ID of the leaf location.
     * @return A string representing the full, human-readable path.
     */
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

    /**
     * Sets up the dropdown menu (AutoCompleteTextView) for selecting a location.
     * It observes the locations from the ViewModel and populates the adapter.
     */
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

    /**
     * Sets up the dependent dropdown menus for super-category and category.
     * Selecting a super-category enables and populates the category dropdown.
     */
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

    /**
     * Populates all form fields with data from the provided [CollectionItem].
     * This is called when editing an existing item.
     * @param item The item whose data should be displayed in the form.
     */
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
            val categories = CategoryMapper.getCategoriesFor(item.superCategorie!!)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etCategory.setAdapter(categoryAdapter)
            binding.categoryLayout.isEnabled = true
        } else {
            binding.categoryLayout.isEnabled = false
        }
    }

    /**
     * Sets the text of the location dropdown to the full hierarchical path of the currently selected location.
     */
    private fun updateLocationSelectionInUI() {
        if (displayLocations.isNotEmpty()) {
            val fullPath = getFullPathForLocation(selectedLocationId)
            binding.etLocation.setText(fullPath, false)
        }
    }

    /**
     * Gathers all data from the form fields, validates it, creates a [CollectionItem] object,
     * and saves it via the ViewModel. It handles both insert and update operations.
     * It also calculates a new image signature if a new image has been provided.
     */
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

    /**
     * Handles action bar item clicks. Specifically handles the "Up" button.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "itemId"
        const val EXTRA_PREFILL_LOCATION_ID = "prefill_location_id"
        const val EXTRA_PREFILL_IMAGE_URI = "prefill_image_uri"
    }
}
