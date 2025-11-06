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
            b.itemImage.contentDescription = itemView.context.getString(R.string.item_thumbnail_description_dynamic, item.titre)

            if (!item.imageUri.isNullOrBlank()) {
                // On utilise Coil pour charger l'image depuis l'URL.
                b.itemImage.load(item.imageUri) {
                    placeholder(R.mipmap.ic_launcher) // Image affichée pendant le chargement
                    error(R.mipmap.ic_launcher) // Image affichée en cas d'erreur
                }
            } else {
                b.itemImage.setImageResource(R.mipmap.ic_launcher)
            }

            b.itemUniverse.text = item.univers?.let { "Univers: $it" } ?: ""
            b.itemUniverse.visibility = if (item.univers.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemEditeur.text = item.editeur?.let { "Editeur: $it" } ?: ""
            b.itemEditeur.visibility = if (item.editeur.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemYear.text = item.annee?.let { "Année: $it" } ?: ""
            b.itemYear.visibility = if (item.annee == null) View.GONE else View.VISIBLE

            b.itemCategory.text = item.categorie?.let { "Catégorie: $it" } ?: ""
            b.itemCategory.visibility = if (item.categorie.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemMaterial.text = item.materiau?.let { "Matériau: $it" } ?: ""
            b.itemMaterial.visibility = if (item.materiau.isNullOrBlank()) View.GONE else View.VISIBLE

            b.itemDimensions.text = item.dimensions?.let { "Dimensions: $it" } ?: ""
            b.itemDimensions.visibility = if (item.dimensions.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CollectionItem>() {
            override fun areItemsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CollectionItem, newItem: CollectionItem) = oldItem == newItem
        }
    }
}