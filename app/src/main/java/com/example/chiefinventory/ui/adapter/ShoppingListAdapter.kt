package com.example.chiefinventory.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.databinding.ItemShoppingListBinding
import com.example.chiefinventory.model.Ingredient

/**
 * Adaptateur de la liste de réassort et de courses.
 *
 * @see REQ-4.5.1.1 Formule de calcul de réassort (TargetThreshold - CurrentStock).
 * @see REQ-4.5.1.2 Format d'affichage papier caddie ultra-lisible ("6 L — Lait demi-écrémé").
 * @see REQ-4.5.1.3 Auto-réassort du garde-manger lors de la mise en caddie.
 */
class ShoppingListAdapter(
    private val onItemBoughtChanged: (Ingredient, Boolean) -> Unit
) : ListAdapter<Ingredient, ShoppingListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemShoppingListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShoppingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = getItem(position)
        val binding = holder.binding

        val currentQty = if (ingredient.quantity != null) {
            ingredient.quantity
        } else {
            0.0
        }

        val targetThreshold = if (ingredient.minThreshold != null) {
            ingredient.minThreshold
        } else {
            0.0
        }

        val rawPackSize = ingredient.packSize
        val packSize = if (rawPackSize > 0.0) {
            rawPackSize
        } else {
            1.0
        }

        val netMissing = calculateNetMissing(currentQty, targetThreshold)
        val numPacks = kotlin.math.ceil(netMissing / packSize).toInt().coerceAtLeast(1)
        val qtyToBuy = numPacks * packSize

        val formattedQty = IngredientAdapter.formatDoubleValue(qtyToBuy)
        val unitStr = if (!ingredient.unit.isNullOrBlank()) {
            " ${ingredient.unit}"
        } else {
            ""
        }

        binding.tvQuantityToBuy.text = "$formattedQty$unitStr"
        binding.tvQuantityToBuy.visibility = View.VISIBLE

        val packInfoStr = formatPackInfo(packSize, numPacks)
        binding.tvItemName.text = " — ${ingredient.name}$packInfoStr"

        val categoryStr = if (!ingredient.category.isNullOrBlank()) {
            ingredient.category
        } else {
            "Épicerie"
        }
        binding.tvCategoryAndLocation.text = categoryStr

        val isBought = currentQty > 0.0
        binding.cbBought.isChecked = isBought

        if (isBought) {
            binding.tvQuantityToBuy.paintFlags = binding.tvQuantityToBuy.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvItemName.paintFlags = binding.tvItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.tvQuantityToBuy.paintFlags = binding.tvQuantityToBuy.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.tvItemName.paintFlags = binding.tvItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        binding.cbBought.setOnCheckedChangeListener { _, isChecked ->
            onItemBoughtChanged(ingredient, isChecked)
        }

        binding.cardShoppingItem.setOnClickListener {
            binding.cbBought.isChecked = !binding.cbBought.isChecked
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem == newItem
    }

    companion object {
        fun calculateNetMissing(currentQty: Double, targetThreshold: Double): Double {
            if (targetThreshold > currentQty) {
                return targetThreshold - currentQty
            } else {
                return 1.0
            }
        }

        fun formatPackInfo(packSize: Double, numPacks: Int): String {
            if (packSize <= 1.0) {
                return ""
            }

            val plural = if (numPacks > 1) {
                "s"
            } else {
                ""
            }

            val formattedPackSize = IngredientAdapter.formatDoubleValue(packSize)
            return " ($numPacks pack$plural de $formattedPackSize)"
        }
    }
}
