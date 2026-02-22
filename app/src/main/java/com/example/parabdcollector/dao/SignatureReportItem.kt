package com.example.parabdcollector.dao

/**
 * A lightweight version of the signature report item to avoid memory issues.
 */
data class SignatureReportItem(
    val id: Long,
    val titre: String,
    val imageUri: String?,
    /** Boolean indicating if the embedding exists in the database. */
    val hasEmbedding: Boolean
)
