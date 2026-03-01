package com.example.chiefinventory.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a single item in the user's collection.
 */
@Parcelize
@Entity(tableName = "collection_items", indices = [Index(value = ["remoteId"], unique = true)])
data class CollectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Int? = null,
    val titre: String,
    val editeur: String? = null,
    val annee: Int? = null,
    val mois: Int? = null,
    val categorie: String? = null,
    val superCategorie: String? = null,
    val materiau: String? = null,
    val tirage: String? = null,
    val dimensions: String? = null,
    val prixAchat: Double? = null,
    val valeurEstimee: Double? = null,
    val lieuAchat: String? = null,
    val description: String? = null,
    val imageUri: String? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageEmbedding: ByteArray? = null,
    val ocrText: String? = null,
    val locationId: Long? = null,
    val isPossessed: Boolean = true,
    /** Timestamp of the last time this item was updated or created. */
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CollectionItem
        if (id != other.id) return false
        if (titre != other.titre) return false
        if (imageEmbedding != null) {
            if (other.imageEmbedding == null) return false
            if (!imageEmbedding.contentEquals(other.imageEmbedding)) return false
        } else if (other.imageEmbedding != null) return false
        if (ocrText != other.ocrText) return false
        if (updatedAt != other.updatedAt) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + titre.hashCode()
        result = 31 * result + (imageEmbedding?.contentHashCode() ?: 0)
        result = 31 * result + (ocrText?.hashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
