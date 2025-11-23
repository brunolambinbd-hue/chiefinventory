package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EditItemViewModel(
    application: Application,
    private val collectionRepository: CollectionRepository,
    private val locationRepository: LocationRepository
) : AndroidViewModel(application) {

    private val _item = MediatorLiveData<CollectionItem>()
    val item: LiveData<CollectionItem> = _item

    private var currentItemSource: LiveData<CollectionItem>? = null

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = null
    )

    // Logique pour l'affichage hiérarchique des emplacements
    private val allLocations: LiveData<List<Location>> = locationRepository.getAll()
    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map {
        buildDisplayList(it)
    }

    private fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
        val displayList = mutableListOf<DisplayLocation>()
        val locationsByParent = locations.groupBy { it.parentLocationId }

        fun addChildren(parentId: Long?, depth: Int) {
            locationsByParent[parentId]?.sortedBy { it.name }?.forEach { location ->
                displayList.add(DisplayLocation(location, depth))
                addChildren(location.id, depth + 1)
            }
        }

        addChildren(null, 0) // On commence par les éléments racines
        return displayList
    }

    fun loadItem(id: Long) {
        currentItemSource?.let { _item.removeSource(it) }
        val newSource = collectionRepository.getById(id)
        _item.addSource(newSource) {
            _item.value = it
        }
        currentItemSource = newSource
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    suspend fun calculateSignature(bitmap: Bitmap): ByteArray? {
        val signature = imageEmbedderHelper.computeSignature(bitmap)
        return signature?.let {
            val floatArray = it.floatEmbedding()
            val byteBuffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            floatArray.forEach { value -> byteBuffer.putFloat(value) }
            byteBuffer.array()
        }
    }

    fun insert(item: CollectionItem) = viewModelScope.launch {
        collectionRepository.insert(item)
    }

    fun update(item: CollectionItem) = viewModelScope.launch {
        collectionRepository.update(item)
    }
}
