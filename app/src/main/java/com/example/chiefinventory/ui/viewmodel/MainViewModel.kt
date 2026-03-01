package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.ui.model.CategoryInfo

/**
 * ViewModel for the main dashboard.
 */
class MainViewModel(
    private val repository: CollectionRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    /** Collection stats */
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()
    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()
    val recentPossessedItems: LiveData<List<CollectionItem>> = repository.getRecentPossessed()
    val recentLocatedItems: LiveData<List<CollectionItem>> = repository.getRecentLocated()

    /** Ingredient stats */
    val allIngredients: LiveData<List<Ingredient>> = ingredientRepository.getAll()

    val unlocatedItems: LiveData<List<CollectionItem>> = repository.getUnlocatedItems()
    val locatedNotPossessedItems: LiveData<List<CollectionItem>> = repository.getLocatedNotPossessedItems()
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    fun getSuperCategoryInfo(isSoughtMode: Boolean): LiveData<List<CategoryInfo>> = repository.getSuperCategoryInfo(isSoughtMode)
    fun getCategoryInfoForSuperCategory(superCategory: String, isSoughtMode: Boolean): LiveData<List<CategoryInfo>> = repository.getCategoryInfoForSuperCategory(superCategory, isSoughtMode)
    fun getItemsBySuperCategoryAndCategory(s: String, c: String, p: Boolean): LiveData<List<CollectionItem>> = repository.getItemsBySuperCategoryAndCategory(s, c, p)
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>> = repository.getItemsByLocationId(locationId)
}
