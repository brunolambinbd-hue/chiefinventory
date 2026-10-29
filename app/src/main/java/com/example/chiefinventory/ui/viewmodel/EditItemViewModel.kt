package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.model.DisplayLocation
import com.example.imagecomparison.EmbeddingUtils
import com.example.imagecomparison.ImageEmbedderHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SaveOperation {
    INSERT,
    UPDATE
}

/**
 * ViewModel for the item editing screen ([com.example.chiefinventory.ui.actvity.EditItemActivity]).
 *
 * This class manages the state for both creating a new item and editing an existing one.
 * It interacts with both [CollectionRepository] and [LocationRepository].
 *
 * @param application The application instance.
 * @param collectionRepository The repository for collection item data.
 * @param locationRepository The repository for location data.
 */
class EditItemViewModel(
    application: Application,
    private val collectionRepository: CollectionRepository,
    locationRepository: LocationRepository
) : AndroidViewModel(application) {

    private val _item = MediatorLiveData<CollectionItem>()
    /** The collection item currently being edited. */
    val item: LiveData<CollectionItem> = _item

    private var currentItemSource: LiveData<CollectionItem>? = null

    private val _imageUri = MutableLiveData<Uri?>()
    /** The URI of the new image taken or selected by the user. */
    val imageUri: LiveData<Uri?> = _imageUri

    private var _imageEmbedderHelper: ImageEmbedderHelper? = null
    private fun getImageEmbedderHelper(): ImageEmbedderHelper {
        if (_imageEmbedderHelper == null) {
            _imageEmbedderHelper = ImageEmbedderHelper(getApplication(), null)
        }
        return _imageEmbedderHelper!!
    }

    /** A flat list of all locations, decorated with their depth for indented display. */
    val displayLocations: LiveData<List<DisplayLocation>> = locationRepository.getAll().map {
        buildDisplayList(it)
    }

    /**
     * Loads an item from the repository by its ID.
     * @param id The ID of the item to load.
     */
    fun loadItem(id: Long) {
        val oldSource = currentItemSource
        if (oldSource != null) {
            _item.removeSource(oldSource)
        }

        val newSource = collectionRepository.getById(id)
        _item.addSource(newSource) { fetchedItem ->
            _item.value = fetchedItem
        }
        currentItemSource = newSource
    }

    /**
     * Sets the new image URI.
     * @param uri The URI of the newly captured image.
     */
    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    /**
     * Calculates the image signature for a given bitmap.
     * @param bitmap The bitmap of the image.
     * @param dispatcher The coroutine dispatcher to use. Defaults to IO.
     * @return A [ByteArray] representing the signature, or null if calculation fails.
     */
    suspend fun calculateSignature(bitmap: Bitmap, dispatcher: CoroutineDispatcher = Dispatchers.IO): ByteArray? = withContext(dispatcher) {
        val signature = getImageEmbedderHelper().computeSignature(bitmap)
        if (signature != null) {
            EmbeddingUtils.embeddingToByteArray(signature)
        } else {
            null
        }
    }

    /**
     * Saves the given item to the database, either by inserting or updating it.
     * @param item The [CollectionItem] to save.
     */
    @Suppress("unused")
    fun saveItem(item: CollectionItem): Job = viewModelScope.launch {
        val operation = determineSaveOperation(item.id)
        if (operation == SaveOperation.INSERT) {
            collectionRepository.insert(item)
        } else {
            collectionRepository.update(item)
        }
    }

    /**
     * Inserts a new item into the database.
     * @param item The [CollectionItem] to insert.
     */
    fun insert(item: CollectionItem): Job = viewModelScope.launch {
        collectionRepository.insert(item)
    }

    /**
     * Updates an existing item in the database.
     * @param item The [CollectionItem] to update.
     */
    fun update(item: CollectionItem): Job = viewModelScope.launch {
        collectionRepository.update(item)
    }

    override fun onCleared() {
        super.onCleared()
        val helper = _imageEmbedderHelper
        if (helper != null) {
            helper.clearImageEmbedder()
        }
    }

    companion object {
        fun determineSaveOperation(itemId: Long): SaveOperation {
            if (itemId == 0L) {
                return SaveOperation.INSERT
            } else {
                return SaveOperation.UPDATE
            }
        }

        fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
            val displayList = mutableListOf<DisplayLocation>()
            val locationsByParent = locations.groupBy { it.parentId }

            fun addChildren(parentId: Long?, depth: Int) {
                val children = locationsByParent[parentId]
                if (children != null) {
                    children.sortedBy { it.name }.forEach { location ->
                        displayList.add(DisplayLocation(location, depth))
                        addChildren(location.id, depth + 1)
                    }
                }
            }

            addChildren(null, 0)
            return displayList
        }
    }
}
