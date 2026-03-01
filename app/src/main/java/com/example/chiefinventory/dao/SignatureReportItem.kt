package com.example.chiefinventory.dao

import androidx.room.ColumnInfo

/**
 * A simple data class to hold the result of a query for the signature report.
 */
data class SignatureReportItem(
    val id: Long,
    val titre: String,
    val imageUri: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageEmbedding: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignatureReportItem

        if (id != other.id) return false
        if (titre != other.titre) return false
        if (imageUri != other.imageUri) return false
        if (imageEmbedding != null) {
            if (other.imageEmbedding == null) return false
            if (!imageEmbedding.contentEquals(other.imageEmbedding)) return false
        } else if (other.imageEmbedding != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + titre.hashCode()
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + (imageEmbedding?.contentHashCode() ?: 0)
        return result
    }
}
