package com.example.parabdcollector.model

/**
 * A lightweight version of [CollectionItem] without the heavy [imageEmbedding] blob.
 * Used for listing items in the UI to prevent CursorWindow memory issues.
 */
data class CollectionItemSummary(
    val id: Long,
    val remoteId: Int?,
    val titre: String,
    val editeur: String?,
    val annee: Int?,
    val mois: Int?,
    val categorie: String?,
    val superCategorie: String?,
    val description: String?,
    val locationId: Long?,
    val isPossessed: Boolean,
    val updatedAt: Long,
    val imageUri: String?,
    val tirage: String? = null
)
