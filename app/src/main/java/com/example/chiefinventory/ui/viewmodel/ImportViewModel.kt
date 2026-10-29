package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling the data import process from a CSV file.
 */
class ImportViewModel(
    application: Application
) : AndroidViewModel(application) {

    fun importCsv(uri: Uri, dispatcher: CoroutineDispatcher = Dispatchers.IO): Job {
        return viewModelScope.launch(dispatcher) {
            // Import logic for general inventory
        }
    }
}
