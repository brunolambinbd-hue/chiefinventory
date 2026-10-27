package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.utils.MatchRecipeInventoryEngine
import com.example.chiefinventory.utils.RecipeMatchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val recipeDao = db.recipeDao()
    private val ingredientDao = db.ingredientDao()

    private val _matchedRecipes = MutableLiveData<List<RecipeMatchResult>>()
    val matchedRecipes: LiveData<List<RecipeMatchResult>> = _matchedRecipes

    fun calculateMatches() {
        viewModelScope.launch(Dispatchers.IO) {
            val recipesWithIngredients = recipeDao.getAllRecipesWithIngredients()
            val pantryIngredients = ingredientDao.getAllSync()

            val recipes = recipesWithIngredients.map { it.recipe }
            val recipeIngredientsMap = recipesWithIngredients.associate { it.recipe.id to it.ingredients }

            val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(recipes, recipeIngredientsMap, pantryIngredients)
            _matchedRecipes.postValue(matches)
        }
    }
}
