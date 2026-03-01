package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ViewModelFactory(
    private val application: Application,
    private val collectionRepository: CollectionRepository,
    private val locationRepository: LocationRepository,
    private val ingredientRepository: IngredientRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(collectionRepository, ingredientRepository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(application, collectionRepository) as T
            modelClass.isAssignableFrom(ImportViewModel::class.java) -> ImportViewModel(application, collectionRepository) as T
            modelClass.isAssignableFrom(SignatureReportViewModel::class.java) -> SignatureReportViewModel(collectionRepository) as T
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> LocationViewModel(locationRepository) as T
            modelClass.isAssignableFrom(EditItemViewModel::class.java) -> EditItemViewModel(application, collectionRepository, locationRepository) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(application, collectionRepository) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> BackupViewModel(application) as T
            modelClass.isAssignableFrom(BatchPossessionViewModel::class.java) -> BatchPossessionViewModel(collectionRepository, locationRepository) as T
            modelClass.isAssignableFrom(CategoryAuditViewModel::class.java) -> CategoryAuditViewModel(collectionRepository, ioDispatcher) as T
            modelClass.isAssignableFrom(IngredientViewModel::class.java) -> IngredientViewModel(ingredientRepository) as T
            modelClass.isAssignableFrom(EditIngredientViewModel::class.java) -> EditIngredientViewModel(application, ingredientRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
