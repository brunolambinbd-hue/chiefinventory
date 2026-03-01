package com.example.chiefinventory.ui.actvity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityEditIngredientBinding
import com.example.chiefinventory.model.Ingredient
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
    private var locationId: Long = -1L
    private var extractedOcrText: String? = null

    private val viewModel: EditIngredientViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        val ingredientId = intent.getLongExtra(EXTRA_INGREDIENT_ID, -1L)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (ingredientId == -1L) "Nouvel ingrédient" else "Modifier ingrédient"

        textRecognitionHelper = TextRecognitionHelper(this)
        setupImageCapture()

        if (ingredientId != -1L) {
            viewModel.loadIngredient(ingredientId)
            viewModel.ingredient.observe(this) { ingredient ->
                currentIngredient = ingredient
                updateUI(ingredient)
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
        binding.btnSave.setOnClickListener { saveIngredient() }
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
                Log.d("EditIngredient", "OCR Result: $extractedOcrText")
                // Si le champ nom est vide, on tente de le remplir avec l'OCR
                if (binding.etName.text.isNullOrBlank() && !extractedOcrText.isNullOrBlank()) {
                    // On prend la première ligne ou les premiers mots
                    val suggestedName = extractedOcrText?.lines()?.firstOrNull { it.isNotBlank() }
                    binding.etName.setText(suggestedName)
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
            val ingredient = (currentIngredient ?: Ingredient(name = "", locationId = locationId)).copy(
                name = name,
                quantity = binding.etQuantity.text.toString().toDoubleOrNull(),
                unit = binding.etUnit.text.toString(),
                description = binding.etDescription.text.toString(),
                imageUri = viewModel.imageUri.value?.toString() ?: currentIngredient?.imageUri,
                imageEmbedding = embedding,
                ocrText = extractedOcrText ?: currentIngredient?.ocrText,
                updatedAt = System.currentTimeMillis()
            )

            if (ingredient.id == 0L) viewModel.insert(ingredient) else viewModel.update(ingredient)
            finish()
        }
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
