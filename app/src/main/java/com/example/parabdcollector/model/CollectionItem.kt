package com.example.parabdcollector.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "collection_items", indices = [Index(value = ["remoteId"], unique = true)])
data class CollectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Int? = null,
    val titre: String,
    val editeur: String?,
    val annee: Int?,
    val mois: Int?,
    val categorie: String?,
    val superCategorie: String?,
    val materiau: String?,
    val tirage: String?,
    val dimensions: String?,
    val prixAchat: Double?,
    val valeurEstimee: Double?,
    val lieuAchat: String?,
    val description: String?,
    val imageUri: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageEmbedding: ByteArray? = null,
    val localisation: String?,
    val isPossessed: Boolean = true 
) : Parcelable {
    // On doit surcharger equals et hashCode à cause du ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CollectionItem

        if (id != other.id) return false
        if (remoteId != other.remoteId) return false
        if (titre != other.titre) return false
        if (editeur != other.editeur) return false
        if (annee != other.annee) return false
        if (mois != other.mois) return false
        if (categorie != other.categorie) return false
        if (superCategorie != other.superCategorie) return false
        if (materiau != other.materiau) return false
        if (tirage != other.tirage) return false
        if (dimensions != other.dimensions) return false
        if (prixAchat != other.prixAchat) return false
        if (valeurEstimee != other.valeurEstimee) return false
        if (lieuAchat != other.lieuAchat) return false
        if (description != other.description) return false
        if (imageUri != other.imageUri) return false
        if (imageEmbedding != null) {
            if (other.imageEmbedding == null) return false
            if (!imageEmbedding.contentEquals(other.imageEmbedding)) return false
        } else if (other.imageEmbedding != null) return false
        if (localisation != other.localisation) return false
        if (isPossessed != other.isPossessed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (remoteId ?: 0)
        result = 31 * result + titre.hashCode()
        result = 31 * result + (editeur?.hashCode() ?: 0)
        result = 31 * result + (annee ?: 0)
        result = 31 * result + (mois ?: 0)
        result = 31 * result + (categorie?.hashCode() ?: 0)
        result = 31 * result + (superCategorie?.hashCode() ?: 0)
        result = 31 * result + (materiau?.hashCode() ?: 0)
        result = 31 * result + (tirage?.hashCode() ?: 0)
        result = 31 * result + (dimensions?.hashCode() ?: 0)
        result = 31 * result + (prixAchat?.hashCode() ?: 0)
        result = 31 * result + (valeurEstimee?.hashCode() ?: 0)
        result = 31 * result + (lieuAchat?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + (imageEmbedding?.contentHashCode() ?: 0)
        result = 31 * result + (localisation?.hashCode() ?: 0)
        result = 31 * result + isPossessed.hashCode()
        return result
    }
}
