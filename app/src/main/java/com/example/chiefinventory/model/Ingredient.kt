package com.example.chiefinventory.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a kitchen ingredient.
 */
@Parcelize
@Entity(tableName = "ingredients", indices = [Index(value = ["locationId"])])
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null, // e.g., "g", "ml", "pcs"
    val category: String? = null, // e.g., "Dairy", "Vegetables"
    val expirationDate: Long? = null,
    val description: String? = null,
    val imageUri: String? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageEmbedding: ByteArray? = null,
    val ocrText: String? = null,
    val locationId: Long? = null, // ID from the locations table (type INGREDIENT)
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
