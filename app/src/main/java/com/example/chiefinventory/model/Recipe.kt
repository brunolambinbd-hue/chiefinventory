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
    val servings: Int? = null,
    val category: String? = null, // e.g., "Dessert", "Main Course"
    val imageUri: String? = null,
    val locationId: Long? = null, // Where this recipe is classified
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Cross-reference entity to link Recipes and Ingredients.
 * This allows a many-to-many relationship (though often simplified to many-to-one in home apps, 
 * many-to-many is more flexible for "Recipe uses Ingredient X").
 */
@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientName"] // Using name because a recipe might use an ingredient you don't have in stock yet
)
data class RecipeIngredient(
    val recipeId: Long,
    val ingredientName: String,
    val quantityRequired: Double? = null,
    val unit: String? = null
)
