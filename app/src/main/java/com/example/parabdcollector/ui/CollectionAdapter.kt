package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
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
            b.itemNotes.text = item.description
            b.itemNotes.visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemImage.contentDescription = itemView.context.getString(R.string.item_thumbnail_description_dynamic, item.titre)

            if (!item.imageUri.isNullOrBlank()) {
                b.itemImage.load(item.imageUri) {
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                }
            } else {
                b.itemImage.setImageResource(R.mipmap.ic_launcher)
            }

            b.itemEditeur.text = item.editeur?.let { "Editeur: $it" } ?: ""
            b.itemEditeur.visibility = if (item.editeur.isNullOrBlank()) View.GONE else View.VISIBLE

            var yearMonthText = ""
            item.annee?.let { yearMonthText += "Année: $it" }
            item.mois?.let { yearMonthText += "/$it" }
            b.itemYear.text = yearMonthText
            b.itemYear.visibility = if (yearMonthText.isBlank()) View.GONE else View.VISIBLE

            b.itemSuperCategory.text = item.superCategorie?.let { "Super-Catégorie: $it" } ?: ""
            b.itemSuperCategory.visibility = if (item.superCategorie.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemCategory.text = item.categorie?.let { "Catégorie: $it" } ?: ""
            b.itemCategory.visibility = if (item.categorie.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemMaterial.text = item.materiau?.let { "Matériau: $it" } ?: ""
            b.itemMaterial.visibility = if (item.materiau.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemDimensions.text = item.dimensions?.let { "Dimensions: $it" } ?: ""
            b.itemDimensions.visibility = if (item.dimensions.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemTirage.text = item.tirage?.let { "Tirage: $it ex." } ?: ""
            b.itemTirage.visibility = if (item.tirage.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CollectionItem>() {
            override fun areItemsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem == newItem
        }
    }
}