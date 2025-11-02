package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

class SoughtViewModel(application: Application, repository: CollectionRepository) : AndroidViewModel(application) {

    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()

}