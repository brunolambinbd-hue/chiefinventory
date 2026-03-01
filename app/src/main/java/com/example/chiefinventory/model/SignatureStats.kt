package com.example.chiefinventory.model

/**
 * A data class that holds aggregated statistics about the state of image signatures in the collection.
 *
 * This is used by the dashboard and the signature report to provide an overview of data quality.
 *
 * @property totalCount The total number of items in the collection.
 * @property validCount The number of items that have a valid, non-empty image signature.
 * @property emptyCount The number of items that have an empty (but not null) image signature.
 * @property missingCount The number of items that have a null image signature.
 */
data class SignatureStats(
    val totalCount: Int,
    val validCount: Int,
    val emptyCount: Int,
    val missingCount: Int
)
