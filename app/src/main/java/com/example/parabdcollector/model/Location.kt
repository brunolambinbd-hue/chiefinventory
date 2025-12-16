package com.example.parabdcollector.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a physical or logical location where a collection item can be stored.
 *
 * @property id The unique identifier for the location.
 * @property name The name of the location (e.g., "Living Room Shelf", "Box A").
 * @property parentId The ID of the parent location, if this is a sub-location. Null for top-level locations.
 */
@Entity(tableName = "locations",
    indices = [Index(value = ["parentId"])]
)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentId: Long?
)
