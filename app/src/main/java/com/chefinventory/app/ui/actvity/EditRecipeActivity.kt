package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ActivityEditRecipeBinding
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.utils.ResourceHealthChecker
import com.chefinventory.app.model.RecipeIngredient
import com.chefinventory.app.ui.viewmodel.EditRecipeViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.utils.BitmapUtils
import com.chefinventory.app.utils.ImageCaptureUtil
import com.chefinventory.app.utils.IngredientParser
import com.chefinventory.app.utils.OcrIngredientSpannableFormatter
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.chefinventory.app.utils.TextRecognitionHelper
import com.parabd.lib.traceability.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private lateinit var textRecognitionHelper: TextRecognitionHelper
    
    private var currentRecipe: Recipe? = null
    private var newBitmap: Bitmap? = null
    private var lastImageUri: Uri? = null
    private var categories: List<Location> = emptyList()
    private var selectedLocationId: Long? = null
    private var isOcrScanning = false

    private val viewModel: EditRecipeViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository, app.recipeRepository)
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
        supportActionBar?.title = if (recipeId == -1L) getString(R.string.edit_recipe_title_new) else getString(R.string.edit_recipe_title_edit)

        textRecognitionHelper = TextRecognitionHelper(this)
        imageCaptureUtil = ImageCaptureUtil(this) { uri ->
            if (uri != null) handleNewImage(uri)
        }
        setupCategoryDropdown()
        setupDifficultyDropdown()
        setupPriceRatingDropdown()
        setupSourceWebLink()
        ResourceHealthChecker.check(this)

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
        binding.btnSave.setOnClickListener { saveRecipe() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
        binding.ivRecipe.setOnClickListener {
            val uriStr = lastImageUri?.toString() ?: currentRecipe?.imageUri
            if (!uriStr.isNullOrBlank()) {
                val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                    putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, uriStr)
                    putExtra(FullScreenImageActivity.EXTRA_TITLE, binding.etTitle.text.toString().ifBlank { currentRecipe?.title ?: "" })
                    putExtra(FullScreenImageActivity.EXTRA_DESCRIPTION, binding.etInstructions.text.toString().ifBlank { currentRecipe?.instructions ?: "" })
                }
                startActivity(intent)
            }
        }
    }

    private fun imageGallery() {
        pickImage.launch("image/*")
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun setupCategoryDropdown() {
        viewModel.recipeCategories.observe(this) { list ->
            categories = list
            val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list.map { it.name })
            binding.actvRecipeCategory.setAdapter(adapterDropdown)
            
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

    @Suppress("UsePropertyAccessSyntax")
    private fun setupDifficultyDropdown() {
        val difficulties = resources.getStringArray(R.array.recipe_difficulties)
        val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, difficulties)
        binding.actvDifficulty.setAdapter(adapterDropdown)
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun setupPriceRatingDropdown() {
        val priceRatings = resources.getStringArray(R.array.recipe_price_ratings)
        val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priceRatings)
        binding.actvPriceRating.setAdapter(adapterDropdown)
    }

    private fun mapPriceRatingToDisplay(codeOrName: String?): String {
        if (codeOrName.isNullOrBlank()) return ""
        val ratings = resources.getStringArray(R.array.recipe_price_ratings)
        return when (codeOrName.trim()) {
            "1", "Éco", "Eco", "Bon marché" -> ratings.getOrElse(0) { "" }
            "2", "Moyen", "Medium", "Coût moyen" -> ratings.getOrElse(1) { "" }
            "3", "Cher", "High", "Assez cher" -> ratings.getOrElse(2) { "" }
            else -> codeOrName
        }
    }

    private fun mapDisplayToPriceRating(displayText: String?): String? {
        if (displayText.isNullOrBlank()) return null
        val ratings = resources.getStringArray(R.array.recipe_price_ratings)
        val idx = ratings.indexOfFirst { it.equals(displayText.trim(), ignoreCase = true) }
        return when (idx) {
            0 -> "1"
            1 -> "2"
            2 -> "3"
            else -> displayText.trim()
        }
    }

    private fun setupSourceWebLink() {
        binding.etSource.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSourceWebLinkState(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateSourceWebLinkState(sourceText: String?) {
        val url = extractUrlFromSource(sourceText)
        if (url != null) {
            binding.tilSource.isEndIconVisible = true
            binding.tilSource.setEndIconOnClickListener {
                openWebUrlInBrowser(url)
            }
        } else {
            binding.tilSource.isEndIconVisible = false
            binding.tilSource.setEndIconOnClickListener(null)
        }
    }

    private fun openWebUrlInBrowser(url: String) {
        try {
            val chromeIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                setPackage("com.android.chrome")
            }
            startActivity(chromeIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.toast_cannot_open_web_browser), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleNewImage(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Permission non-persistable, continue normalement
        }
        lastImageUri = uri
        binding.ivRecipe.isVisible = true
        binding.ivRecipe.load(uri)
        newBitmap = BitmapUtils.getBitmapFromUri(this, uri)
        runOcr()
    }

    private fun runOcr() {
        val bitmap = newBitmap ?: return
        isOcrScanning = true
        Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_scan_starting), Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val ocrBitmap = withContext(Dispatchers.IO) {
                    BitmapUtils.scaleBitmapToMaxDimension(bitmap, 2048)
                }
                val visionText = textRecognitionHelper.recognizeVisionText(ocrBitmap) ?: return@launch
                val database = (application as ChefInventoryApplication).database
                val offRepository = OpenFoodFactsRepository(database.openFoodFactsDao())
                val offTaxonomyNames = withContext(Dispatchers.IO) {
                    try {
                        com.chefinventory.app.data.OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(this@EditRecipeActivity, database)
                        offRepository.getAllCleanNames()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                val result = RecipeOcrParser.parse(visionText, resources, offTaxonomyNames)
                
                // Réinitialisation explicite du titre et de la catégorie à chaque nouveau scan pour éviter toute fuite de l'ancienne recette
                binding.etTitle.setText(result.title ?: "")
                selectedLocationId = null
                binding.actvRecipeCategory.setText("", false)

                result.category?.takeIf { it.isNotBlank() }?.let { catName ->
                    val matchedCategory = categories.find { cat -> cat.name.equals(catName, ignoreCase = true) }
                    if (matchedCategory != null) {
                        selectedLocationId = matchedCategory.id
                        binding.actvRecipeCategory.setText(matchedCategory.name, false)
                    } else {
                        binding.actvRecipeCategory.setText(catName, false)
                    }
                }
                val formattedIngredients = withContext(Dispatchers.Default) {
                    OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
                        context = this@EditRecipeActivity,
                        fullText = result.ingredients ?: "",
                        vocabulary = OcrVocabularyLoader.loadFromProjectResources(),
                        offTaxonomyList = offTaxonomyNames
                    )
                }
                binding.etIngredients.movementMethod = android.text.method.LinkMovementMethod.getInstance()
                binding.etIngredients.setText(formattedIngredients)
                binding.etInstructions.setText(result.instructions ?: "")
                binding.etWine.setText(result.wine ?: "")
                binding.etSource.setText(result.source ?: "")
                updateSourceWebLinkState(result.source)
                binding.etServings.setText(result.servings ?: "")
                
                binding.etPrepTime.setText(result.prepTime ?: "")
                binding.etCookTime.setText(result.cookTime ?: "")
                binding.etRestingTime.setText(result.restingTime ?: "")
                binding.etKcal.setText(result.kcalPerServing ?: "")
                if (result.nutriScore == "PENDING") {
                    binding.etNutriscore.setText("")
                    Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_nutriscore_pending), Toast.LENGTH_LONG).show()
                } else {
                    binding.etNutriscore.setText(result.nutriScore ?: "")
                }
                binding.actvDifficulty.setText(result.difficulty ?: "", false)
                binding.actvPriceRating.setText(mapPriceRatingToDisplay(result.priceRating), false)
                binding.etNutritionInfo.setText(result.nutritionInfo ?: "")
            } catch (e: Exception) {
                Log.e("EditRecipe", "OCR failed", e)
                val stackTrace = e.stackTraceToString()
                val fullReport = "📷 ERREUR DE NUMÉRISATION OCR :\nL'analyse de l'image a rencontré un problème (image invalide, mémoire saturée ou erreur de lecture).\n\n--- DÉTAILS TECHNIQUES ---\n$stackTrace"
                val crashIntent = Intent(this@EditRecipeActivity, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(CrashActivity.EXTRA_CRASH_INFO, fullReport)
                }
                startActivity(crashIntent)
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
        updateSourceWebLinkState(recipe.source)
        binding.etKcal.setText(recipe.kcalPerServing?.toString() ?: "")
        binding.etNutriscore.setText(recipe.nutriScore ?: "")
        binding.actvDifficulty.setText(recipe.difficulty ?: "", false)
        binding.actvPriceRating.setText(mapPriceRatingToDisplay(recipe.priceRating), false)
        binding.etNutritionInfo.setText(recipe.nutritionInfo ?: "")
        
        recipe.imageUri?.let {
            try {
                val uri = it.toUri()
                lastImageUri = uri
                binding.ivRecipe.isVisible = true
                binding.ivRecipe.load(uri) {
                    listener(
                        onError = { _, result ->
                            LogManager.e("EditRecipe", "Failed to load recipe image: ${result.throwable.message}")
                            Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_recipe_image_permission_denied), Toast.LENGTH_LONG).show()
                        }
                    )
                }
                lifecycleScope.launch {
                    try {
                        newBitmap = withContext(Dispatchers.IO) {
                            BitmapUtils.getBitmapFromUri(this@EditRecipeActivity, uri)
                        }
                    } catch (e: Exception) {
                        LogManager.e("EditRecipe", "Failed to decode bitmap from URI: $uri", e)
                        Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_recipe_image_permission_denied), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                LogManager.e("EditRecipe", "SecurityException or error accessing image URI: $it", e)
                Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_recipe_image_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
        
        viewModel.getIngredientsForRecipe(recipe.id).observe(this) { ingredients ->
            if (!isOcrScanning) {
                val text = ingredients.joinToString("\n") { 
                    val qty = it.quantityRequired?.let { q -> if (q % 1.0 == 0.0) q.toInt().toString() else q.toString() } ?: ""
                    val unitStr = if (!it.unit.isNullOrBlank()) "${it.unit} " else ""
                    val info = it.supplementalInfo?.takeIf { i -> i.isNotBlank() }?.let { i -> " ($i)" } ?: ""
                    "$qty $unitStr${it.ingredientName}$info".replace(Regex("\\s+"), " ").trim()
                }
                lifecycleScope.launch {
                    val database = (application as ChefInventoryApplication).database
                    val offRepository = OpenFoodFactsRepository(database.openFoodFactsDao())
                    val offTaxonomyNames = withContext(Dispatchers.IO) {
                        try {
                            com.chefinventory.app.data.OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(this@EditRecipeActivity, database)
                            offRepository.getAllCleanNames()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                    val formattedSpannable = withContext(Dispatchers.Default) {
                        OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
                            context = this@EditRecipeActivity,
                            fullText = text,
                            vocabulary = OcrVocabularyLoader.loadFromProjectResources(),
                            offTaxonomyList = offTaxonomyNames
                        )
                    }
                    binding.etIngredients.movementMethod = android.text.method.LinkMovementMethod.getInstance()
                    binding.etIngredients.setText(formattedSpannable)
                }
            }
        }
    }

    private fun saveRecipe() {
        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, getString(R.string.toast_recipe_title_required), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.btnSave.isEnabled = false
            try {
                withContext(Dispatchers.IO) {
                    val savedImageUriStr = if (newBitmap != null) {
                        try {
                            val fileName = "recipe_${System.currentTimeMillis()}.jpg"
                            val internalUri = BitmapUtils.saveBitmapToInternalStorage(this@EditRecipeActivity, newBitmap!!, fileName)
                            internalUri.toString()
                        } catch (e: Exception) {
                            LogManager.e("EditRecipe", "Failed to save image to internal storage", e)
                            lastImageUri?.toString() ?: currentRecipe?.imageUri
                        }
                    } else {
                        lastImageUri?.toString() ?: currentRecipe?.imageUri
                    }

                    val recipe = (currentRecipe ?: Recipe(title = "")).copy(
                        title = title,
                        instructions = binding.etInstructions.text.toString(),
                        preparationTimeMinutes = binding.etPrepTime.text.toString().toIntOrNull(),
                        cookingTimeMinutes = binding.etCookTime.text.toString().toIntOrNull(),
                        restingTimeMinutes = binding.etRestingTime.text.toString().toIntOrNull(),
                        servings = binding.etServings.text.toString().toIntOrNull(),
                        kcalPerServing = binding.etKcal.text.toString().toIntOrNull(),
                        difficulty = binding.actvDifficulty.text.toString().takeIf { it.isNotBlank() },
                        priceRating = mapDisplayToPriceRating(binding.actvPriceRating.text.toString()),
                        wineRecommendation = binding.etWine.text.toString(),
                        source = binding.etSource.text.toString(),
                        nutritionInfo = binding.etNutritionInfo.text.toString().takeIf { it.isNotBlank() },
                        nutriScore = binding.etNutriscore.text.toString().takeIf { it.isNotBlank() },
                        locationId = selectedLocationId,
                        imageUri = savedImageUriStr,
                        updatedAt = System.currentTimeMillis()
                    )

                    val ingredients = parseIngredientsFromText(binding.etIngredients.text.toString(), recipe.id)

                    if (recipe.id == 0L) {
                        viewModel.insert(recipe, ingredients)
                    } else {
                        viewModel.update(recipe, ingredients)
                    }
                }
                Toast.makeText(this@EditRecipeActivity, getString(R.string.toast_recipe_saved), Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                LogManager.e("EditRecipe", "Error saving recipe", e)
                binding.btnSave.isEnabled = true
            }
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
            .setTitle(R.string.dialog_delete_recipe_title)
            .setMessage(R.string.dialog_delete_recipe_message)
            .setPositiveButton(R.string.supprimer) { _, _ ->
                currentRecipe?.let {
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
        const val EXTRA_RECIPE_ID: String = "recipe_id"
        const val EXTRA_LOCATION_ID: String = "location_id"

        fun extractUrlFromSource(sourceText: String?): String? {
            if (sourceText.isNullOrBlank()) {
                return null
            }

            val trimmed = sourceText.trim()
            val regex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
            val match = regex.find(trimmed)
            return match?.value
        }
    }
}
