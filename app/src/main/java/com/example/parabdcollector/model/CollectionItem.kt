package com.example.parabdcollector.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a single item in the user's collection.
 * This is the core data model for the application, used as a Room database entity and for UI display.
 *
 * @property id The unique primary key for the item in the local database.
 * @property remoteId An optional ID from an external data source, used for synchronization.
 * @property titre The main title of the item. This is a mandatory field.
 * @property editeur The publisher or editor of the item.
 * @property annee The year of publication or creation.
 * @property mois The month of publication or creation.
 * @property categorie The specific category of the item (e.g., "Affiches", "Albums collectifs").
 * @property superCategorie The standardized, broader category (e.g., "Image", "Album"). See [com.example.parabdcollector.utils.CategoryMapper].
 * @property materiau The material the item is made of (e.g., "Papier Velin").
 * @property tirage The print run or edition size (e.g., "500 ex.").
 * @property dimensions The physical dimensions of the item (e.g., "50x70cm").
 * @property prixAchat The price paid by the user for the item.
 * @property valeurEstimee The estimated current market value of the item.
 * @property lieuAchat The location where the item was purchased.
 * @property description A free-form text field for user notes and additional details.
 * @property imageUri The URI of the item's image, which can be a local content URI or a remote web URL.
 * @property imageEmbedding The computed image signature (embedding) as a ByteArray, used for similarity searches.
 * @property locationId The foreign key referencing the [Location] where this item is stored.
 * @property isPossessed A boolean flag indicating whether the user owns this item (true) or is seeking it (false).
 */
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
    val locationId: Long? = null,
    val isPossessed: Boolean = true
) : Parcelable {
    // We must override equals() and hashCode() because of the ByteArray property.
    // The default data class implementation would perform a referential equality check on the array.
    @Suppress("CognitiveComplexity")
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
        if (locationId != other.locationId) return false
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
        result = 31 * result + (locationId?.hashCode() ?: 0)
        result = 31 * result + isPossessed.hashCode()
        return result
    }
}
