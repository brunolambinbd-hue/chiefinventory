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
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ActivityEditRecipeBinding
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.ui.viewmodel.EditRecipeViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.ImageCaptureUtil
import com.example.chiefinventory.utils.RecipeOcrParser
import com.example.chiefinventory.utils.TextRecognitionHelper
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentRecipe: Recipe? = null
    private var newBitmap: Bitmap? = null
    private var categories: List<Location> = emptyList()
    private var selectedLocationId: Long? = null

    private val viewModel: EditRecipeViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L)
        selectedLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L).takeIf { it != -1L }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (recipeId == -1L) "Nouvelle Recette" else "Modifier Recette"

        textRecognitionHelper = TextRecognitionHelper(this)
        setupImageCapture()
        setupCategoryDropdown()

        if (recipeId != -1L) {
            binding.btnDelete.isVisible = true
            viewModel.loadRecipe(recipeId)
            viewModel.recipe.observe(this) { recipe ->
                recipe?.let {
                    currentRecipe = it
                    selectedLocationId = it.locationId
                    updateUI(it)
                }
            }
            viewModel.getIngredientsForRecipe(recipeId).observe(this) { ingredients ->
                if (ingredients != null && binding.etIngredients.text.isNullOrBlank()) {
                    val ingredientsText = ingredients.joinToString("\n") { it.ingredientName }
                    binding.etIngredients.setText(ingredientsText)
                }
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.startCamera() }
        binding.btnPickFile.setOnClickListener { imageCaptureUtil.startGallery() }
        binding.btnScanOcr.setOnClickListener { newBitmap?.let { runFullOcr(it) } }
        binding.btnSave.setOnClickListener { saveRecipe() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun setupCategoryDropdown() {
        viewModel.recipeCategories.observe(this) { list ->
            categories = list
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list.map { it.name })
            binding.actvRecipeCategory.setAdapter(adapter)
            
            selectedLocationId?.let { id ->
                list.find { it.id == id }?.let {
                    binding.actvRecipeCategory.setText(it.name, false)
                }
            }
        }

        binding.actvRecipeCategory.setOnItemClickListener { _, _, position, _ ->
            selectedLocationId = categories[position].id
        }
    }

    private fun setupImageCapture() {
        imageCaptureUtil = ImageCaptureUtil(this) { uri ->
            if (uri != null) {
                binding.ivRecipe.isVisible = true
                binding.ivRecipe.load(uri)
                viewModel.setImageUri(uri)
                binding.btnScanOcr.isVisible = true
                newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
            }
        }
    }

    private fun runFullOcr(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val fullText = textRecognitionHelper.recognizeText(bitmap) ?: ""
                val result = RecipeOcrParser.parse(fullText, resources)

                if (binding.etTitle.text.isNullOrBlank()) binding.etTitle.setText(result.title)
                if (binding.etIngredients.text.isNullOrBlank()) binding.etIngredients.setText(result.ingredients)
                if (binding.etInstructions.text.isNullOrBlank()) binding.etInstructions.setText(result.instructions)
                if (binding.etServings.text.isNullOrBlank()) binding.etServings.setText(result.servings)
                if (binding.etWine.text.isNullOrBlank()) binding.etWine.setText(result.wine)
                if (binding.etSource.text.isNullOrBlank()) binding.etSource.setText(result.source)

                Toast.makeText(this@EditRecipeActivity, "Analyse terminée", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("EditRecipe", "OCR failed", e)
            }
        }
    }

    private fun updateUI(recipe: Recipe) {
        binding.etTitle.setText(recipe.title)
        binding.etInstructions.setText(recipe.instructions ?: "")
        binding.etPrepTime.setText(recipe.preparationTimeMinutes?.toString() ?: "")
        binding.etCookTime.setText(recipe.cookingTimeMinutes?.toString() ?: "")
        binding.etServings.setText(recipe.servings?.toString() ?: "")
        binding.etWine.setText(recipe.wineRecommendation ?: "")
        binding.etSource.setText(recipe.source ?: "")
        recipe.imageUri?.let {
            binding.ivRecipe.isVisible = true
            binding.ivRecipe.load(Uri.parse(it))
        }
    }

    private fun saveRecipe() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val recipe = (currentRecipe ?: Recipe(title = "", locationId = selectedLocationId)).copy(
                title = title,
                instructions = binding.etInstructions.text.toString(),
                preparationTimeMinutes = binding.etPrepTime.text.toString().toIntOrNull(),
                cookingTimeMinutes = binding.etCookTime.text.toString().toIntOrNull(),
                servings = binding.etServings.text.toString().toIntOrNull(),
                wineRecommendation = binding.etWine.text.toString(),
                source = binding.etSource.text.toString(),
                imageUri = viewModel.imageUri.value?.toString() ?: currentRecipe?.imageUri,
                locationId = selectedLocationId,
                updatedAt = System.currentTimeMillis()
            )

            val ingredientLines = binding.etIngredients.text.toString().lines().filter { it.isNotBlank() }
            val ingredients = ingredientLines.map { RecipeIngredient(recipeId = 0, ingredientName = it) }

            if (recipe.id == 0L) viewModel.insert(recipe, ingredients) else viewModel.update(recipe, ingredients)
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer la recette")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette recette ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                currentRecipe?.let {
                    viewModel.delete(it)
                    Toast.makeText(this, "Recette supprimée", Toast.LENGTH_SHORT).show()
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
        const val EXTRA_RECIPE_ID = "recipe_id"
    }
}
