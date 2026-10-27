package com.example.chiefinventory.utils

import android.content.Context
import android.util.Log
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import org.json.JSONArray
import org.json.JSONObject

/**
 * Chargeur d'importation sur demande des 58 recettes d'inspiration de départ depuis `starter_recipes_seed.json`.
 *
 * @see REQ-4.8.1.1 Carnet de 58 recettes de départ libres de droits.
 * @see REQ-4.8.1.2 Importation sur demande via le menu latéral.
 */
object StarterRecipeSeedLoader {

    private const val TAG = "StarterRecipeSeedLoader"

    /**
     * Structure de recette de départ extraite du JSON.
     *
     * @property title Titre de la recette.
     * @property category Catégorie (Entrée, Plat principal, Dessert).
     * @property prepTimeMinutes Temps de préparation en minutes.
     * @property cookTimeMinutes Temps de cuisson en minutes.
     * @property servings Nombre de portions.
     * @property ingredients Liste des lignes d'ingrédients brutes.
     * @property instructions Liste des consignes/étapes.
     * @property sourceUrl URL source d'origine.
     */
    data class SeedRecipe(
        val title: String,
        val category: String,
        val cuisineOrigin: String?,
        val prepTimeMinutes: Int?,
        val cookTimeMinutes: Int?,
        val servings: Int?,
        val ingredients: List<String>,
        val instructions: List<String>,
        val sourceUrl: String?
    )

    /**
     * Lit et parse n'importe quel fichier JSON de pack de recettes depuis le dossier assets (ex: "seeds/recipes_seed_be.json").
     */
    fun loadSeedRecipesFromAssetFile(context: Context, assetPath: String): List<SeedRecipe> {
        val recipes = mutableListOf<SeedRecipe>()
        try {
            val jsonStr = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)

            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val parsed = parseSeedRecipeJsonObject(obj)
                if (parsed != null) {
                    recipes.add(parsed)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la lecture de $assetPath", e)
        }
        return recipes
    }

    /**
     * Lit et parse le fichier JSON principal `starter_recipes_seed.json` depuis le dossier assets.
     */
    fun loadSeedRecipesFromAssets(context: Context): List<SeedRecipe> {
        return loadSeedRecipesFromAssetFile(context, "starter_recipes_seed.json")
    }

    /**
     * Parse un objet JSON de recette du fichier seed.
     */
    fun parseSeedRecipeJsonObject(obj: JSONObject): SeedRecipe? {
        val title = obj.optString("title", "").trim()
        if (title.isBlank()) return null

        val category = obj.optString("category", "Plat principal").trim()
        val prep = if (obj.has("prepTimeMinutes") && !obj.isNull("prepTimeMinutes")) obj.optInt("prepTimeMinutes") else null
        val cook = if (obj.has("cookTimeMinutes") && !obj.isNull("cookTimeMinutes")) obj.optInt("cookTimeMinutes") else null
        val serv = if (obj.has("servings") && !obj.isNull("servings")) obj.optInt("servings") else null
        val sourceUrl = obj.optString("sourceUrl", "").ifBlank { null }

        val ingredientsList = mutableListOf<String>()
        val ingrArray = obj.optJSONArray("ingredients")
        if (ingrArray != null) {
            for (i in 0 until ingrArray.length()) {
                val line = ingrArray.optString(i, "").trim()
                if (line.isNotBlank()) ingredientsList.add(line)
            }
        }

        val instructionsList = mutableListOf<String>()
        val instArray = obj.optJSONArray("instructions")
        if (instArray != null) {
            for (i in 0 until instArray.length()) {
                val line = instArray.optString(i, "").trim()
                if (line.isNotBlank()) instructionsList.add(line)
            }
        }

        val cuisineOrigin = obj.optString("cuisineOrigin", "").ifBlank { null }

        return SeedRecipe(
            title = title,
            category = category,
            cuisineOrigin = cuisineOrigin,
            prepTimeMinutes = prep,
            cookTimeMinutes = cook,
            servings = serv,
            ingredients = ingredientsList,
            instructions = instructionsList,
            sourceUrl = sourceUrl
        )
    }

    data class ImportSeedResult(
        val insertedCount: Int,
        val updatedCount: Int
    )

    suspend fun importCountryPackToDatabase(context: Context, appDatabase: AppDatabase, assetPath: String): ImportSeedResult {
        val seedRecipes = loadSeedRecipesFromAssetFile(context, assetPath)
        if (seedRecipes.isEmpty()) return ImportSeedResult(0, 0)
        return importSeedListToDatabase(seedRecipes, appDatabase)
    }

