package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.*
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.repo.RecipeRepository

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    private val _categoryId = MutableLiveData<Long>(-1L)
    
    // Par défaut on affiche TOUT (categoryId = -1)
    private val _baseRecipes = _categoryId.switchMap { catId ->
        if (catId == -1L) {
            repository.allRecipes
        } else {
            repository.getRecipesByCategory(catId)
        }
    }

    val recipes: LiveData<List<Recipe>> = MediatorLiveData<List<Recipe>>().apply {
        fun update() {
            val query = _searchQuery.value ?: ""
            val list = _baseRecipes.value ?: emptyList()
            
            value = if (query.isBlank()) {
                list
            } else {
                list.filter { it.title.contains(query, ignoreCase = true) || 
                              it.instructions?.contains(query, ignoreCase = true) == true }
            }
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
}
