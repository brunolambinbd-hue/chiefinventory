package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemCollectionBinding
import com.example.chiefinventory.ui.model.CategoryInfo

/**
 * A RecyclerView adapter for displaying a list of categories.
 *
 * @param isSoughtMode A boolean flag to determine the display format.
 *                     If true (for "Mes Recherches"), shows "(sought/total)".
 *                     If false (for "Mes Produits"), shows "(possessed/total)".
 * @param onItemClicked A lambda function that is invoked with the category name when an item is clicked.
 */
class CategoryAdapter(private val isSoughtMode: Boolean, private val onItemClicked: (String) -> Unit) :
    ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, isSoughtMode)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current.name) }
        holder.bind(current)
    }

    class CategoryViewHolder(private val binding: ItemCollectionBinding, private val isSoughtMode: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryInfo: CategoryInfo) {
            val context = binding.root.context
            val text = if (isSoughtMode) {
                // For "Sought" screen, calculate sought items and display (sought / total)
                val soughtCount = categoryInfo.totalCount - categoryInfo.possessedCount
                context.getString(R.string.category_item_format_sought, categoryInfo.name, soughtCount, categoryInfo.totalCount)
            } else {
                // For "Possessed" screen, display (possessed / total)
                context.getString(R.string.category_item_format_sought, categoryInfo.name, categoryInfo.possessedCount, categoryInfo.totalCount)
            }
            binding.itemName.text = text

            // Hide all other views from the item_collection.xml layout
            binding.itemImage.visibility = View.GONE
            binding.itemRemoteId.visibility = View.GONE
            binding.itemSimilarity.visibility = View.GONE
            binding.itemNotes.visibility = View.GONE
            binding.itemEditeur.visibility = View.GONE
            binding.itemYear.visibility = View.GONE
            binding.itemCategoryHierarchy.visibility = View.GONE
            binding.itemMaterial.visibility = View.GONE
            binding.itemDimensions.visibility = View.GONE
            binding.itemTirage.visibility = View.GONE
            binding.debugInfo.visibility = View.GONE
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
