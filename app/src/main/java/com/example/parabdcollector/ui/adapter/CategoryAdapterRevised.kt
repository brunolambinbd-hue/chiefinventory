package com.example.parabdcollector.ui.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemCategoryRevisedBinding
import com.example.parabdcollector.ui.model.CategoryInfo

/**
 * A RecyclerView adapter for displaying a list of categories.
 *
 * @param isSoughtMode A boolean flag to determine the display format.
 *                     If true (for "Mes Recherches"), shows "(sought/total)".
 *                     If false (for "Mes Produits"), shows "(possessed/total)".
 * @param onItemClicked A lambda function that is invoked with the category name when an item is clicked.
 */
class CategoryAdapterRevised(private val isSoughtMode: Boolean, private val onItemClicked: (String) -> Unit) :
    ListAdapter<CategoryInfo, CategoryAdapterRevised.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRevisedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, isSoughtMode)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current.name) }
        holder.bind(current)
    }

    class CategoryViewHolder(private val binding: ItemCategoryRevisedBinding, private val isSoughtMode: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryInfo: CategoryInfo) {
            val context = binding.root.context
            binding.revisedCategoryName.text = categoryInfo.name

            val possessedCount = categoryInfo.possessedCount
            val totalCount = categoryInfo.totalCount
            val soughtCount = totalCount - possessedCount

            val countToShow = if (isSoughtMode) soughtCount else possessedCount

            binding.revisedCategoryCount.text = context.getString(R.string.category_count_format, countToShow, totalCount)

            if (totalCount > 0) {
                binding.revisedCategoryProgress.max = totalCount
                binding.revisedCategoryProgress.progress = countToShow

                val percentage = (possessedCount * 100) / totalCount

                val progressColorRes = if (isSoughtMode) {
                    when {
                        percentage > 75 -> R.color.status_ok // Almost complete, few sought
                        percentage > 25 -> R.color.status_warning
                        else -> R.color.status_error // Not complete at all, many sought
                    }
                } else {
                    when {
                        percentage < 25 -> R.color.status_error
                        percentage < 75 -> R.color.status_warning
                        else -> R.color.status_ok
                    }
                }
                val color = ContextCompat.getColor(context, progressColorRes)
                binding.revisedCategoryProgress.progressDrawable.colorFilter = 
                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

            } else {
                binding.revisedCategoryProgress.max = 1
                binding.revisedCategoryProgress.progress = 0
            }
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
