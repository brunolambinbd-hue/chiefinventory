package com.example.parabdcollector.dao

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
)
