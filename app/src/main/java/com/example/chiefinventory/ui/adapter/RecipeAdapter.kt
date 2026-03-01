package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chiefinventory.databinding.ItemRecipeBinding
import com.example.chiefinventory.model.Recipe

class RecipeAdapter(private val onRecipeClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvRecipeTitle.text = recipe.title
            
            val prep = recipe.preparationTimeMinutes ?: 0
            val cook = recipe.cookingTimeMinutes ?: 0
            binding.tvRecipeInfo.text = "Prép: ${prep}min | Cuisson: ${cook}min"
            binding.tvRecipeCategory.text = recipe.category ?: ""
            
            recipe.imageUri?.let {
                binding.ivRecipeThumbnail.load(it)
            }

            binding.root.setOnClickListener { onRecipeClick(recipe) }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean = oldItem == newItem
    }
}
