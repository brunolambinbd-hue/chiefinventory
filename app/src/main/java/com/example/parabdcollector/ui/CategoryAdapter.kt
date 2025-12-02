package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.model.CategoryInfo

/**
 * An adapter for displaying a list of categories or super-categories in a RecyclerView.
 *
 * This adapter takes a list of [CategoryInfo] objects, which contain the category name and the count of items in it.
 * It uses a simple layout to display the formatted string.
 *
 * @param onItemClicked A lambda function to be invoked when an item in the list is clicked.
 *                      It receives the name of the clicked category as a String.
 */
class CategoryAdapter(private val onItemClicked: (String) -> Unit) : ListAdapter<CategoryInfo, CategoryAdapter.VH>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(itemView as TextView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val categoryInfo = getItem(position)
        holder.bind(categoryInfo)
    }

    /**
     * ViewHolder for a single category item.
     * @param textView The TextView that represents the entire list item layout.
     */
    inner class VH(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        init {
            // Set up the click listener for the item view.
            textView.setOnClickListener {
                val position = bindingAdapterPosition
                // Ensure the position is valid before handling the click.
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position).name)
                }
            }
        }

        /**
         * Binds a [CategoryInfo] object to the TextView.
         * @param categoryInfo The data to display.
         */
        fun bind(categoryInfo: CategoryInfo) {
            textView.text = textView.context.getString(R.string.category_item_format, categoryInfo.name, categoryInfo.count)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                // Items are considered the same if their names are identical.
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                // Content is the same if the objects are equal (data class implements this).
                return oldItem == newItem
            }
        }
    }
}
