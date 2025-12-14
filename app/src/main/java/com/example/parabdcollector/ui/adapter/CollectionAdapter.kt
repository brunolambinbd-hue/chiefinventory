package com.example.parabdcollector.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCollectionBinding
import com.example.parabdcollector.ui.model.SearchResultItem

/**
 * A versatile RecyclerView adapter for displaying a list of [SearchResultItem]s.
 *
 * This adapter is used in multiple screens (search results, category item lists) to display
 * the details of a collection item. It handles the visibility of each field based on whether
 * the data is present and also displays an optional similarity score.
 *
 * @param onItemClicked A lambda function invoked when an item in the list is clicked.
 */
class CollectionAdapter(private val onItemClicked: (SearchResultItem) -> Unit) : ListAdapter<SearchResultItem, CollectionAdapter.CollectionViewHolder>(DiffCallback) {

    /**
     * Creates and returns a new [CollectionViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionViewHolder(binding)
    }

    /**
     * Binds the data at the specified position to the [CollectionViewHolder].
     */
    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current, position)
    }

    /**
     * ViewHolder for a single collection item, handling the data binding logic.
     * @param binding The view binding for the item layout.
     */
    class CollectionViewHolder(private val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds a [SearchResultItem] to the views in the layout.
         * This function sets the text for each field and manages its visibility, hiding fields that are null or blank.
         * @param searchResult The data to display.
         */
        fun bind(searchResult: SearchResultItem, position: Int) {
            val item = searchResult.item
            val context = binding.root.context

            // Apply zebra striping for better readability
            if (position % 2 == 0) {
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.zebra_stripe_color))
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            binding.itemName.text = item.titre

            // Display remoteId if available
            if (item.remoteId != null) {
                binding.itemRemoteId.text = context.getString(R.string.report_item_id, item.remoteId)
                binding.itemRemoteId.visibility = View.VISIBLE
            } else {
                binding.itemRemoteId.visibility = View.GONE
            }

            binding.itemImage.load(item.imageUri?.toUri()) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher)
                error(R.mipmap.ic_launcher)
            }

            // Handle visibility for all optional text fields
            binding.itemNotes.text = item.description
            binding.itemNotes.visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.itemEditeur.text = item.editeur
            binding.itemEditeur.visibility = if (item.editeur.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.itemYear.text = item.annee?.toString()
            binding.itemYear.visibility = if (item.annee == null) View.GONE else View.VISIBLE

            val categoryHierarchy = listOfNotNull(item.superCategorie, item.categorie).joinToString(" > ")
            binding.itemCategoryHierarchy.text = categoryHierarchy
            binding.itemCategoryHierarchy.visibility = if (categoryHierarchy.isBlank()) View.GONE else View.VISIBLE
            
            binding.itemMaterial.text = item.materiau
            binding.itemMaterial.visibility = if (item.materiau.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.itemDimensions.text = item.dimensions
            binding.itemDimensions.visibility = if (item.dimensions.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.itemTirage.text = item.tirage
            binding.itemTirage.visibility = if (item.tirage.isNullOrBlank()) View.GONE else View.VISIBLE

            // Display similarity score if available (for image search results)
            if (searchResult.similarity != null) {
                binding.itemSimilarity.text = context.getString(R.string.similarity_score_format, searchResult.similarity * 100)
                binding.itemSimilarity.visibility = View.VISIBLE
            } else {
                binding.itemSimilarity.visibility = View.GONE
            }
        }
    }

    companion object {
        /**
         * A DiffUtil.ItemCallback for calculating the difference between two [SearchResultItem]s.
         */
        private val DiffCallback = object : DiffUtil.ItemCallback<SearchResultItem>() {
            override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem == newItem
        }
    }
}
