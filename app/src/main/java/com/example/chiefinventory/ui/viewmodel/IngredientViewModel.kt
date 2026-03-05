package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val ingredients: LiveData<List<Ingredient>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            // Par défaut, on affiche TOUT le stock
            repository.getAll().asFlow()
        } else {
            // Sinon on filtre par le texte saisi
            val dbQuery = "%$query%"
            repository.searchInLocation(dbQuery, -1L).asFlow()
        }
    }.asLiveData()

    fun delete(ingredient: Ingredient) = viewModelScope.launch {
        repository.delete(ingredient)
    }
}
