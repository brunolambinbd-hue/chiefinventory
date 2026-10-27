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

        val currentQty = ingredient.quantity ?: 0.0
        val targetThreshold = ingredient.minThreshold ?: 0.0
        val packSize = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0

        val netMissing = if (targetThreshold > currentQty) {
            targetThreshold - currentQty
        } else if (currentQty <= 0.0) {
            if (targetThreshold > 0.0) targetThreshold else 1.0
        } else {
            1.0
        }

        val numPacks = kotlin.math.ceil(netMissing / packSize).toInt().coerceAtLeast(1)
        val qtyToBuy = numPacks * packSize

        val formattedQty = if (qtyToBuy % 1.0 == 0.0) qtyToBuy.toInt().toString() else qtyToBuy.toString()
        val unitStr = ingredient.unit?.let { if (it.isNotBlank()) " $it" else "" } ?: ""

        binding.tvQuantityToBuy.text = "$formattedQty$unitStr"
        binding.tvQuantityToBuy.visibility = View.VISIBLE

        val packInfoStr = if (packSize > 1.0) " ($numPacks pack${if (numPacks > 1) "s" else ""} de ${if (packSize % 1.0 == 0.0) packSize.toInt() else packSize})" else ""
        binding.tvItemName.text = " — ${ingredient.name}$packInfoStr"

        val categoryStr = ingredient.category ?: "Épicerie"
        binding.tvCategoryAndLocation.text = categoryStr

        val isBought = (ingredient.quantity ?: 0.0) > 0.0
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
}
