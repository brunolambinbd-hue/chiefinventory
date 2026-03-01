package com.example.chiefinventory.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.repo.RecipeRepository
import kotlinx.coroutines.launch

class EditRecipeViewModel(
    private val repository: RecipeRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = _recipe

    private val _recipeIngredients = MutableLiveData<List<RecipeIngredient>>()
    val recipeIngredients: LiveData<List<RecipeIngredient>> = _recipeIngredients

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    val recipeCategories: LiveData<List<Location>> = locationRepository.getAllByType(LocationType.RECIPE)

    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            val recipeData = repository.getRecipeById(id)
            _recipe.value = recipeData
        }
    }

    fun getIngredientsForRecipe(recipeId: Long): LiveData<List<RecipeIngredient>> {
        return repository.getIngredientsForRecipe(recipeId)
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun insert(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            val recipeId = repository.insertRecipe(recipe)
            val ingredientsWithId = ingredients.map { it.copy(recipeId = recipeId) }
            repository.insertRecipeIngredients(ingredientsWithId)
        }
    }

    fun update(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            repository.updateRecipe(recipe)
            repository.deleteIngredientsByRecipeId(recipe.id)
            val ingredientsWithId = ingredients.map { it.copy(recipeId = recipe.id) }
            repository.insertRecipeIngredients(ingredientsWithId)
        }
    }

    fun delete(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteIngredientsByRecipeId(recipe.id)
            repository.deleteRecipe(recipe)
        }
    }
}
