package com.example.chiefinventory.ui.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemCategoryRevisedBinding
import com.example.chiefinventory.ui.model.CategoryInfo

class CategoryAdapterRevised(
    private val isSoughtMode: Boolean, 
    private val onItemClicked: (String) -> Unit
) : ListAdapter<CategoryInfo, CategoryAdapterRevised.CategoryViewHolder>(DiffCallback) {

    // Enum to represent the possible changes in a payload.
    private enum class Payload {
        PROGRESS_UPDATE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRevisedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, isSoughtMode)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // No payload, do a full bind
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Payload present, do a partial update
            val current = getItem(position)
            holder.updateProgress(current)
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current.name) }
        holder.bind(current)
    }

    class CategoryViewHolder(private val binding: ItemCategoryRevisedBinding, private val isSoughtMode: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryInfo: CategoryInfo) {
            binding.revisedCategoryName.text = categoryInfo.name
            updateProgress(categoryInfo)
        }

        fun updateProgress(categoryInfo: CategoryInfo) {
            val context = binding.root.context
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
                binding.revisedCategoryProgress.progressDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            } else {
                binding.revisedCategoryProgress.max = 1
                binding.revisedCategoryProgress.progress = 0
            }
        }
    }

    companion object {
        private object DiffCallback : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem.possessedCount == newItem.possessedCount && oldItem.totalCount == newItem.totalCount
            }

            override fun getChangePayload(oldItem: CategoryInfo, newItem: CategoryInfo): Any? {
                return if (oldItem.possessedCount != newItem.possessedCount || oldItem.totalCount != newItem.totalCount) {
                    Payload.PROGRESS_UPDATE
                } else {
                    null
                }
            }
        }
    }
}
