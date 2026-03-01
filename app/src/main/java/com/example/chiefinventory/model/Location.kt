package com.example.chiefinventory.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Types of locations available in the application.
 */
enum class LocationType {
    COLLECTION, // Legacy items (ParaBD)
    INGREDIENT,
    RECIPE
}

/**
 * Represents a physical or logical location where an item, ingredient or recipe can be stored or classified.
 *
 * @property id The unique identifier for the location.
 * @property name The name of the location (e.g., "Fridge", "Bakery shelf").
 * @property parentId The ID of the parent location, if this is a sub-location.
 * @property type The category of this location (Ingredient vs Recipe).
 */
@Entity(tableName = "locations",
    indices = [Index(value = ["parentId", "type"])]
)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentId: Long?,
    val type: LocationType = LocationType.COLLECTION
)
