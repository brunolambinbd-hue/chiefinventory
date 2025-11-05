package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parabdcollector.repo.CollectionRepository

class ViewModelFactory(private val application: Application, private val repository: CollectionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MainViewModel(application, repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ImportViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ImportViewModel(application, repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}