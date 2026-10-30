package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the ingredient list screen.
 */
class IngredientViewModel(
    private val repository: IngredientRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private var locationId: Long = -1L

    fun setLocation(id: Long) {
        locationId = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val ingredients: LiveData<List<Ingredient>> = repository.getAll()

    fun delete(ingredient: Ingredient) = viewModelScope.launch {
        repository.delete(ingredient)
    }
}
