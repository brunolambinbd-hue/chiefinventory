package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.dao.RecipeDao
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.utils.MatchRecipeInventoryEngine
import com.chefinventory.app.utils.RecipeMatchResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MatchRecipeViewModel(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val recipeDao: RecipeDao = AppDatabase.getDatabase(application).recipeDao(),
    private val ingredientDao: IngredientDao = AppDatabase.getDatabase(application).ingredientDao()
) : AndroidViewModel(application) {

    private val _matchedRecipes = MutableLiveData<List<RecipeMatchResult>>()
    val matchedRecipes: LiveData<List<RecipeMatchResult>> = _matchedRecipes

    fun calculateMatches(): Job = viewModelScope.launch(ioDispatcher) {
        val recipesWithIngredients = recipeDao.getAllRecipesWithIngredients()
        val pantryIngredients = ingredientDao.getAllSync()

        val recipes = recipesWithIngredients.map { it.recipe }
        val recipeIngredientsMap = recipesWithIngredients.associate { it.recipe.id to it.ingredients }

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(recipes, recipeIngredientsMap, pantryIngredients)
        _matchedRecipes.postValue(matches)
    }
}
