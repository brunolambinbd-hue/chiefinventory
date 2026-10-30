package com.chefinventory.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ItemMatchRecipeBinding
import com.chefinventory.app.utils.RecipeMatchResult
import com.chefinventory.app.utils.setOnSingleClickListener

class MatchRecipeAdapter(
    private val onRecipeClicked: (RecipeMatchResult) -> Unit
) : ListAdapter<RecipeMatchResult, MatchRecipeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemMatchRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMatchRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvRecipeTitle.text = item.recipe.title
        val pctInt = item.matchPercentage.toInt()
        binding.tvMatchPercentage.text = context.getString(R.string.match_percentage_format, pctInt)

        if (pctInt == 100) {
            binding.tvMatchPercentage.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            binding.tvStockDetails.text = context.getString(R.string.match_details_100_format, item.totalInStock, item.totalRequired)
            binding.tvMissingDetails.visibility = View.GONE
        } else {
            val color = if (pctInt >= 50) android.R.color.holo_orange_dark else android.R.color.holo_red_dark
            binding.tvMatchPercentage.setBackgroundColor(ContextCompat.getColor(context, color))
            binding.tvStockDetails.text = context.getString(R.string.match_details_partial_format, item.totalInStock, item.totalRequired)
            binding.tvMissingDetails.visibility = View.VISIBLE
            binding.tvMissingDetails.text = context.getString(R.string.match_details_missing_format, item.missingIngredients.joinToString(", "))
        }

        binding.root.setOnSingleClickListener {
            onRecipeClicked(item)
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<RecipeMatchResult>() {
        override fun areItemsTheSame(oldItem: RecipeMatchResult, newItem: RecipeMatchResult): Boolean = oldItem.recipe.id == newItem.recipe.id
        override fun areContentsTheSame(oldItem: RecipeMatchResult, newItem: RecipeMatchResult): Boolean = oldItem == newItem
    }
}
