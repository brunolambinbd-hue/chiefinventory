package com.chefinventory.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ItemRecipeBinding
import com.chefinventory.app.model.Recipe

class RecipeAdapter(private val onRecipeClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(internal val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            val context = binding.root.context
            binding.tvRecipeTitle.text = recipe.title

            val prep = if (recipe.preparationTimeMinutes != null) {
                recipe.preparationTimeMinutes
            } else {
                0
            }

            val cook = if (recipe.cookingTimeMinutes != null) {
                recipe.cookingTimeMinutes
            } else {
                0
            }

            val kcalStr = if (recipe.kcalPerServing != null) {
                context.getString(R.string.recipe_kcal_format, recipe.kcalPerServing)
            } else {
                ""
            }

            binding.tvRecipeInfo.text = context.getString(R.string.recipe_info_format, prep, cook, kcalStr)

            val categoryStr = recipe.category.orEmpty().trim()
            val nutritionStr = recipe.nutritionInfo.orEmpty().trim()

            val displayCategory = when {
                categoryStr.isNotBlank() && nutritionStr.isNotBlank() -> context.getString(R.string.recipe_category_nutrition_format, categoryStr, nutritionStr)
                categoryStr.isNotBlank() -> categoryStr
                else -> nutritionStr
            }
            binding.tvRecipeCategory.text = displayCategory
            
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
