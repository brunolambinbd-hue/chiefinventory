package com.example.chiefinventory.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a cooking recipe.
 */
@Parcelize
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val instructions: String? = null,
    val preparationTimeMinutes: Int? = null,
    val cookingTimeMinutes: Int? = null,
    val restingTimeMinutes: Int? = null, // Nouveau champ : Temps de repos en minutes
    val servings: Int? = null,
    val category: String? = null,
    val imageUri: String? = null,
    val locationId: Long? = null,
    val wineRecommendation: String? = null,
    val source: String? = null, // New field for origin (hotel, website, book)
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
