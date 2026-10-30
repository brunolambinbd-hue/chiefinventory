package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.LocationRepository
import kotlinx.coroutines.launch

class EditIngredientViewModel(
    application: Application,
    private val repository: IngredientRepository,
    private val locationRepository: LocationRepository
) : AndroidViewModel(application) {

    private val _ingredient = MutableLiveData<Ingredient>()
    val ingredient: LiveData<Ingredient> = _ingredient

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    val ingredientCategories: LiveData<List<Location>> = locationRepository.getAllByType(LocationType.INGREDIENT)

    fun loadIngredient(id: Long) {
        viewModelScope.launch {
            val ingr = repository.getById(id)
            if (ingr != null) {
                _ingredient.value = ingr
                val uriStr = ingr.imageUri
                if (uriStr != null) {
                    _imageUri.value = Uri.parse(uriStr)
                }
            }
        }
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun insert(ingredient: Ingredient) = viewModelScope.launch {
        repository.insert(ingredient)
    }

    fun update(ingredient: Ingredient) = viewModelScope.launch {
        repository.update(ingredient)
    }

    fun delete(ingredient: Ingredient) = viewModelScope.launch {
        repository.delete(ingredient)
    }
}
