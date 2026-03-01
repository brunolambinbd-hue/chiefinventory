package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.databinding.ItemRecipeCategoryBinding

class RecipeCategoryAdapter(private val onCategoryClick: (RecipeCategoryInfo) -> Unit) :
    ListAdapter<RecipeCategoryInfo, RecipeCategoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecipeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(info: RecipeCategoryInfo) {
            binding.tvCategoryName.text = info.categoryName
            binding.tvRecipeCount.text = info.recipeCount.toString()
            binding.root.setOnClickListener { onCategoryClick(info) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RecipeCategoryInfo>() {
        override fun areItemsTheSame(oldItem: RecipeCategoryInfo, newItem: RecipeCategoryInfo): Boolean =
            oldItem.categoryId == newItem.categoryId
        override fun areContentsTheSame(oldItem: RecipeCategoryInfo, newItem: RecipeCategoryInfo): Boolean =
            oldItem == newItem
    }
}
