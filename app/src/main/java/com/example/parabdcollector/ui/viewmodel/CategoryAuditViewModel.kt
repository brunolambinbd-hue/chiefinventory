package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.CategoryMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Category Audit feature.
 * Detects and repairs inconsistent super-categories based on CategoryMapper rules.
 */
class CategoryAuditViewModel(private val repository: CollectionRepository) : ViewModel() {

    private val _auditResult = MutableLiveData<Int?>(null)
    val auditResult: LiveData<Int?> = _auditResult

    private val _updateStatus = MutableLiveData<Int?>(null)
    val updateStatus: LiveData<Int?> = _updateStatus

    private var itemsToFix: List<CollectionItem> = emptyList()

    /**
     * Scans the database to find items whose current super-category doesn't match the Mapper rules.
     * Only targets items with missing or placeholder super-categories (N/D).
     */
    fun performAudit() {
        viewModelScope.launch {
            // We use the non-limited search function we added for batch processing
            val allItems = repository.getAllByTitle("") 
            
            itemsToFix = allItems.filter { item ->
                val currentSuper = item.superCategorie
                val shouldBeSuper = item.categorie?.let { CategoryMapper.getSuperCategoryFor(it) }
                
                // Criteria: has a rule in Mapper AND current value is empty or "N/D"
                shouldBeSuper != null && (currentSuper.isNullOrBlank() || currentSuper == "N/D" || currentSuper == "Non Défini")
            }

            _auditResult.value = itemsToFix.size
        }
    }

    /**
     * Updates the identified items with their correct super-category.
     */
    fun fixInconsistencies() {
        val list = itemsToFix
        if (list.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                list.forEach { item ->
                    val correctSuper = item.categorie?.let { CategoryMapper.getSuperCategoryFor(it) }
                    if (correctSuper != null) {
                        repository.update(item.copy(superCategorie = correctSuper))
                    }
                }
            }
            _updateStatus.value = list.size
            _auditResult.value = null // Clear result after success
        }
    }
}
