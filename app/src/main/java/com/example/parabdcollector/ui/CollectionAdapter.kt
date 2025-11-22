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
import java.nio.ByteBuffer

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
            val context = itemView.context

            binding.itemName.text = item.titre

            // On affiche la similarité si elle est disponible
            searchResultItem.similarity?.let {
                binding.itemSimilarity.text = context.getString(R.string.similarity_score_format, it * 100)
                binding.itemSimilarity.visibility = View.VISIBLE
            } ?: run {
                binding.itemSimilarity.visibility = View.GONE
            }

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

            val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                val sigInfo = item.imageEmbedding?.let { embedding ->
                    if (embedding.isNotEmpty()) {
                        val byteBuffer = ByteBuffer.wrap(embedding)
                        val preview = (1..5).map { "%.2f".format(byteBuffer.float) }.joinToString(", ")
                        context.getString(R.string.signature_preview_format, preview)
                    } else {
                        context.getString(R.string.signature_status_empty)
                    }
                } ?: context.getString(R.string.signature_status_missing)
                
                binding.debugInfo.text = context.getString(R.string.debug_signature_info, item.remoteId, sigInfo)
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
