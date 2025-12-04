package com.example.parabdcollector.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a physical or logical location where a collection item can be stored.
 *
 * This entity is designed to form a hierarchical tree structure, allowing for nested locations
 * (e.g., a "Shelf" inside a "Library" which is in the "Office").
 *
 * @property id The unique primary key for the location.
 * @property name The user-defined name of the location (e.g., "Living Room Shelf", "Box #3").
 * @property parentLocationId The foreign key referencing the `id` of the parent location.
 *                            A null value indicates that this is a top-level (root) location.
 */
@Parcelize
@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["parentLocationId"],
            // When a parent location is deleted, all its child locations will be deleted as well.
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentLocationId")]
)
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentLocationId: Long?
) : Parcelable
