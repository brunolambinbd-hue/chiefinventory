package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository

class ViewModelFactory(
    private val application: Application,
    private val collectionRepository: CollectionRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
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
                LocationViewModel(locationRepository) as T
            modelClass.isAssignableFrom(EditItemViewModel::class.java) ->
                EditItemViewModel(application, collectionRepository, locationRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
