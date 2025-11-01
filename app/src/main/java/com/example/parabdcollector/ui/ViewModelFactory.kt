package com.example.parabdcollector.ui

// Fichier : ViewModelFactory.kt (ou à l'intérieur de MainViewModel.kt)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parabdcollector.repo.CollectionRepository

class ViewModelFactory(private val repository: CollectionRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Vérifie si la classe demandée est bien MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Si oui, crée une instance en lui passant le repository
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        // Sinon, lève une exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
