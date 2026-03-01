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
import com.example.chiefinventory.databinding.ActivityEditRecipeBinding
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.ui.viewmodel.EditRecipeViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.ImageCaptureUtil
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

        binding.btnTakePicture.setOnClickListener { imageCaptureUtil.start() }
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
                val lines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
                if (lines.isEmpty()) return@launch

                if (binding.etTitle.text.isNullOrBlank()) {
                    binding.etTitle.setText(lines[0])
                }

                val ingredientKeywords = listOf("ingrédients", "ingredients", "pour", "composition")
                val instructionKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
                val wineKeywords = listOf("merlot", "bordeaux", "bourgogne", "rhone", "alsace", "bulgarie", "italie", "espagne", "cépage", "region", "chardonnay", "sauvignon", "touraine", "cl", "vol", "75cl", "75 cl")
                val wineTitleKeywords = listOf("vin", "accord", "boisson", "boire", "servir avec", "notre vin", "vin conseillé", "vin suggéré")
                
                val sourceKeywords = listOf("hôtel", "hotel", "restaurant", "rue", "avenue", "place", "boulevard", "route", "cedex", "paris", "bruxelles")
                val phonePrefixes = listOf("tel", "tél", "phone", "gsm", "mobile", "contact", "fax")
                
                val zipCodeRegex = Regex("\\b\\d{4,5}\\b")
                val phoneRegex = Regex("(?:(?:\\+|00)32|0)[\\s./-]*[1-9](?:[\\s./-]*\\d{2}){3,4}")
                
                // Détection du début des instructions : numérotation OU verbe d'action français commun (infinitif ou impératif)
                val stepStartRegex = Regex("^(?:[\\d\\-*]+[.)]|étape|méthode|dans|faire|mélanger|couper|cuire|préchauffer|ajouter|prélevez|éplucher|hacher|émincer|verser|fouetter|incorporer|laisser|mettre|disposer|servir|nettoyer|laver|cuisez|mélangez|coupez|versez|ajoutez)", RegexOption.IGNORE_CASE)

                var currentSection = 0 
                val ingredientsList = mutableListOf<String>()
                val instructionsList = mutableListOf<String>()
                val detectedWineList = mutableListOf<String>()
                val detectedSourceList = mutableListOf<String>()
                var detectedServings: String? = null

                for (line in lines) {
                    val lowerLine = line.lowercase()
                    
                    // 1. Détection des portions
                    val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
                    val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)
                    if (detectedServings == null) {
                        val match = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
                        match?.let { detectedServings = it.groupValues[1] }
                    }

                    // 2. Détection du vin
                    val containsWineKey = wineKeywords.any { lowerLine.contains(it) }
                    val isWineTitle = wineTitleKeywords.any { lowerLine.contains(it) }

                    if (containsWineKey || isWineTitle) {
                        var cleanWine = line
                        cleanWine = cleanWine.replace(Regex("(?i)^(?:notre\\s+)?vin\\s+(?:conseillé|suggéré)?\\s*:?", RegexOption.IGNORE_CASE), "")
                        cleanWine = cleanWine.replace(Regex("(?i)^(?:accord|boisson|boire|servir avec)\\s*:?", RegexOption.IGNORE_CASE), "")
                        cleanWine = cleanWine.trim()
                        if (cleanWine.isNotEmpty() && cleanWine.length > 2) {
                            detectedWineList.add(cleanWine)
                        }
                        continue 
                    }

                    // 3. Détection de la source / adresse / téléphone
                    val isPhone = phoneRegex.containsMatchIn(line)
                    val isAddress = sourceKeywords.any { lowerLine.contains(it) } || zipCodeRegex.containsMatchIn(line)

                    if (isPhone || isAddress) {
                        var cleanSource = line
                        if (isPhone) {
                            phonePrefixes.forEach { prefix ->
                                cleanSource = cleanSource.replace(Regex("(?i)$prefix\\s*[:\\-.]?", RegexOption.IGNORE_CASE), "").trim()
                            }
                        }
                        detectedSourceList.add(cleanSource)
                        continue 
                    }

                    // 4. Détection du passage aux INSTRUCTIONS (très important)
                    if (instructionKeywords.any { lowerLine.contains(it) } || stepStartRegex.containsMatchIn(line)) {
                        currentSection = 2
                        // On continue SEULEMENT si c'est un mot-clé de titre (ex: "Préparation :"), 
                        // mais on GARDE la ligne si c'est déjà une action (ex: "Prélevez finement...")
                        if (instructionKeywords.any { lowerLine.contains(it) } && !stepStartRegex.containsMatchIn(line)) continue 
                    } else if (ingredientKeywords.any { lowerLine.contains(it) }) {
                        currentSection = 1
                        continue
                    }

                    // 5. Remplissage des sections
                    when (currentSection) {
                        1 -> ingredientsList.add(line)
                        2 -> instructionsList.add(line)
                        else -> {
                            // Par défaut si pas encore de section, on devine par le format
                            if (Regex("^[\\d\\-*]").containsMatchIn(line)) ingredientsList.add(line)
                            else if (line != lines[0]) instructionsList.add(line)
                        }
                    }
                }

                if (binding.etIngredients.text.isNullOrBlank()) binding.etIngredients.setText(ingredientsList.joinToString("\n"))
                if (binding.etInstructions.text.isNullOrBlank()) binding.etInstructions.setText(instructionsList.joinToString("\n"))
                if (binding.etServings.text.isNullOrBlank() && detectedServings != null) binding.etServings.setText(detectedServings)
                
                if (binding.etWine.text.isNullOrBlank() && detectedWineList.isNotEmpty()) {
                    val finalWine = detectedWineList.joinToString(" ").replace(Regex("^[:\\-\\s\\.]+"), "").trim()
                    binding.etWine.setText(finalWine)
                }

                if (binding.etSource.text.isNullOrBlank() && detectedSourceList.isNotEmpty()) {
                    binding.etSource.setText(detectedSourceList.joinToString(", "))
                }

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
            Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show()
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
