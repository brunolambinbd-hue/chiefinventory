package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.repo.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the ingredient list screen.
 */
class IngredientViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private var locationId: Long = -1L

    /**
     * Set the current location ID and trigger initial load.
     */
    fun setLocation(id: Long) {
        locationId = id
    }

    /**
     * Update the search query.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Exposes the filtered list of ingredients based on location and search query.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ingredients: LiveData<List<Ingredient>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getByLocation(locationId).asFlow()
        } else {
            // SQL LIKE search needs the % wildcards
            val dbQuery = "%$query%"
            // Use the repository method that calls the DAO with SQL filtering
            repository.searchInLocation(dbQuery, locationId).asFlow()
        }
    }.asLiveData()

    fun delete(ingredient: Ingredient) = viewModelScope.launch {
        repository.delete(ingredient)
    }
}
