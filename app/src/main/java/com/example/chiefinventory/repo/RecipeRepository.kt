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

    /**
     * Recherche des recettes contenant les ingrédients fournis.
     */
    suspend fun searchRecipesByIngredients(availableIngredients: List<String>): List<Pair<Recipe, Int>> {
        val allWithIngredients = recipeDao.getAllRecipesWithIngredients()
        val normalizedAvailable = availableIngredients.map { normalize(it) }

        return allWithIngredients.map { item ->
            val recipeIngredients = item.ingredients.map { normalize(it.ingredientName) }
            val matchCount = recipeIngredients.count { recipeIng ->
                normalizedAvailable.any { availIng -> recipeIng.contains(availIng) || availIng.contains(recipeIng) }
            }
            Pair(item.recipe, matchCount)
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
    }

    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredient>) {
        if (ingredients.isEmpty()) return
        
        Log.d("RecipeRepository", "Insertion de ${ingredients.size} ingrédients pour la recette")
        
        // Sauvegarde des liaisons pour cette recette
        recipeDao.insertRecipeIngredients(ingredients)
        
        // Récupérer le stock existant pour éviter les doublons (normalisation accents/espaces)
        val allExisting = ingredientDao.getAllSync()

        for (ri in ingredients) {
            val rawName = ri.ingredientName.trim()
            if (rawName.isEmpty()) continue
            
            // On ne filtre plus ici (déjà fait par l'OCR), on parse pour extraire Qté/Unité/Nom
            val parsed = IngredientParser.parse(rawName)
            val nameToStore = parsed.name
            
            val normalizedNew = normalize(nameToStore)
            val existing = allExisting.find { normalize(it.name) == normalizedNew }

            if (existing == null) {
                Log.d("RecipeRepository", "Création automatique de l'ingrédient global: $nameToStore")
                ingredientDao.insert(Ingredient(
                    name = nameToStore,
                    quantity = parsed.quantity,
                    unit = parsed.unit
                ))
            } else {
                Log.d("RecipeRepository", "L'ingrédient existe déjà: $nameToStore")
            }
        }
    }

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
