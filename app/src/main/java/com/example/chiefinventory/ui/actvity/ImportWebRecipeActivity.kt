package com.example.chiefinventory.ui.actvity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityImportWebRecipeBinding
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.utils.IngredientParser
import com.example.chiefinventory.utils.WebRecipeImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportWebRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportWebRecipeBinding
    private var lastResult: WebRecipeImporter.WebRecipeResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportWebRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnImportUrl.setOnClickListener {
            val url = binding.etRecipeUrl.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(this, "Veuillez coller une URL valide", Toast.LENGTH_SHORT).show()
            } else {
                fetchWebRecipe(url)
            }
        }

        binding.btnSaveRecipe.setOnClickListener {
            saveRecipeToDatabase()
        }

        handleShareIntent()
    }

    private fun handleShareIntent() {
        val action = intent?.action
        val type = intent?.type
        if (android.content.Intent.ACTION_SEND == action && type != null && "text/plain" == type) {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: ""
            val extractedUrl = extractUrlFromSharedText(sharedText)
            if (extractedUrl.isNotBlank()) {
                binding.etRecipeUrl.setText(extractedUrl)
                fetchWebRecipe(extractedUrl)
            }
        }
    }

    private fun fetchWebRecipe(url: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollPreview.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val result = WebRecipeImporter.importFromUrl(url)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (result.title.isNullOrBlank()) {
                    Toast.makeText(this@ImportWebRecipeActivity, "Impossible d'extraire la recette depuis cette URL", Toast.LENGTH_SHORT).show()
                } else {
                    lastResult = result
                    displayPreview(result)
                }
            }
        }
    }

    private fun displayPreview(result: WebRecipeImporter.WebRecipeResult) {
        binding.scrollPreview.visibility = View.VISIBLE
        binding.tvPreviewTitle.text = result.title

        val catStr = result.category?.let { "Catégorie: $it" } ?: ""
        val prep = result.prepTimeMinutes?.let { "Prép: ${it}min" } ?: ""
        val cook = result.cookTimeMinutes?.let { "Cuisson: ${it}min" } ?: ""
        val serv = result.servings?.let { "Portions: $it" } ?: ""
        binding.tvPreviewMetadata.text = listOf(catStr, prep, cook, serv).filter { it.isNotBlank() }.joinToString(" | ")

        if (!result.imageUrl.isNullOrBlank()) {
            binding.ivRecipeImage.visibility = View.VISIBLE
            binding.ivRecipeImage.load(result.imageUrl)
        } else {
            binding.ivRecipeImage.visibility = View.GONE
        }

        binding.tvPreviewIngredients.text = if (result.ingredients.isNotEmpty()) {
            result.ingredients.joinToString("\n") { "• $it" }
        } else {
            "Aucun ingrédient extrait"
        }

        binding.tvPreviewInstructions.text = if (result.instructions.isNotEmpty()) {
            result.instructions.mapIndexed { index, inst -> "${index + 1}. $inst" }.joinToString("\n\n")
        } else {
            "Aucune instruction extraite"
        }
    }

    private fun saveRecipeToDatabase() {
        val result = lastResult ?: return
        val app = application as CollectionApplication

        lifecycleScope.launch(Dispatchers.IO) {
            val prepMinutes = result.prepTimeMinutes?.toIntOrNull()
            val cookMinutes = result.cookTimeMinutes?.toIntOrNull()
            val servingsInt = result.servings?.toIntOrNull()

            val db = com.example.chiefinventory.data.AppDatabase.getDatabase(this@ImportWebRecipeActivity)
            val locations = db.locationDao().getAllSync()
            val matchedCat = result.category?.let { catName ->
                locations.find { it.name.equals(catName, ignoreCase = true) }
            }

            val recipe = Recipe(
                title = result.title ?: "Recette Web",
                instructions = result.instructions.joinToString("\n\n"),
                preparationTimeMinutes = prepMinutes,
                cookingTimeMinutes = cookMinutes,
                servings = servingsInt,
                imageUri = result.imageUrl,
                source = result.sourceUrl,
                locationId = matchedCat?.id
            )

            val recipeId = app.recipeRepository.insertRecipe(recipe)

            val recipeIngredients = result.ingredients.map { rawIng ->
                val parsed = IngredientParser.parse(rawIng)
                RecipeIngredient(
                    recipeId = recipeId,
                    ingredientName = parsed.name,
                    quantityRequired = parsed.quantity,
                    unit = parsed.unit,
                    supplementalInfo = parsed.supplementalInfo
                )
            }

            app.recipeRepository.insertRecipeIngredients(recipeIngredients)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ImportWebRecipeActivity, "Recette '${result.title}' enregistrée avec succès !", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        fun extractUrlFromSharedText(text: String): String {
            val matcher = Regex("https?://[\\w\\d.\\-_~:/?#\\[\\]@!$&'()*+,;=%]+").find(text)
            return matcher?.value?.trim() ?: text.trim()
        }
    }
}
