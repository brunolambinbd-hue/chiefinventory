package com.chefinventory.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.RecipeRepository

/**
 * ViewModel for the main dashboard (Stock, Ingredients, Pantry, Recipes, Shopping List).
 */
class MainViewModel(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository? = null
) : ViewModel() {

    /** Ingredient stats */
    val allIngredients: LiveData<List<Ingredient>> = ingredientRepository.getAll()

    /** Shopping list / out of stock ingredients */
    val outOfStockIngredients: LiveData<List<Ingredient>> = ingredientRepository.getOutOfStockIngredients()

    /** Recipe stats */
    val allRecipes: LiveData<List<Recipe>>? = recipeRepository?.allRecipes
}
