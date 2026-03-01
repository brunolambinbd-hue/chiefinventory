package com.example.chiefinventory.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.utils.IngredientParser
import java.text.Normalizer

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {

    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    fun getRecipesByCategory(categoryId: Long): LiveData<List<Recipe>> = 
        recipeDao.getRecipesByCategory(categoryId)

    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredient>) {
        if (ingredients.isEmpty()) return
        
        Log.d("RecipeRepository", "Insertion de ${ingredients.size} ingrédients pour la recette")
        
        recipeDao.insertRecipeIngredients(ingredients)
        
        val wineKeywords = listOf(
            "vin", "chateau", "domaine", "cuvée", "rouge", "blanc", "rosé", "bouteille",
            "merlot", "cabernet", "syrah", "chardonnay", "sauvignon", "pinot", "malbec",
            "bordeaux", "bourgogne", "rhone", "alsace", "bulgarie", "italie", "espagne",
            "pays d'oc", "cépage", "cru", "appellation", "aop", "igp", "touraine"
        )

        // Récupérer tous les ingrédients pour comparer sans accents ni espaces
        val allExisting = ingredientDao.getAllSync()

        for (ri in ingredients) {
            val rawName = ri.ingredientName.trim()
            if (rawName.isEmpty()) continue
            
            val lowerName = rawName.lowercase()
            val isWine = wineKeywords.any { lowerName.contains(it) }
            
            if (isWine) {
                Log.d("RecipeRepository", "Exclusion du vin: $rawName")
                continue
            }

            val parsed = IngredientParser.parse(rawName)
            val nameToStore = parsed.name
            
            // On cherche si une version normalisée existe déjà
            val normalizedNew = normalize(nameToStore)
            val existing = allExisting.find { normalize(it.name) == normalizedNew }

            if (existing == null) {
                Log.d("RecipeRepository", "Création auto: ${parsed.quantity} ${parsed.unit ?: ""} $nameToStore")
                ingredientDao.insert(Ingredient(
                    name = nameToStore,
                    quantity = parsed.quantity,
                    unit = parsed.unit
                ))
            } else {
                Log.d("RecipeRepository", "L'ingrédient existe déjà (similitude détectée): $nameToStore (trouvé: ${existing.name})")
            }
        }
    }

    /**
     * Normalise une chaîne : minuscules, sans accents, sans espaces.
     */
    private fun normalize(input: String): String {
        val temp = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        return temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                   .replace(" ", "")
                   .replace("-", "")
    }

    fun getIngredientsForRecipe(recipeId: Long): LiveData<List<RecipeIngredient>> = 
        recipeDao.getIngredientsForRecipe(recipeId)

    suspend fun deleteIngredientsByRecipeId(recipeId: Long) = 
        recipeDao.deleteIngredientsByRecipeId(recipeId)

    fun getRecipeCategoriesWithCount(): LiveData<List<RecipeCategoryInfo>> = 
        recipeDao.getRecipeCategoriesWithCount()
}
