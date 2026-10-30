package com.chefinventory.app.ui.actvity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ActivityEditIngredientBinding
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.StockPolicy
import com.chefinventory.app.ui.viewmodel.EditIngredientViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import com.chefinventory.app.utils.BitmapUtils
import com.chefinventory.app.utils.ImageCaptureUtil
import com.chefinventory.app.utils.IngredientParser
import com.chefinventory.app.utils.TextRecognitionHelper
import kotlinx.coroutines.launch

class EditIngredientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditIngredientBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentIngredient: Ingredient? = null
    private var newBitmap: Bitmap? = null
    private var locations: List<Location> = emptyList()
    private var selectedLocationId: Long? = null
    private var extractedOcrText: String? = null
    private var selectedStockPolicy: String = StockPolicy.PERMANENT

    private val viewModel: EditIngredientViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ingredientId = intent.getLongExtra(EXTRA_INGREDIENT_ID, -1L)
        selectedLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L).takeIf { it != -1L }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (ingredientId == -1L) getString(R.string.edit_ingredient_title_new) else getString(R.string.edit_ingredient_title_edit)

        textRecognitionHelper = TextRecognitionHelper(this)
        setupImageCapture()
        setupLocationDropdown()
        setupStockPolicyDropdown()

        if (ingredientId != -1L) {
            binding.btnDelete.isVisible = true
            viewModel.loadIngredient(ingredientId)
            viewModel.ingredient.observe(this) { ingredient ->
                if (ingredient != null) {
                    currentIngredient = ingredient
                    selectedLocationId = ingredient.locationId
                    updateUI(ingredient)
                }
            }
        } else {
            val extraName = intent.getStringExtra(EXTRA_NAME)
            if (!extraName.isNullOrBlank()) {
                val extraQty = intent.getDoubleExtra(EXTRA_QTY, 1.0)
                val extraUnit = intent.getStringExtra(EXTRA_UNIT) ?: ""
                val extraThreshold = intent.getDoubleExtra(EXTRA_THRESHOLD, extraQty / 2.0)
                val extraPackSize = intent.getDoubleExtra(EXTRA_PACK_SIZE, 1.0)

                binding.etName.setText(extraName)
                binding.etQuantity.setText(if (extraQty % 1.0 == 0.0) extraQty.toInt().toString() else extraQty.toString())
                binding.etUnit.setText(extraUnit)
                binding.etMinThreshold.setText(if (extraThreshold % 1.0 == 0.0) extraThreshold.toInt().toString() else extraThreshold.toString())
                binding.etPackSize.setText(if (extraPackSize % 1.0 == 0.0) extraPackSize.toInt().toString() else extraPackSize.toString())
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
        binding.btnSave.setOnClickListener { saveIngredient() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }

        binding.btnEditIncrement.setOnClickListener {
            val current = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val unit = binding.etUnit.text.toString()
            val step = calculateQuantityStep(unit)
            val nextVal = current + step
            binding.etQuantity.setText(formatQuantity(nextVal))
        }

        binding.btnEditDecrement.setOnClickListener {
            val current = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val unit = binding.etUnit.text.toString()
            val step = calculateQuantityStep(unit)
            val nextVal = (current - step).coerceAtLeast(0.0)
            binding.etQuantity.setText(formatQuantity(nextVal))
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun setupLocationDropdown() {
        viewModel.ingredientCategories.observe(this) { list ->
            locations = list
            // Construction des noms hiérarchiques (ex : Cuisine > Frigo)
            val displayNames = list.map { buildLocationPath(it, list) }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, displayNames)
            binding.actvIngredientLocation.setAdapter(adapter)
            
            selectedLocationId?.let { id ->
                list.find { it.id == id }?.let {
                    binding.actvIngredientLocation.setText(buildLocationPath(it, list), false)
                }
            }
        }

        binding.actvIngredientLocation.setOnItemClickListener { _, _, position, _ ->
            selectedLocationId = locations[position].id
        }
    }

    /**
     * Construit récursivement le chemin complet d'une localisation.
     */
    private fun buildLocationPath(location: Location, allLocations: List<Location>): String {
        val path = mutableListOf<String>()
        var current: Location? = location
        while (current != null) {
            path.add(0, current.name)
            current = allLocations.find { it.id == current.parentId }
        }
        return path.joinToString(" > ")
    }

    private fun setupImageCapture() {
        imageCaptureUtil = ImageCaptureUtil(this) { uri ->
            if (uri != null) {
                binding.ivIngredient.isVisible = true
                binding.ivIngredient.load(uri)
                viewModel.setImageUri(uri)
                newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
                newBitmap?.let { runOcr(it) }
            }
        }
    }

    private fun runOcr(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                extractedOcrText = textRecognitionHelper.recognizeText(bitmap)
                if (!extractedOcrText.isNullOrBlank()) {
                    val lines = extractedOcrText!!.lines()
                        .map { it.trim() }
                        .filter { it.length > 2 && !it.contains(Regex("[0-9]{8,13}")) }

                    val ingredientLine = lines.firstOrNull { it.matches(Regex(".*\\d.*[a-zA-Z].*")) } ?: lines.firstOrNull()

                    if (ingredientLine != null) {
                        val parsedIngredient = IngredientParser.parse(ingredientLine)
                        if (binding.etName.text.isNullOrBlank()) {
                            binding.etName.setText(parsedIngredient.name)
                        }
                        if (binding.etQuantity.text.isNullOrBlank()) {
                            binding.etQuantity.setText(parsedIngredient.quantity?.toString() ?: "")
                        }
                        if (binding.etUnit.text.isNullOrBlank()) {
                            binding.etUnit.setText(parsedIngredient.unit ?: "")
                        }
                        if (binding.etSupplementalInfo.text.isNullOrBlank()) {
                            binding.etSupplementalInfo.setText(parsedIngredient.supplementalInfo ?: "")
                        }
                        Toast.makeText(this@EditIngredientActivity, getString(R.string.toast_ingredient_detected), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditIngredient", "OCR failed", e)
            }
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun setupStockPolicyDropdown() {
        val stockPolicyOptions = listOf(
            getString(R.string.stock_policy_permanent_label),
            getString(R.string.stock_policy_on_demand_label)
        )
        val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stockPolicyOptions)
        binding.actvStockPolicy.setAdapter(adapterDropdown)
        binding.actvStockPolicy.setOnItemClickListener { _, _, position, _ ->
            selectedStockPolicy = if (position == 0) StockPolicy.PERMANENT else StockPolicy.ON_DEMAND
        }
    }

    private fun updateUI(ingredient: Ingredient) {
        binding.etName.setText(ingredient.name)
        binding.etQuantity.setText(ingredient.quantity?.toString() ?: "")
        binding.etUnit.setText(ingredient.unit ?: "")
        binding.etSupplementalInfo.setText(ingredient.supplementalInfo ?: "")
        binding.etDescription.setText(ingredient.description ?: "")
        val thresholdVal = ingredient.minThreshold ?: (ingredient.quantity ?: 0.0)
        binding.etMinThreshold.setText(if (thresholdVal > 0.0) thresholdVal.toString() else "")
        val packVal = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0
        binding.etPackSize.setText(if (packVal % 1.0 == 0.0) packVal.toInt().toString() else packVal.toString())
        selectedStockPolicy = ingredient.stockPolicy.uppercase()
        val policyLabel = if (selectedStockPolicy == StockPolicy.ON_DEMAND) getString(R.string.stock_policy_on_demand_label) else getString(R.string.stock_policy_permanent_label)
        binding.actvStockPolicy.setText(policyLabel, false)

        ingredient.imageUri?.let {
            binding.ivIngredient.isVisible = true
            binding.ivIngredient.load(it.toUri())
        }
    }

    private fun saveIngredient() {
        val name = binding.etName.text.toString()
        if (name.isBlank()) {
            Toast.makeText(this, getString(R.string.toast_ingredient_name_required), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val embedding = currentIngredient?.imageEmbedding
            val qtyVal = binding.etQuantity.text.toString().toDoubleOrNull()
            val thresholdVal = binding.etMinThreshold.text.toString().toDoubleOrNull() ?: (qtyVal ?: 0.0)
            val packVal = binding.etPackSize.text.toString().toDoubleOrNull() ?: 1.0

            val ingredient = (currentIngredient ?: Ingredient(name = "")).copy(
                name = name,
                quantity = qtyVal,
                unit = binding.etUnit.text.toString(),
                stockPolicy = selectedStockPolicy,
                supplementalInfo = binding.etSupplementalInfo.text.toString(),
                description = binding.etDescription.text.toString(),
                minThreshold = thresholdVal,
                packSize = packVal,
                imageUri = viewModel.imageUri.value?.toString() ?: currentIngredient?.imageUri,
                imageEmbedding = embedding,
                ocrText = extractedOcrText ?: currentIngredient?.ocrText,
                locationId = selectedLocationId,
                updatedAt = System.currentTimeMillis()
            )

            if (ingredient.id == 0L) viewModel.insert(ingredient) else viewModel.update(ingredient)
            
            Toast.makeText(this@EditIngredientActivity, getString(R.string.toast_ingredient_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_ingredient_title)
            .setMessage(R.string.dialog_delete_ingredient_message)
            .setPositiveButton(R.string.supprimer) { _, _ ->
                currentIngredient?.let {
                    viewModel.delete(it)
                    finish()
                }
            }
            .setNegativeButton(R.string.annuler, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_LOCATION_ID: String = "location_id"
        const val EXTRA_INGREDIENT_ID: String = "ingredient_id"
        const val EXTRA_NAME: String = "extra_name"
        const val EXTRA_QTY: String = "extra_qty"
        const val EXTRA_UNIT: String = "extra_unit"
        const val EXTRA_THRESHOLD: String = "extra_threshold"
        const val EXTRA_PACK_SIZE: String = "extra_pack_size"

        fun calculateQuantityStep(unit: String): Double {
            val cleanUnit = unit.lowercase().trim()
            return when {
                cleanUnit.contains("g") || cleanUnit.contains("ml") || cleanUnit.contains("cl") -> 50.0
                else -> 1.0
            }
        }

        fun formatQuantity(value: Double): String {
            return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
        }
    }
}
