package com.example.parabdcollector.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "collection_items", indices = [Index(value = ["remoteId"], unique = true)])
data class CollectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Int? = null,
    val titre: String,
    val editeur: String?,
    val annee: Int?,
    val mois: Int?,
    val categorie: String?,
    val superCategorie: String?,
    val materiau: String?,
    val tirage: String?,
    val dimensions: String?,
    val prixAchat: Double?,
    val valeurEstimee: Double?,
    val lieuAchat: String?,
    val notes: String?,
    val imageUri: String?,
    val localisation: String?,
    val isPossessed: Boolean = true 
)
