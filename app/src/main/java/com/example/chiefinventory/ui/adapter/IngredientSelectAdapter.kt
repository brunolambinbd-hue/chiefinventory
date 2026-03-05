package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.databinding.ItemIngredientSelectBinding
import com.example.chiefinventory.model.Ingredient

class IngredientSelectAdapter(
    private val onIngredientToggle: (String) -> Unit
) : ListAdapter<IngredientSelectAdapter.IngredientItem, IngredientSelectAdapter.ViewHolder>(DiffCallback()) {

    data class IngredientItem(
        val name: String,
        val isSelected: Boolean
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemIngredientSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IngredientItem) {
            binding.tvIngredientName.text = item.name
            binding.checkbox.isChecked = item.isSelected
            
            binding.root.setOnClickListener {
                onIngredientToggle(item.name)
            }
            binding.checkbox.setOnClickListener {
                onIngredientToggle(item.name)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<IngredientItem>() {
        override fun areItemsTheSame(oldItem: IngredientItem, newItem: IngredientItem) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: IngredientItem, newItem: IngredientItem) =
            oldItem == newItem
    }
}
