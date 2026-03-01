package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.utils.CategoryMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Category Audit feature.
 * Detects and repairs inconsistent super-categories based on CategoryMapper rules.
 */
class CategoryAuditViewModel(
    private val repository: CollectionRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _auditResult = MutableLiveData<Int?>(null)
    val auditResult: LiveData<Int?> = _auditResult

    private val _updateStatus = MutableLiveData<Int?>(null)
    val updateStatus: LiveData<Int?> = _updateStatus

    private var itemsToFix: List<CollectionItem> = emptyList()

    /**
     * Scans the database to find items whose current super-category doesn't match the Mapper rules.
     * Targets items with missing or placeholder super-categories (N/D, #N/D, Non Défini).
     */
    fun performAudit() {
        viewModelScope.launch {
            // Utilisation de la fonction de récupération totale pour ne rien rater
            val allItems = repository.getAllItemsSuspend()
            
            itemsToFix = withContext(ioDispatcher) {
                allItems.filter { item ->
                    val currentSuper = item.superCategorie?.trim() ?: ""
                    val rawCategory = item.categorie?.trim() ?: ""
                    val shouldBeSuper = CategoryMapper.getSuperCategoryFor(rawCategory)
                    
                    // Détection incluant la variante Excel "#N/D"
                    val isPlaceholder = currentSuper.isBlank() || 
                                       currentSuper.equals("N/D", ignoreCase = true) || 
                                       currentSuper.equals("#N/D", ignoreCase = true) || 
                                       currentSuper.equals("Non Défini", ignoreCase = true)
                    
                    shouldBeSuper != null && isPlaceholder
                }
            }

            _auditResult.postValue(itemsToFix.size)
        }
    }

    /**
     * Updates the identified items with their correct super-category.
     */
    fun fixInconsistencies() {
        val list = itemsToFix
        if (list.isEmpty()) return

        viewModelScope.launch {
            withContext(ioDispatcher) {
                list.forEach { item ->
                    val rawCategory = item.categorie?.trim() ?: ""
                    val correctSuper = CategoryMapper.getSuperCategoryFor(rawCategory)
                    if (correctSuper != null) {
                        repository.update(item.copy(superCategorie = correctSuper))
                    }
                }
            }
            _updateStatus.postValue(list.size)
            _auditResult.postValue(null)
        }
    }
}
