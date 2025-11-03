package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCollectionBinding
import com.example.parabdcollector.model.CollectionItem

class CollectionAdapter(private val onItemClicked: (CollectionItem) -> Unit) : ListAdapter<CollectionItem, CollectionAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VH(private val b: ItemCollectionBinding) : RecyclerView.ViewHolder(b.root) {
        init {
            b.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(item: CollectionItem) {
            b.itemName.text = item.titre
            b.itemImage.contentDescription = itemView.context.getString(R.string.item_thumbnail_description_dynamic, item.titre)

            // On charge l'image si l'URI existe.
            if (!item.imageUri.isNullOrBlank()) {
                b.itemImage.setImageURI(item.imageUri.toUri())
            } else {
                // Sinon, on affiche une image par défaut.
                b.itemImage.setImageResource(R.mipmap.ic_launcher)
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