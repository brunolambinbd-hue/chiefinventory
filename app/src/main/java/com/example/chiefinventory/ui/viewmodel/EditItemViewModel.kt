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
import com.example.imagecomparison.EmbeddingUtils
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.model.DisplayLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = null
    )

    /** A flat list of all locations, decorated with their depth for indented display. */
    val displayLocations: LiveData<List<DisplayLocation>> = locationRepository.getAll().map {
        buildDisplayList(it)
    }

    /**
     * Recursively builds a flat list of [DisplayLocation]s from a hierarchical list of [Location]s.
     * @param locations The complete list of locations from the database.
     * @return A list of [DisplayLocation]s, ordered and with depth information.
     */
    private fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
        val displayList = mutableListOf<DisplayLocation>()
        val locationsByParent = locations.groupBy { it.parentId }

        fun addChildren(parentId: Long?, depth: Int) {
            locationsByParent[parentId]?.sortedBy { it.name }?.forEach { location ->
                displayList.add(DisplayLocation(location, depth))
                addChildren(location.id, depth + 1)
            }
        }

        addChildren(null, 0) // Start with root elements
        return displayList
    }

    /**
     * Loads an item from the repository by its ID.
     * @param id The ID of the item to load.
     */
    fun loadItem(id: Long) {
        currentItemSource?.let { _item.removeSource(it) }
        val newSource = collectionRepository.getById(id)
        _item.addSource(newSource) {
            _item.value = it
        }
        currentItemSource = newSource
    }

    /**
     * Sets the new image URI.
     * @param uri The URI of the newly captured image.
     */
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    /**
     * Calculates the image signature for a given bitmap.
     * @param bitmap The bitmap of the image.
     * @param dispatcher The coroutine dispatcher to use. Defaults to IO.
     * @return A [ByteArray] representing the signature, or null if calculation fails.
     */
    suspend fun calculateSignature(bitmap: Bitmap, dispatcher: CoroutineDispatcher = Dispatchers.IO): ByteArray? = withContext(dispatcher) {
        val signature = imageEmbedderHelper.computeSignature(bitmap)
        signature?.let { EmbeddingUtils.embeddingToByteArray(it) }
    }

    /**
     * Saves the given item to the database, either by inserting or updating it.
     * @param item The [CollectionItem] to save.
     */
    @Suppress("unused")
    fun saveItem(item: CollectionItem): Job = viewModelScope.launch {
        if (item.id == 0L) {
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
}
