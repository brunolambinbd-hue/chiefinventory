package com.example.parabdcollector.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["parentLocationId"],
            onDelete = ForeignKey.CASCADE // Si un parent est supprimé, ses enfants le sont aussi
        )
    ],
    indices = [Index("parentLocationId")]
)
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentLocationId: Long?
) : Parcelable
