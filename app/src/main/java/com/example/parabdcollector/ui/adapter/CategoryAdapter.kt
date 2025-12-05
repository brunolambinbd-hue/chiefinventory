package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCollectionBinding
import com.example.parabdcollector.ui.model.CategoryInfo

/**
 * A RecyclerView adapter for displaying a list of categories (either super-categories or sub-categories).
 *
 * This adapter takes a list of [CategoryInfo] objects and displays them, showing the category name and the
 * number of items within that category. It re-uses the item_collection layout for display.
 *
 * @param onItemClicked A lambda function that is invoked with the category name when an item is clicked.
 */
class CategoryAdapter(private val onItemClicked: (String) -> Unit) :
    ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    /**
     * Creates a new [CategoryViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCollectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    /**
     * Binds a [CategoryInfo] item to a [CategoryViewHolder] and sets its click listener.
     * The listener is set unconditionally to allow navigation into empty categories.
     */
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current.name)
        }
        holder.bind(current)
    }

    /**
     * ViewHolder for a single category item.
     * @param binding The view binding for the item's layout.
     */
    class CategoryViewHolder(private var binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the [CategoryInfo] data to the views.
         * @param categoryInfo The data to display.
         */
        fun bind(categoryInfo: CategoryInfo) {
            // Use the correct view ID 'itemName' from item_collection.xml
            binding.itemName.text =
                binding.root.context.getString(R.string.category_item_format, categoryInfo.name, categoryInfo.count)

            // Hide all other views from the item_collection.xml layout as they are not relevant for a category
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
        /**
         * A DiffUtil.ItemCallback implementation to efficiently update the list.
         */
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
