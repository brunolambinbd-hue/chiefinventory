package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the main dashboard screen ([com.example.parabdcollector.ui.actvity.MainActivity]).
 *
 * This ViewModel provides various LiveData streams to the UI, exposing different slices of the
 * collection data such as possessed items, sought items, and overall statistics.
 *
 * @property repository The [CollectionRepository] from which to get the data.
 */
class MainViewModel(private val repository: CollectionRepository) : ViewModel() {

    /** A list of all items the user possesses. */
    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()

    /** A list of all items the user is seeking. */
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()

    /** The total count of all items in the collection. */
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    /** Live statistics about the state of image signatures in the collection. */
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    /**
     * Inserts a new item into the collection.
     * @param item The [CollectionItem] to insert.
     */
    fun insert(item: CollectionItem) = viewModelScope.launch {
        repository.insert(item)
    }

    /**
     * Updates an existing item in the collection.
     * @param item The [CollectionItem] to update.
     */
    fun update(item: CollectionItem) = viewModelScope.launch {
        repository.update(item)
    }

    /**
     * Gets statistical information (name and count) for each super-category.
     * @param isPossessed True to get stats for possessed items, false for sought items.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getSuperCategoryInfo(isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getSuperCategoryInfo(isPossessed)
    }

    /**
     * Gets statistical information for detailed categories within a given super-category.
     * @param superCategory The name of the super-category to filter by.
     * @param isPossessed True to get stats for possessed items, false for sought items.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getCategoryInfoForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getCategoryInfoForSuperCategory(superCategory, isPossessed)
    }

    /**
     * Gets all items belonging to a specific category and super-category.
     * @param superCategory The name of the super-category.
     * @param category The name of the detailed category.
     * @param isPossessed True to select possessed items, false for sought items.
     * @return A [LiveData] list of matching [CollectionItem]s.
     */
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return repository.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }
}
