package com.example.chiefinventory.ui.actvity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityEditRecipeBinding
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.ui.viewmodel.EditRecipeViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.ImageCaptureUtil
import com.example.chiefinventory.utils.IngredientParser
import com.example.chiefinventory.utils.RecipeOcrParser
import com.example.chiefinventory.utils.TextRecognitionHelper
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentRecipe: Recipe? = null
    private var newBitmap: Bitmap? = null
    private var lastImageUri: Uri? = null
    private var categories: List<Location> = emptyList()
    private var selectedLocationId: Long? = null

    private val viewModel: EditRecipeViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleNewImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L)
        selectedLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L).takeIf { it != -1L }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (recipeId == -1L) "Nouvelle recette" else "Modifier la recette"

        textRecognitionHelper = TextRecognitionHelper(this)
        imageCaptureUtil = ImageCaptureUtil(this) { uri ->
            if (uri != null) handleNewImage(uri)
        }
        setupCategoryDropdown()
        setupDifficultyDropdown()

        if (recipeId != -1L) {
            binding.btnDelete.isVisible = true
            viewModel.loadRecipe(recipeId)
            viewModel.recipe.observe(this) { recipe ->
                if (recipe != null) {
                    currentRecipe = recipe
                    selectedLocationId = recipe.locationId
                    updateUI(recipe)
                }
            }
        }

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.startCamera() }
        binding.btnPickFile.setOnClickListener { imageGallery() }
        binding.btnScanOcr.setOnClickListener { runOcr() }
        binding.btnSave.setOnClickListener { saveRecipe() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun imageGallery() {
        pickImage.launch("image/*")
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

    private fun setupDifficultyDropdown() {
        val difficulties = arrayOf("facile", "moyen", "difficile")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, difficulties)
        binding.actvDifficulty.setAdapter(adapter)
    }

    private fun handleNewImage(uri: Uri) {
        lastImageUri = uri
        binding.ivRecipe.isVisible = true
        binding.ivRecipe.load(uri)
        binding.btnScanOcr.isVisible = true
        newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
    }

    private fun runOcr() {
        val bitmap = newBitmap ?: return
        lifecycleScope.launch {
            try {
                val fullText: String = textRecognitionHelper.recognizeText(bitmap) ?: ""
                val result = RecipeOcrParser.parse(fullText, resources)
                
                binding.etIngredients.setText(result.ingredients ?: "")
                binding.etInstructions.setText(result.instructions ?: "")
                binding.etWine.setText(result.wine ?: "")
                binding.etSource.setText(result.source ?: "")
                binding.etServings.setText(result.servings ?: "")
                
                binding.etPrepTime.setText(result.prepTime ?: "")
                binding.etCookTime.setText(result.cookTime ?: "")
                binding.etRestingTime.setText(result.restingTime ?: "")
                binding.etKcal.setText(result.kcalPerServing ?: "")
                binding.actvDifficulty.setText(result.difficulty ?: "", false) // false pour ne pas filtrer la liste

                Toast.makeText(this@EditRecipeActivity, "Scan terminé", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("EditRecipe", "OCR failed", e)
                Toast.makeText(this@EditRecipeActivity, "Échec du scan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(recipe: Recipe) {
        binding.etTitle.setText(recipe.title)
        binding.etInstructions.setText(recipe.instructions ?: "")
        binding.etPrepTime.setText(recipe.preparationTimeMinutes?.toString() ?: "")
        binding.etCookTime.setText(recipe.cookingTimeMinutes?.toString() ?: "")
        binding.etRestingTime.setText(recipe.restingTimeMinutes?.toString() ?: "")
        binding.etServings.setText(recipe.servings?.toString() ?: "")
        binding.etWine.setText(recipe.wineRecommendation ?: "")
        binding.etSource.setText(recipe.source ?: "")
        binding.etKcal.setText(recipe.kcalPerServing?.toString() ?: "")
        binding.actvDifficulty.setText(recipe.difficulty ?: "", false)
        
        recipe.imageUri?.let {
            val uri = Uri.parse(it)
            lastImageUri = uri
            binding.ivRecipe.isVisible = true
            binding.ivRecipe.load(uri)
            binding.btnScanOcr.isVisible = true
            lifecycleScope.launch {
                newBitmap = BitmapUtils.getBitmapFromUri(this@EditRecipeActivity, uri)
            }
        }
        
        viewModel.getIngredientsForRecipe(recipe.id).observe(this) { ingredients ->
            val text = ingredients.joinToString("\n") { 
                val qty = it.quantityRequired?.let { q -> if (q % 1.0 == 0.0) q.toInt().toString() else q.toString() } ?: ""
                val info = it.supplementalInfo?.let { i -> " ($i)" } ?: ""
                "$qty ${it.unit ?: ""} ${it.ingredientName}$info".trim()
            }
            binding.etIngredients.setText(text)
        }
    }

    private fun saveRecipe() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val recipe = (currentRecipe ?: Recipe(title = "")).copy(
                title = title,
                instructions = binding.etInstructions.text.toString(),
                preparationTimeMinutes = binding.etPrepTime.text.toString().toIntOrNull(),
                cookingTimeMinutes = binding.etCookTime.text.toString().toIntOrNull(),
                restingTimeMinutes = binding.etRestingTime.text.toString().toIntOrNull(),
                servings = binding.etServings.text.toString().toIntOrNull(),
                kcalPerServing = binding.etKcal.text.toString().toIntOrNull(),
                difficulty = binding.actvDifficulty.text.toString().takeIf { it.isNotBlank() },
                wineRecommendation = binding.etWine.text.toString(),
                source = binding.etSource.text.toString(),
                locationId = selectedLocationId,
                imageUri = lastImageUri?.toString() ?: currentRecipe?.imageUri,
                updatedAt = System.currentTimeMillis()
            )

            val ingredients = parseIngredientsFromText(binding.etIngredients.text.toString(), recipe.id)

            if (recipe.id == 0L) {
                viewModel.insert(recipe, ingredients)
            } else {
                viewModel.update(recipe, ingredients)
            }
            
            Toast.makeText(this@EditRecipeActivity, "Recette sauvegardée", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun parseIngredientsFromText(text: String, recipeId: Long): List<RecipeIngredient> {
        return text.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val parsed = IngredientParser.parse(line)
                RecipeIngredient(
                    recipeId = recipeId,
                    ingredientName = parsed.name,
                    quantityRequired = parsed.quantity,
                    unit = parsed.unit,
                    supplementalInfo = parsed.supplementalInfo
                )
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer la recette")
            .setMessage("Voulez-vous vraiment supprimer cette recette ?")
            .setPositiveButton("Supprimer") { _, _ ->
                currentRecipe?.let {
                    viewModel.delete(it)
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
        const val EXTRA_RECIPE_ID = "recipe_id"
        const val EXTRA_LOCATION_ID = "location_id"
    }
}
