package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.repo.RecipeRepository

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    private val _categoryId = MutableLiveData<Long>(-1L)

    // Par défaut on affiche TOUT (categoryId = -1)
    private val _baseRecipes = _categoryId.switchMap { catId ->
        resolveBaseRecipes(catId, repository)
    }

    val recipes: LiveData<List<Recipe>> = MediatorLiveData<List<Recipe>>().apply {
        fun update() {
            val query = _searchQuery.value
            val baseList = _baseRecipes.value
            val list = if (baseList != null) {
                baseList
            } else {
                emptyList()
            }

            value = filterRecipes(list, query)
        }

        addSource(_baseRecipes) { update() }
        addSource(_searchQuery) { update() }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryId(id: Long) {
        _categoryId.value = id
    }

    companion object {
        fun resolveBaseRecipes(catId: Long?, repository: RecipeRepository): LiveData<List<Recipe>> {
            if (catId == null || catId == -1L) {
                return repository.allRecipes
            } else {
                return repository.getRecipesByCategory(catId)
            }
        }

        fun filterRecipes(recipes: List<Recipe>, query: String?): List<Recipe> {
            val cleanQuery = if (!query.isNullOrBlank()) {
                query.trim()
            } else {
                ""
            }

            if (cleanQuery.isBlank()) {
                return recipes
            }

            return recipes.filter { recipe ->
                val titleMatches = recipe.title.contains(cleanQuery, ignoreCase = true)
                val instructions = recipe.instructions
                val instructionsMatches = if (instructions != null) {
                    instructions.contains(cleanQuery, ignoreCase = true)
                } else {
                    false
                }

                titleMatches || instructionsMatches
            }
        }
    }
}
