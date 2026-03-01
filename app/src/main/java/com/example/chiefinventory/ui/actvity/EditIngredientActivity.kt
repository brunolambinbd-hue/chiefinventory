package com.example.chiefinventory.ui.actvity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityEditIngredientBinding
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.ui.viewmodel.EditIngredientViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.ImageCaptureUtil
import com.example.chiefinventory.utils.TextRecognitionHelper
import kotlinx.coroutines.launch

class EditIngredientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditIngredientBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentIngredient: Ingredient? = null
    private var newBitmap: Bitmap? = null
    private var categories: List<Location> = emptyList()
    private var selectedLocationId: Long? = null
    private var extractedOcrText: String? = null

    private val viewModel: EditIngredientViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ingredientId = intent.getLongExtra(EXTRA_INGREDIENT_ID, -1L)
        selectedLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L).takeIf { it != -1L }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (ingredientId == -1L) "Nouvel ingrédient" else "Modifier ingrédient"

        textRecognitionHelper = TextRecognitionHelper(this)
        setupImageCapture()
        setupCategoryDropdown()

        if (ingredientId != -1L) {
            binding.btnDelete.isVisible = true
            viewModel.loadIngredient(ingredientId)
            viewModel.ingredient.observe(this) { ingredient ->
                currentIngredient = ingredient
                selectedLocationId = ingredient.locationId
                updateUI(ingredient)
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
        binding.btnSave.setOnClickListener { saveIngredient() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun setupCategoryDropdown() {
        viewModel.ingredientCategories.observe(this) { list ->
            categories = list
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list.map { it.name })
            binding.actvIngredientLocation.setAdapter(adapter)
            
            selectedLocationId?.let { id ->
                list.find { it.id == id }?.let {
                    binding.actvIngredientLocation.setText(it.name, false)
                }
            }
        }

        binding.actvIngredientLocation.setOnItemClickListener { _, _, position, _ ->
            selectedLocationId = categories[position].id
        }
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
                        .filter { it.length > 2 }
                        .filter { !it.contains(Regex("[0-9]{8,13}")) }

                    val suggestedName = lines.firstOrNull { line ->
                        !line.contains(Regex("\\d+\\s*(g|kg|ml|l|%)", RegexOption.IGNORE_CASE)) &&
                        !line.contains(Regex("\\d{2}[/. ]\\d{2}[/. ]\\d{2,4}"))
                    }

                    if (binding.etName.text.isNullOrBlank() && suggestedName != null) {
                        binding.etName.setText(suggestedName)
                        Toast.makeText(this@EditIngredientActivity, "Nom détecté : $suggestedName", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditIngredient", "OCR failed", e)
            }
        }
    }

    private fun updateUI(ingredient: Ingredient) {
        binding.etName.setText(ingredient.name)
        binding.etQuantity.setText(ingredient.quantity?.toString() ?: "")
        binding.etUnit.setText(ingredient.unit ?: "")
        binding.etDescription.setText(ingredient.description ?: "")
        ingredient.imageUri?.let {
            binding.ivIngredient.isVisible = true
            binding.ivIngredient.load(Uri.parse(it))
        }
    }

    private fun saveIngredient() {
        val name = binding.etName.text.toString()
        if (name.isBlank()) {
            Toast.makeText(this, "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val embedding = newBitmap?.let { viewModel.calculateSignature(it) } ?: currentIngredient?.imageEmbedding
            val ingredient = (currentIngredient ?: Ingredient(name = "")).copy(
                name = name,
                quantity = binding.etQuantity.text.toString().toDoubleOrNull(),
                unit = binding.etUnit.text.toString(),
                description = binding.etDescription.text.toString(),
                imageUri = viewModel.imageUri.value?.toString() ?: currentIngredient?.imageUri,
                imageEmbedding = embedding,
                ocrText = extractedOcrText ?: currentIngredient?.ocrText,
                locationId = selectedLocationId,
                updatedAt = System.currentTimeMillis()
            )

            if (ingredient.id == 0L) viewModel.insert(ingredient) else viewModel.update(ingredient)
            
            Toast.makeText(this@EditIngredientActivity, "Ingrédient sauvegardé", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'ingrédient")
            .setMessage("Êtes-vous sûr de vouloir supprimer cet ingrédient ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                currentIngredient?.let {
                    viewModel.delete(it)
                    Toast.makeText(this, "Ingrédient supprimé", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_LOCATION_ID = "location_id"
        const val EXTRA_INGREDIENT_ID = "ingredient_id"
    }
}
