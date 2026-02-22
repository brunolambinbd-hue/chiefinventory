package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.model.CategoryInfo

/**
 * ViewModel for the main dashboard and general collection statistics.
 */
class MainViewModel(private val repository: CollectionRepository) : ViewModel() {

    /** Total count of items in the collection. */
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    /** List of all possessed items. */
    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()

    /** List of all sought (not possessed) items. */
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()

    /** List of all unlocated items (possessed but no location). */
    val unlocatedItems: LiveData<List<CollectionItem>> = repository.getUnlocatedItems()

    /** List of items that have a location but are not possessed. */
    val locatedNotPossessedItems: LiveData<List<CollectionItem>> = repository.getLocatedNotPossessedItems()

    /** The 50 most recently updated possessed items (Recent Finds). */
    val recentPossessedItems: LiveData<List<CollectionItem>> = repository.getRecentPossessed()

    /** The 50 most recently updated items with a location (Recent Organizations). */
    val recentLocatedItems: LiveData<List<CollectionItem>> = repository.getRecentLocated()

    /** Statistics about image signatures in the collection. */
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    /** Gets statistics for super-categories. */
    fun getSuperCategoryInfo(isSoughtMode: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getSuperCategoryInfo(isSoughtMode)
    }

    /** Gets statistics for sub-categories within a super-category. */
    fun getCategoryInfoForSuperCategory(superCategory: String, isSoughtMode: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getCategoryInfoForSuperCategory(superCategory, isSoughtMode)
    }

    /** Gets items by category and possession status. */
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return repository.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }

    /** Gets items for a specific location. */
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>> {
        return repository.getItemsByLocationId(locationId)
    }

    fun getItemsBySession(sessionId: Long): LiveData<List<CollectionItem>> {
        return repository.getItemsBySession(sessionId)
    }
}
