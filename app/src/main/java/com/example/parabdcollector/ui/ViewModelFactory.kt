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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(collectionRepository) as T
        }
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(application, collectionRepository) as T
        }
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            return ImportViewModel(application, collectionRepository) as T
        }
        if (modelClass.isAssignableFrom(SignatureReportViewModel::class.java)) {
            return SignatureReportViewModel(collectionRepository) as T
        }
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}