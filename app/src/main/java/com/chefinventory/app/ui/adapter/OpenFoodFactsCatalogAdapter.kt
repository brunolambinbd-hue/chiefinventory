package com.chefinventory.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chefinventory.app.databinding.ItemOpenFoodFactsTaxonomyBinding
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.utils.setOnSingleClickListener

/**
 * Adaptateur RecyclerView pour l'affichage fluide et la recherche du Catalogue Open Food Facts.
 */
class OpenFoodFactsCatalogAdapter(
    private val onItemClick: (OpenFoodFactsTaxonomy) -> Unit,
    private val onAddToPantryClick: (OpenFoodFactsTaxonomy) -> Unit
) : ListAdapter<OpenFoodFactsTaxonomy, OpenFoodFactsCatalogAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<OpenFoodFactsTaxonomy>() {
        override fun areItemsTheSame(oldItem: OpenFoodFactsTaxonomy, newItem: OpenFoodFactsTaxonomy): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: OpenFoodFactsTaxonomy, newItem: OpenFoodFactsTaxonomy): Boolean = oldItem == newItem
    }

    class ViewHolder(internal val binding: ItemOpenFoodFactsTaxonomyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOpenFoodFactsTaxonomyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding

        val formattedName = item.cleanName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        binding.tvCleanName.text = formattedName

        val categoryText = when {
            !item.ecobalyseCategory.isNullOrBlank() -> item.ecobalyseCategory
            !item.category.isNullOrBlank() && item.category != "Alimentation" -> item.category
            else -> "other_food_items"
        }
        binding.tvCategory.text = categoryText
        binding.tvCategory.isVisible = true

        if (!item.synonyms.isNullOrBlank()) {
            binding.tvSynonyms.text = item.synonyms
            binding.tvSynonyms.isVisible = true
        } else {
            binding.tvSynonyms.isVisible = false
        }

        binding.root.setOnSingleClickListener {
            onItemClick(item)
        }

        binding.btnAddToPantry.setOnSingleClickListener {
            onAddToPantryClick(item)
        }
    }
}
