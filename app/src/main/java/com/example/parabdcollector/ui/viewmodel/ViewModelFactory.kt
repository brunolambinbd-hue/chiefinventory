package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository

/**
 * A centralized factory for creating all ViewModel instances in the application.
 *
 * This class allows for the injection of dependencies (like repositories) into the ViewModels,
 * which is crucial for both the app's architecture and for testing.
 *
 * @param application The application instance, required for ViewModels that need a context.
 * @param collectionRepository The repository for collection item data.
 * @param locationRepository The repository for location data.
 */
class ViewModelFactory(
    private val application: Application,
    private val collectionRepository: CollectionRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    
    /**
     * Creates a new instance of the given [ViewModel] class.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if the provided modelClass is unknown.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(collectionRepository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(application, collectionRepository) as T
            modelClass.isAssignableFrom(ImportViewModel::class.java) ->
                ImportViewModel(application, collectionRepository) as T
            modelClass.isAssignableFrom(SignatureReportViewModel::class.java) ->
                SignatureReportViewModel(collectionRepository) as T
            modelClass.isAssignableFrom(LocationViewModel::class.java) ->
                LocationViewModel(locationRepository, collectionRepository) as T
            modelClass.isAssignableFrom(EditItemViewModel::class.java) ->
                EditItemViewModel(application, collectionRepository, locationRepository) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) ->
                InventoryViewModel(application, collectionRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
