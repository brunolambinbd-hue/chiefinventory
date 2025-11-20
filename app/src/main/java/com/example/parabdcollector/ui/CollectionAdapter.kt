package com.example.parabdcollector.ui

import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCollectionBinding
import com.example.parabdcollector.model.SearchResultItem

class CollectionAdapter(
    private val onItemClicked: (SearchResultItem) -> Unit
) : ListAdapter<SearchResultItem, CollectionAdapter.CollectionViewHolder>(DiffCallback) {

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
        fun bind(searchResultItem: SearchResultItem) {
            val item = searchResultItem.item
            
            var titleText = item.titre
            searchResultItem.similarity?.let {
                 titleText += " (Similarité: %.1f%%)".format(it * 100)
            }
            binding.itemName.text = titleText

            binding.itemImage.load(item.imageUri) {
                placeholder(R.mipmap.ic_launcher)
                error(R.mipmap.ic_launcher)
            }

            fun bindField(textView: android.widget.TextView, value: String?) {
                textView.isVisible = !value.isNullOrBlank()
                textView.text = value
            }

            bindField(binding.itemNotes, item.description)
            bindField(binding.itemEditeur, item.editeur)
            bindField(binding.itemMaterial, item.materiau)
            bindField(binding.itemDimensions, item.dimensions)
            bindField(binding.itemTirage, item.tirage)

            val categoryHierarchy = when {
                !item.superCategorie.isNullOrBlank() && !item.categorie.isNullOrBlank() -> "${item.superCategorie} > ${item.categorie}"
                !item.superCategorie.isNullOrBlank() -> item.superCategorie
                !item.categorie.isNullOrBlank() -> item.categorie
                else -> null
            }
            bindField(binding.itemCategoryHierarchy, categoryHierarchy)

            binding.itemYear.isVisible = item.annee != null
            binding.itemYear.text = item.annee?.toString()

            val isDebuggable = (itemView.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                val sigInfo = item.imageEmbedding?.size?.let { "$it bytes" } ?: "N/A"
                binding.debugInfo.text = "ID: ${item.remoteId}, Sig: $sigInfo"
                binding.debugInfo.visibility = View.VISIBLE
            } else {
                binding.debugInfo.visibility = View.GONE
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