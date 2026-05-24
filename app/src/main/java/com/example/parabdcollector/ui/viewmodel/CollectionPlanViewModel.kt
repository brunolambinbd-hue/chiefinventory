package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.dao.FullHierarchyItem
import com.example.parabdcollector.repo.CollectionRepository

/**
 * ViewModel for the Collection Plan screen.
 */
class CollectionPlanViewModel(repository: CollectionRepository) : ViewModel() {
    val hierarchy: LiveData<List<FullHierarchyItem>> = repository.getFullHierarchy()
}
