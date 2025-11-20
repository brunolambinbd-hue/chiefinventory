package com.example.parabdcollector.model

/**
 * Un conteneur qui encapsule un objet de la collection et son score de similarité
 * lors d'une recherche par image.
 */
data class SearchResultItem(
    val item: CollectionItem,
    val similarity: Double? = null
)
