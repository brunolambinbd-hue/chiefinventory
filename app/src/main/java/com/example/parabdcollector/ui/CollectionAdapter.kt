package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCollectionBinding
import com.example.parabdcollector.model.SearchResultItem

class CollectionAdapter(private val onItemClicked: (SearchResultItem) -> Unit) : ListAdapter<SearchResultItem, CollectionAdapter.CollectionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current)
    }

    class CollectionViewHolder(private val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResult: SearchResultItem) {
            val item = searchResult.item
            val context = binding.root.context

            binding.itemName.text = item.titre

            // Affichage du remoteId
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

            // Gestion des autres champs avec visibilité
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

            // Affichage de la similarité si elle existe
            if (searchResult.similarity != null) {
                binding.itemSimilarity.text = context.getString(R.string.similarity_score_format, searchResult.similarity * 100)
                binding.itemSimilarity.visibility = View.VISIBLE
            } else {
                binding.itemSimilarity.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SearchResultItem>() {
            override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem == newItem
        }
    }
}
