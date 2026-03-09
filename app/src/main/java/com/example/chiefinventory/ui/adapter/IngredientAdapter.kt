package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.databinding.ItemIngredientBinding
import com.example.chiefinventory.model.Ingredient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IngredientAdapter(private val onClick: (Ingredient) -> Unit) :
    ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(ingredient: Ingredient, onClick: (Ingredient) -> Unit) {
            // Affichage du nom avec l'info supplémentaire éventuelle (ex: poids)
            binding.tvName.text = if (!ingredient.supplementalInfo.isNullOrBlank()) {
                "${ingredient.name} (${ingredient.supplementalInfo})"
            } else {
                ingredient.name
            }

            binding.tvQuantity.text = buildString {
                append(ingredient.quantity ?: "")
                append(" ")
                append(ingredient.unit ?: "")
            }.trim()

            binding.tvExpiry.text = ingredient.expirationDate?.let { 
                "Expire le : " + dateFormat.format(Date(it))
            } ?: ""

            binding.root.setOnClickListener { onClick(ingredient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem == newItem
    }
}
