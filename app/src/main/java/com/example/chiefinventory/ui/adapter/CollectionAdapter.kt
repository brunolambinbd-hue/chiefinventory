package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemSearchResultBinding
import com.example.chiefinventory.ui.model.SearchResultItem

class CollectionAdapter(private val onItemClicked: (SearchResultItem) -> Unit) :
    ListAdapter<SearchResultItem, CollectionAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current, position)
    }

    class ItemViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResult: SearchResultItem, position: Int) {
            val item = searchResult.item
            val context = binding.root.context

            binding.itemName.text = item.titre
            binding.itemId.text = context.getString(R.string.report_item_id, item.id)

            val detailsParts = mutableListOf<String>()
            item.editeur?.takeIf { it.isNotBlank() }?.let { detailsParts.add(it) }
            item.annee?.let {
                val yearMonth = if (item.mois != null && item.mois > 0) "$it/${item.mois}" else it.toString()
                detailsParts.add(yearMonth)
            }
            item.tirage?.takeIf { it.isNotBlank() }?.let { detailsParts.add("Tirage: $it") }
            binding.itemDetails.text = detailsParts.joinToString(" - ")

            binding.itemDescription.text = item.description
            binding.itemDescription.isVisible = !item.description.isNullOrBlank()

            // Utilisation du zebra striping avec la ressource dédiée (compatible mode nuit)
            val backgroundColor = if (position % 2 != 0) {
                ContextCompat.getColor(context, R.color.zebra_stripe_background)
            } else {
                android.graphics.Color.TRANSPARENT
            }
            binding.innerCardLayout.setBackgroundColor(backgroundColor)
            
            item.imageUri?.let {
                binding.itemImage.load(it.toUri()) {
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                }
            } ?: binding.itemImage.setImageResource(R.mipmap.ic_launcher)

            binding.similarityScore.isVisible = searchResult.similarity != null
            searchResult.similarity?.let {
                val percentage = it * 100
                binding.similarityScore.text = context.getString(R.string.similarity_score_format, percentage)
            }

            // Always show the status badge
            binding.possessionStatus.visibility = View.VISIBLE
            if (item.isPossessed) {
                binding.possessionStatus.text = context.getString(R.string.possessed_status)
                binding.possessionStatus.setBackgroundResource(R.drawable.status_background)
            } else {
                binding.possessionStatus.text = context.getString(R.string.sought_status)
                binding.possessionStatus.setBackgroundResource(R.drawable.status_background_sought)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SearchResultItem>() {
            override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem):
                    Boolean = oldItem.item == newItem.item && oldItem.similarity == newItem.similarity
        }
    }
}
