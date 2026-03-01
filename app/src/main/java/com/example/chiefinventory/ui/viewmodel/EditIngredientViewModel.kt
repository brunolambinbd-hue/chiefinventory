package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.imagecomparison.ImageEmbedderHelper
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

    private val imageEmbedderHelper = ImageEmbedderHelper(application, null)

    val ingredientCategories: LiveData<List<Location>> = locationRepository.getAllByType(LocationType.INGREDIENT)

    fun loadIngredient(id: Long) {
        viewModelScope.launch {
            repository.getById(id)?.let {
                _ingredient.value = it
                it.imageUri?.let { uri -> _imageUri.value = Uri.parse(uri) }
            }
        }
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun calculateSignature(bitmap: Bitmap): ByteArray? {
        val signature = imageEmbedderHelper.computeSignature(bitmap)
        return signature?.let {
            val floatArray = it.floatEmbedding()
            val byteArray = ByteArray(floatArray.size * 4)
            java.nio.ByteBuffer.wrap(byteArray)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer()
                .put(floatArray)
            byteArray
        }
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

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