    /**
     * Insère ou met à jour le lot de recettes d'inspiration dans la base SQLite Room.
     * Si une recette existe déjà par son titre, son origine régionale est mise à jour sans créer de doublon.
     *
     * @see REQ-4.8.1.1
     * @return Un objet [ImportSeedResult] contenant le nombre d'insertions et de mises à jour.
     */
    suspend fun importSeedRecipesToDatabase(context: Context, appDatabase: AppDatabase): ImportSeedResult {
        val seedRecipes = loadSeedRecipesFromAssets(context)
        if (seedRecipes.isEmpty()) return ImportSeedResult(0, 0)
        return importSeedListToDatabase(seedRecipes, appDatabase)
    }

    suspend fun importSeedListToDatabase(seedRecipes: List<SeedRecipe>, appDatabase: AppDatabase): ImportSeedResult {
        val recipeDao = appDatabase.recipeDao()
        val locationDao = appDatabase.locationDao()

        val existingRecipesWithIngr = recipeDao.getAllRecipesWithIngredients()
        val existingMap = existingRecipesWithIngr.associateBy { it.recipe.title.lowercase().trim() }
        val standardRecipeCategoryNames = listOf("Entrée", "Plat principal", "Dessert", "Boisson", "Accompagnement")
        var recipeLocations = locationDao.getAllSync().filter { it.type == LocationType.RECIPE }

        for (catName in standardRecipeCategoryNames) {
            val normalizedCatName = CategoryMapper.normalizeRecipeCategory(catName) ?: catName
            if (recipeLocations.none { CategoryMapper.normalizeRecipeCategory(it.name) == normalizedCatName }) {
                locationDao.insert(com.example.chiefinventory.model.Location(name = normalizedCatName, parentId = null, type = LocationType.RECIPE))
            }
        }
        recipeLocations = locationDao.getAllSync().filter { it.type == LocationType.RECIPE }

        var insertedCount = 0
        var updatedCount = 0

        for (seed in seedRecipes) {
            val cleanTitle = seed.title.lowercase().trim()
            val normalizedSeedCat = CategoryMapper.normalizeRecipeCategory(seed.category) ?: seed.category
            val matchedCat = recipeLocations.find { CategoryMapper.normalizeRecipeCategory(it.name) == normalizedSeedCat }
            val fullSource = listOfNotNull(seed.cuisineOrigin, seed.sourceUrl).filter { it.isNotBlank() }.joinToString(" | ")

            val existing = existingMap[cleanTitle]
            if (existing != null) {
                val updatedRecipe = existing.recipe.copy(
                    instructions = if (existing.recipe.instructions.isNullOrBlank() && seed.instructions.isNotEmpty()) seed.instructions.joinToString("\n\n") else existing.recipe.instructions,
                    source = if (fullSource.isNotBlank()) fullSource else existing.recipe.source,
                    locationId = if (existing.recipe.locationId == null || existing.recipe.locationId == -1L) matchedCat?.id else existing.recipe.locationId
                )
                recipeDao.updateRecipe(updatedRecipe)

                // Si la recette existante n'avait pas d'ingrédients, injecter les ingrédients du seed
                if (existing.ingredients.isEmpty() && seed.ingredients.isNotEmpty()) {
                    val recipeIngredients = seed.ingredients.map { raw ->
                        val parsed = IngredientParser.parse(raw)
                        RecipeIngredient(
                            recipeId = existing.recipe.id,
                            ingredientName = parsed.name,
                            quantityRequired = parsed.quantity,
                            unit = parsed.unit,
                            supplementalInfo = parsed.supplementalInfo
                        )
                    }
                    recipeDao.insertRecipeIngredients(recipeIngredients)
                }

                updatedCount++
            } else {
                val recipe = Recipe(
                    title = seed.title,
                    instructions = seed.instructions.joinToString("\n\n"),
                    preparationTimeMinutes = seed.prepTimeMinutes,
                    cookingTimeMinutes = seed.cookTimeMinutes,
                    servings = seed.servings,
                    source = fullSource.ifBlank { null },
                    locationId = matchedCat?.id
                )

                val recipeId = recipeDao.insertRecipe(recipe)

                val recipeIngredients = seed.ingredients.map { raw ->
                    val parsed = IngredientParser.parse(raw)
                    RecipeIngredient(
                        recipeId = recipeId,
                        ingredientName = parsed.name,
                        quantityRequired = parsed.quantity,
                        unit = parsed.unit,
                        supplementalInfo = parsed.supplementalInfo
                    )
                }

                recipeDao.insertRecipeIngredients(recipeIngredients)
                insertedCount++
            }
        }

        return ImportSeedResult(insertedCount, updatedCount)
    }
}
