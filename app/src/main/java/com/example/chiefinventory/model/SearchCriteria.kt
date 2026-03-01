package com.example.chiefinventory.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchCriteria(
    val titre: String? = null,
    val superCategorie: String? = null,
    val categorie: String? = null,
    val editeur: String? = null,
    val annee: Int? = null,
    val mois: Int? = null,
    val description: String? = null,
    val tirage: String? = null,
    val dimensions: String? = null,
    val isPossessed: Boolean? = null
) : Parcelable
