package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

data class SearchCriteria(
    val titre: String? = null,
    val editeur: String? = null,
    val annee: Int? = null,
    val mois: Int? = null,
    val categorie: String? = null
)

class SearchViewModel(private val repository: CollectionRepository) : ViewModel() {

    fun search(query: String): LiveData<List<CollectionItem>> {
        return repository.search("%${query}%")
    }

    fun advancedSearch(criteria: SearchCriteria): LiveData<List<CollectionItem>> {
        return repository.advancedSearch(
            titre = criteria.titre?.let { "%$it%" },
            editeur = criteria.editeur?.let { "%$it%" },
            annee = criteria.annee,
            mois = criteria.mois,
            categorie = criteria.categorie?.let { "%$it%" }
        )
    }
}