package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ActivityEditItemBinding
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.ui.model.DisplayLocation
import com.example.chiefinventory.ui.viewmodel.EditItemViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.CategoryMapper
import com.example.chiefinventory.utils.ImageCaptureUtil
import com.example.chiefinventory.utils.TextRecognitionHelper
import kotlinx.coroutines.launch

/**
 * Activity for creating or editing a collection item.
 */
class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentItem: CollectionItem? = null
    private var newBitmap: Bitmap? = null
    private var displayLocations = emptyList<DisplayLocation>()
    private var selectedLocId: Long? = null
    private var extractedOcrText: String? = null

    private val isNewItem by lazy {
        intent.getLongExtra(EXTRA_ITEM_ID, -1L) == -1L
    }

    private val viewModel: EditItemViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textRecognitionHelper = TextRecognitionHelper(this)
        setupImageCapture()
        setupCategorySpinners()
        setupLocationDropdown()

        if (isNewItem) {
            supportActionBar?.title = getString(R.string.edit_item_title_new)
            binding.switchPossessed.isChecked = true
            handlePrefill()
        } else {
            val id = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
            viewModel.loadItem(id)
            viewModel.item.observe(this) { item ->
                currentItem = item
                selectedLocId = item.locationId
                extractedOcrText = item.ocrText
                updateUI(item)
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
        binding.btnSave.setOnClickListener { saveItem() }
        binding.itemImage.setOnClickListener { navigateToFullScreen() }
    }

    private fun handlePrefill() {
        val prefillLocId = intent.getLongExtra(EXTRA_PREFILL_LOCATION_ID, -1L)
        if (prefillLocId != -1L) {
            selectedLocId = prefillLocId
        }

        intent.getStringExtra(EXTRA_PREFILL_IMAGE_URI)?.let { uriString ->
            val uri = uriString.toUri()
            binding.itemImage.isVisible = true
            binding.itemImage.load(uri)
            viewModel.setImageUri(uri)
            newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
            newBitmap?.let { runOcrAndEmbedding(it) }
        }
    }

    private fun setupImageCapture() {
        imageCaptureUtil = ImageCaptureUtil(this) { uri ->
            if (uri != null) {
                binding.itemImage.isVisible = true
                binding.itemImage.load(uri)
                viewModel.setImageUri(uri)
                newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
                newBitmap?.let { runOcrAndEmbedding(it) }
            } else {
                Toast.makeText(this, "Erreur de capture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun runOcrAndEmbedding(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                extractedOcrText = textRecognitionHelper.recognizeText(bitmap)
                Log.d("EditItemActivity", "OCR Text: $extractedOcrText")
            } catch (e: Exception) {
                Log.e("EditItemActivity", "OCR failed", e)
            }
        }
    }

    private fun navigateToFullScreen() {
        val uri = viewModel.imageUri.value?.toString() ?: currentItem?.imageUri
        val intent = Intent(this, FullScreenImageActivity::class.java).apply {
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, uri)
            putExtra(FullScreenImageActivity.EXTRA_TITLE, binding.etTitle.text.toString())
            putExtra(FullScreenImageActivity.EXTRA_EDITOR, binding.etEditor.text.toString())
            putExtra(FullScreenImageActivity.EXTRA_YEAR, binding.etYear.text.toString().toIntOrNull())
            putExtra(FullScreenImageActivity.EXTRA_DESCRIPTION, binding.etDescription.text.toString())
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_SIGNATURE, currentItem?.imageEmbedding)
        }
        startActivity(intent)
    }

    private fun setupLocationDropdown() {
        viewModel.displayLocations.observe(this) { locations ->
            displayLocations = locations
            val names = mutableListOf(getString(R.string.none))
            names.addAll(locations.map { "    ".repeat(it.depth) + it.location.name })
            binding.etLocation.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, names))
            updateLocationUI()
        }

        binding.etLocation.setOnItemClickListener { _, _, position, _ ->
            selectedLocId = if (position == 0) null else displayLocations.getOrNull(position - 1)?.location?.id
            updateLocationUI()
        }
    }

    private fun setupCategorySpinners() {
        val superCats = CategoryMapper.getSuperCategories()
        binding.etSuperCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, superCats))
        binding.categoryLayout.isEnabled = false

        binding.etSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val sel = parent.getItemAtPosition(position) as String
            binding.etCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, CategoryMapper.getCategoriesFor(sel)))
            binding.categoryLayout.isEnabled = true
            binding.etCategory.setText("", false)
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
        updateLocationUI()
        item.imageUri?.let {
            binding.itemImage.isVisible = true
            binding.itemImage.load(it.toUri())
            viewModel.setImageUri(it.toUri())
        }
        binding.categoryLayout.isEnabled = !item.superCategorie.isNullOrBlank()
    }

    private fun updateLocationUI() {
        if (selectedLocId == null) {
            binding.etLocation.setText("", false)
            return
        }

        if (displayLocations.isNotEmpty()) {
            val locMap = displayLocations.associateBy { it.location.id }
            val path = mutableListOf<String>()
            var curr = selectedLocId
            while (curr != null) {
                val l = locMap[curr]?.location
                if (l != null) { path.add(0, l.name); curr = l.parentId } else break
            }
            binding.etLocation.setText(path.joinToString(" > "), false)
        }
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, R.string.toast_title_is_mandatory, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val embedding = newBitmap?.let { viewModel.calculateSignature(it) } ?: currentItem?.imageEmbedding
            val item = (currentItem ?: CollectionItem(titre = "")).copy(
                titre = title,
                editeur = binding.etEditor.text.toString(),
                annee = binding.etYear.text.toString().toIntOrNull(),
                mois = binding.etMonth.text.toString().toIntOrNull(),
                categorie = binding.etCategory.text.toString(),
                superCategorie = binding.etSuperCategory.text.toString(),
                materiau = binding.etMaterial.text.toString(),
                tirage = binding.etPrintRun.text.toString(),
                dimensions = binding.etDimensions.text.toString(),
                description = binding.etDescription.text.toString(),
                prixAchat = binding.etPurchasePrice.text.toString().toDoubleOrNull(),
                valeurEstimee = binding.etEstimatedValue.text.toString().toDoubleOrNull(),
                lieuAchat = binding.etPurchaseLocation.text.toString(),
                isPossessed = binding.switchPossessed.isChecked,
                imageUri = viewModel.imageUri.value?.toString() ?: currentItem?.imageUri,
                imageEmbedding = embedding,
                ocrText = extractedOcrText,
                locationId = selectedLocId
            )
            if (isNewItem) viewModel.insert(item) else viewModel.update(item)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_ITEM_ID: String = "itemId"
        const val EXTRA_PREFILL_LOCATION_ID: String = "prefillLocationId"
        const val EXTRA_PREFILL_IMAGE_URI: String = "prefillImageUri"
    }
}
