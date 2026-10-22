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
    val restingTimeMinutes: Int? = null,
    val servings: Int? = null,
    val kcalPerServing: Int? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val imageUri: String? = null,
    val locationId: Long? = null,
    val wineRecommendation: String? = null,
    val source: String? = null,
    val nutritionInfo: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
