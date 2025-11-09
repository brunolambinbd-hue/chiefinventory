package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemSearchResultBinding
import com.example.parabdcollector.model.CollectionItem

class SearchResultAdapter(private val onItemClicked: (CollectionItem) -> Unit) : ListAdapter<CollectionItem, SearchResultAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VH(private val b: ItemSearchResultBinding) : RecyclerView.ViewHolder(b.root) {
        init {
            b.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(item: CollectionItem) {
            b.searchItemTitle.text = item.titre
            b.searchItemDescription.text = item.description // Correction : on utilise bien le champ 'notes' pour la description

            if (!item.imageUri.isNullOrBlank()) {
                b.searchItemImage.load(item.imageUri) {
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                }
            } else {
                b.searchItemImage.setImageResource(R.mipmap.ic_launcher)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CollectionItem>() {
            override fun areItemsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem == newItem
        }
    }
}