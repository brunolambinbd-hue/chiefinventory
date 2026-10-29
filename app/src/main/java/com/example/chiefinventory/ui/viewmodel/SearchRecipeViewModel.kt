package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.dao.RecipeWithIngredients
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for searching recipes based on available ingredients.
 */
class SearchRecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    // Results initialized as an empty list
    private val _searchResults = MutableLiveData<List<Pair<Recipe, Int>>>(emptyList())
    val searchResults: LiveData<List<Pair<Recipe, Int>>> = _searchResults

    // List of ingredients selected by the user
    private val _selectedIngredients = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedIngredients: LiveData<MutableSet<String>> = _selectedIngredients

    // All recipes with their ingredients
    val recipesWithIngredients: LiveData<List<RecipeWithIngredients>> = recipeRepository.getAllRecipesWithIngredientsLiveData()

    // All available ingredients in the database
    val allIngredients: LiveData<List<Ingredient>> = ingredientRepository.getAll()

    /**
     * Toggles an ingredient in the selection.
     */
    fun toggleIngredient(name: String) {
        val current = toggleIngredientSet(_selectedIngredients.value, name)
        _selectedIngredients.value = current
        performSearch()
    }

    /**
     * Clears all selected ingredients.
     */
    fun clearSelection() {
        _selectedIngredients.value = mutableSetOf()
        _searchResults.value = emptyList()
    }

    /**
     * Executes the search based on selected ingredients.
     */
    private fun performSearch() {
        val set = _selectedIngredients.value
        val selected = if (set != null) {
            set.toList()
        } else {
            emptyList()
        }

        if (selected.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val results = recipeRepository.searchRecipesByIngredients(selected)
            _searchResults.value = results
        }
    }

    companion object {
        fun toggleIngredientSet(currentSet: MutableSet<String>?, name: String): MutableSet<String> {
            val set = if (currentSet != null) {
                currentSet
            } else {
                mutableSetOf()
            }

            if (set.contains(name)) {
                set.remove(name)
            } else {
                set.add(name)
            }

            return set
        }
    }
}
