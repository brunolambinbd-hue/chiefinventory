package com.example.chiefinventory.ui.model

import android.os.Parcelable
import com.example.chiefinventory.model.CollectionItem
import kotlinx.parcelize.Parcelize

/**
 * A view-specific data class that represents an item in a search result list.
 *
 * It wraps the core [CollectionItem] entity with an optional similarity score, which is populated
 * only when performing an image-based search. This allows the UI to display both the item data
 * and its relevance to the image query.
 *
 * @property item The original [CollectionItem] from the search query.
 * @property similarity The cosine similarity score (between 0.0 and 1.0) of the item's image
 *                      compared to the search image. This is null for text-only searches.
 */
@Parcelize
data class SearchResultItem(
    val item: CollectionItem,
    val similarity: Double? = null
) : Parcelable
