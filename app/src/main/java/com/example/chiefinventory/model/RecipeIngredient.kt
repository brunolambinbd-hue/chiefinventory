package com.example.chiefinventory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an ingredient linked to a specific recipe.
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["locationId"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val ingredientName: String,
    val quantityRequired: Double? = null,
    val unit: String? = null,
    val supplementalInfo: String? = null,
    val locationId: Long? = null // Nouveau champ : Emplacement de l'ingrédient (Frigo, Placard, etc.)
)
