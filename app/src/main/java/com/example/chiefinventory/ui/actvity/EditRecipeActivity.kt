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
import com.example.chiefinventory.utils.IngredientParser
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
                val processedText = fullText.replace("|", "\n|")
                val lines = processedText.lines().map { it.trim() }.filter { it.isNotBlank() }
                
                if (lines.isEmpty()) return@launch

                if (binding.etTitle.text.isNullOrBlank()) {
                    binding.etTitle.setText(lines[0])
                }

                // Chargement des ressources
                val wineAppellations = resources.getStringArray(R.array.wine_appellations).toList()
                val wineProducers = resources.getStringArray(R.array.wine_producers).toList()
                val wineKeywords = resources.getStringArray(R.array.wine_keywords).toList()
                val sourceKeywords = resources.getStringArray(R.array.source_keywords).toList()
                val menuCategoryKeywords = resources.getStringArray(R.array.menu_category_keywords).toList()
                val phonePrefixes = resources.getStringArray(R.array.phone_prefixes).toList()
                val stepActionKeywords = resources.getStringArray(R.array.step_action_keywords).toList()

                val wineTitleKeywords = listOf("vin", "accord", "boisson", "boire", "servir avec", "notre vin", "vin conseillé", "vin suggéré")
                val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")
                val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
                
                val zipCodeRegex = Regex("\\b\\d{4,5}\\b")
                val phoneRegex = Regex("(?:(?:\\+|00)32|0)[\\s./-]*[1-9](?:[\\s./-]*\\d{2}){3,4}")
                val stepStartRegex = Regex("\\b(?:${stepActionKeywords.joinToString("|")})\\b", RegexOption.IGNORE_CASE)
                val qtyRegex = Regex("^[|Il!\\d\\-*]") 

                var currentSection = 0 // 0: None, 1: Ingredients, 2: Instructions
                val rawIngredientsList = mutableListOf<String>()
                val instructionsList = mutableListOf<String>()
                val detectedWineList = mutableListOf<String>()
                val detectedSourceList = mutableListOf<String>()
                var detectedServings: String? = null

                for (line in lines) {
                    val lowerLine = line.lowercase()
                    
                    // 1. Détection des portions
                    val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
                    val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)
                    val servingsMatch = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
                    if (servingsMatch != null) {
                        if (detectedServings == null) detectedServings = servingsMatch.groupValues[1]
                        continue 
                    }

                    // 2. REGLE DE BASCULE : Est-ce que ça ressemble à un ingrédient ou une instruction ?
                    val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) && !line.contains(Regex("^\\d+\\."))
                    val isActionVerb = stepStartRegex.containsMatchIn(line)

                    // SI c'est une action, on bascule vers instructions (PRIORITÉ MAX)
                    if (isActionVerb || instructionHeaderKeywords.any { lowerLine.contains(it) }) {
                        currentSection = 2
                        if (instructionHeaderKeywords.any { lowerLine.contains(it) }) continue
                    } 
                    // SINON SI ça ressemble à un ingrédient, on bascule vers ingrédients
                    else if (looksLikeIngredient || ingredientHeaderKeywords.any { lowerLine.contains(it) }) {
                        currentSection = 1
                        if (ingredientHeaderKeywords.any { lowerLine.contains(it) }) continue
                    }

                    // 3. Détection du VIN (Si on n'est pas déjà sûr que c'est un ingrédient ou une instruction)
                    val isWineLine = wineKeywords.any { lowerLine.contains(it) } || 
                                   wineTitleKeywords.any { lowerLine.contains(it) } ||
                                   wineAppellations.any { lowerLine.contains(it) } ||
                                   wineProducers.any { lowerLine.contains(it) }

                    if (isWineLine && !lowerLine.contains("vinaigre") && currentSection != 2 && !looksLikeIngredient) {
                        var cleanWine = line
                        val wineRemovePattern = resources.getString(R.string.wine_remove_pattern)
                        val wineRemoveRegex = Regex(wineRemovePattern, RegexOption.IGNORE_CASE)
                        cleanWine = cleanWine.replace(wineRemoveRegex, "")
                        cleanWine = cleanWine.replace(Regex("(?i)^(?:accord|boisson|boire|servir avec|suggestion)\\s*:?", RegexOption.IGNORE_CASE), "").trim()
                        if (cleanWine.isNotEmpty() && cleanWine.length > 2) detectedWineList.add(cleanWine)
                        continue 
                    }

                    // 4. SOURCE
                    if (phoneRegex.containsMatchIn(line) || sourceKeywords.any { lowerLine.contains(it) } || zipCodeRegex.containsMatchIn(line)) {
                        var cleanSource = line
                        phonePrefixes.forEach { cleanSource = cleanSource.replace(Regex("(?i)$it\\s*[:\\-.]?", RegexOption.IGNORE_CASE), "").trim() }
                        detectedSourceList.add(cleanSource)
                        continue 
                    }

                    // 5. EXCLUSION des catégories de menu
                    if (menuCategoryKeywords.any { lowerLine.contains(it) } && !lowerLine.contains(Regex("\\d"))) {
                        continue
                    }

                    // 6. Remplissage selon la section
                    when (currentSection) {
                        1 -> rawIngredientsList.add(line)
                        2 -> instructionsList.add(line)
                        else -> {
                            if (line != lines[0]) {
                                instructionsList.add(line)
                                if (line.length > 30) currentSection = 2
                            }
                        }
                    }
                }

                // Fusion et Nettoyage des ingrédients
                val finalIngredients = mutableListOf<String>()
                if (rawIngredientsList.isNotEmpty()) {
                    var currentIngredient = IngredientParser.preClean(rawIngredientsList[0])
                    for (i in 1 until rawIngredientsList.size) {
                        val nextLine = IngredientParser.preClean(rawIngredientsList[i])
                        val nextLooksLikeIngredient = qtyRegex.containsMatchIn(nextLine.take(5))
                        if (!nextLooksLikeIngredient && nextLine.length > 2) {
                            currentIngredient += " $nextLine"
                        } else {
                            val cleaned = cleanIngredientText(currentIngredient)
                            if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                            currentIngredient = nextLine
                        }
                    }
                    val cleanedLast = cleanIngredientText(currentIngredient)
                    if (cleanedLast.isNotBlank()) finalIngredients.add(cleanedLast)
                }

                if (binding.etIngredients.text.isNullOrBlank()) binding.etIngredients.setText(finalIngredients.joinToString("\n"))
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

    private fun cleanIngredientText(text: String): String {
        return text.replace(Regex("\\(.*?\\)"), "").replace(Regex("(?i)\\b(ingrédients|ingredients|personnes|portions|pers\\.?)\\b"), "").replace(Regex("\\s{2,}"), " ").trim()
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
