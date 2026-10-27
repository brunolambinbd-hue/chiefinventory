package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.databinding.ItemIngredientBinding
import com.example.chiefinventory.model.Ingredient

class IngredientAdapter(
    private val onClick: (Ingredient) -> Unit,
    private val onEditClick: ((Ingredient) -> Unit)? = null,
    private var locationNamesMap: Map<Long, String> = emptyMap()
) : ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DiffCallback) {

    fun setLocationNamesMap(map: Map<Long, String>) {
        this.locationNamesMap = map
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            ingredient: Ingredient,
            onClick: (Ingredient) -> Unit,
            onEditClick: ((Ingredient) -> Unit)?,
            locationNamesMap: Map<Long, String>
        ) {
            binding.tvName.text = ingredient.name

            val qtyVal = ingredient.quantity ?: 0.0
            val formattedQty = if (qtyVal % 1.0 == 0.0) qtyVal.toInt().toString() else qtyVal.toString()
            val unitStr = ingredient.unit?.let { if (it.isNotBlank()) " $it" else "" } ?: ""
            binding.tvQuantity.text = "$formattedQty$unitStr"

            val thresholdVal = ingredient.minThreshold ?: 0.0
            val isOutOfStock = qtyVal <= 0.0
            val isLowStock = qtyVal <= thresholdVal && qtyVal > 0.0

            val formattedThreshold = if (thresholdVal % 1.0 == 0.0) thresholdVal.toInt().toString() else thresholdVal.toString()
            val packVal = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0
            val formattedPack = if (packVal % 1.0 == 0.0) packVal.toInt().toString() else packVal.toString()

            val packInfoStr = if (packVal > 1.0) " • Pack: $formattedPack" else ""

            if (isOutOfStock) {
                binding.cardIngredient.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
                binding.cardIngredient.strokeColor = android.graphics.Color.parseColor("#EF5350")
                binding.tvStockStatus.text = "🚨 En Rupture (Seuil: $formattedThreshold$unitStr$packInfoStr)"
                binding.tvStockStatus.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
                binding.tvName.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
                binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
                binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#B71C1C"))
            } else if (isLowStock) {
                binding.cardIngredient.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF8E1"))
                binding.cardIngredient.strokeColor = android.graphics.Color.parseColor("#FFA000")
                binding.tvStockStatus.text = "⚠️ Stock Faible (Seuil: $formattedThreshold$unitStr$packInfoStr)"
                binding.tvStockStatus.setTextColor(android.graphics.Color.parseColor("#E65100"))
                binding.tvName.setTextColor(android.graphics.Color.parseColor("#E65100"))
                binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#E65100"))
                binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#E65100"))
            } else {
                binding.cardIngredient.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
                binding.cardIngredient.strokeColor = android.graphics.Color.parseColor("#1976D2")
                binding.tvStockStatus.text = "🎯 Seuil min : $formattedThreshold$unitStr$packInfoStr"
                binding.tvStockStatus.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
                binding.tvName.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
                binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
                binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#0D47A1"))
            }

            val rawHomeLocName = ingredient.locationId?.let { locationNamesMap[it] } ?: "Frigo"
            val homeLocIcon = when {
                rawHomeLocName.contains("Frigo", ignoreCase = true) -> "❄️"
                rawHomeLocName.contains("Placard", ignoreCase = true) -> "🧺"
                rawHomeLocName.contains("Surgélateur", ignoreCase = true) || rawHomeLocName.contains("Congélateur", ignoreCase = true) -> "🧊"
                rawHomeLocName.contains("Cave", ignoreCase = true) -> "🍷"
                else -> "📍"
            }

            val categoryStr = ingredient.category ?: "Épicerie"
            binding.tvLocation.text = "$homeLocIcon $rawHomeLocName  •  📍 $categoryStr"
            binding.tvStockPolicy.text = "🔄 ${ingredient.stockPolicy.uppercase()}"

            if (!ingredient.supplementalInfo.isNullOrBlank()) {
                binding.tvSupplementalInfo.isVisible = true
                binding.tvSupplementalInfo.text = "Info : ${ingredient.supplementalInfo}"
            } else {
                binding.tvSupplementalInfo.isVisible = false
            }

            binding.root.setOnClickListener { onClick(ingredient) }
            binding.btnEditIngredient.setOnClickListener { onEditClick?.invoke(ingredient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onEditClick, locationNamesMap)
    }

    object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem == newItem
    }
}
