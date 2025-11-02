package com.example.parabdcollector.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "collection_items")
data class CollectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val univers: String?,
    val fabricant: String?,
    val annee: Int?,
    val categorie: String?,
    val materiau: String?,
    val tirage: String?,
    val dimensions: String?,
    val prixAchat: Double?,
    val valeurEstimee: Double?,
    val lieuAchat: String?,
    val notes: String?,
    val imageUri: String?,
    val localisation: String?,
    val isPossessed: Boolean = true // Nouveau champ pour distinguer les objets possédés
)
