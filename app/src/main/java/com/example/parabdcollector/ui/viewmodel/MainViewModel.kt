package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.model.CategoryInfo

/**
 * The main ViewModel for the application's primary screens ([MainActivity], [ItemListActivity], etc.).
 *
 * This ViewModel acts as an intermediary between the UI controllers and the [CollectionRepository],
 * exposing various data streams as [LiveData] objects for the UI to observe.
 *
 * @property repository The single source of truth for collection data.
 */
class MainViewModel(private val repository: CollectionRepository) : ViewModel() {

    /** A LiveData list of all items the user possesses. */
    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()

    /** A LiveData list of all items the user is seeking. */
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()

    /** A LiveData object holding the total number of items in the collection. */
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    /** A LiveData object holding statistics about the quality of image signatures. */
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    /**
     * Returns statistical information about super-categories for either possessed or sought items.
     * @param isPossessed True to get stats for possessed items, false for sought items.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getSuperCategoryInfo(isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getSuperCategoryInfo(isPossessed)
    }

    /**
     * Returns statistical information about detailed categories within a given super-category.
     * @param superCategory The name of the super-category to filter by.
     * @param isPossessed True to get stats for possessed items, false for sought items.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getCategoryInfoForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getCategoryInfoForSuperCategory(superCategory, isPossessed)
    }

    /**
     * Returns a list of items belonging to a specific category and super-category.
     * @param superCategory The name of the super-category.
     * @param category The name of the detailed category.
     * @param isPossessed True to get possessed items, false for sought items.
     * @return A [LiveData] list of matching [CollectionItem]s.
     */
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return repository.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }

    /**
     * Returns a list of items belonging to a specific location.
     * @param locationId The ID of the location.
     * @return A [LiveData] list of matching [CollectionItem]s.
     */
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>> {
        return repository.getItemsByLocationId(locationId)
    }
}
