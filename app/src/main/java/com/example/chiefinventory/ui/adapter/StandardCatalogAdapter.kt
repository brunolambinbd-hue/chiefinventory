package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemCatalogChipBinding
import com.example.chiefinventory.model.StandardCatalogItem

/**
 * Entrée de sélection individuelle du catalogue standard.
 *
 * @property item L'ingrédient type du dictionnaire XML.
 * @property isSelected État de sélection (true = 🟢 En Stock, false = 🔴 En Rupture).
 * @property currentQuantity Quantité actuelle en stock.
 */
data class SelectedCatalogEntry(
    val item: StandardCatalogItem,
    var isSelected: Boolean = true,
    var currentQuantity: Double = item.defaultQuantity
)

/**
 * Adaptateur de grille du catalogue du garde-manger avec affichage à 3 couleurs.
 *
 * @param onSelectionChanged Callback déclenché lors de la modification du nombre d'articles sélectionnés.
 */
class StandardCatalogAdapter(
    private val onSelectionChanged: (Int) -> Unit,
    private val onEditIngredientClicked: ((StandardCatalogItem) -> Unit)? = null
) : ListAdapter<SelectedCatalogEntry, StandardCatalogAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemCatalogChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCatalogChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        val binding = holder.binding

        binding.tvIngredientName.text = entry.item.name
        binding.tvCategoryLocation.text = "${entry.item.category} • ${entry.item.defaultLocationName}"

        val effectiveThreshold = entry.item.minThreshold
        val isOutOfStock = !entry.isSelected || entry.currentQuantity <= 0.0
        val isLowStock = entry.isSelected && entry.currentQuantity <= effectiveThreshold && entry.currentQuantity > 0.0

        if (isOutOfStock) {
            // 🔴 En Rupture : Fond Rose Pastel, Texte Rouge Bordeaux Foncé
            binding.cardChip.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
            binding.cardChip.strokeColor = android.graphics.Color.parseColor("#EF5350")
            binding.tvIngredientName.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            binding.tvCategoryLocation.setTextColor(android.graphics.Color.parseColor("#C62828"))
            binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            binding.btnDecrement.setColorFilter(android.graphics.Color.parseColor("#B71C1C"))
            binding.btnIncrement.setColorFilter(android.graphics.Color.parseColor("#B71C1C"))
            binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#B71C1C"))
            binding.layoutQuantityStepper.visibility = View.GONE
        } else if (isLowStock) {
            // 🟡 Stock Faible (Alerte) : Fond Jaune Pastel, Texte Ambre Sombre
            binding.cardChip.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF8E1"))
            binding.cardChip.strokeColor = android.graphics.Color.parseColor("#FFA000")
            binding.tvIngredientName.setTextColor(android.graphics.Color.parseColor("#E65100"))
            binding.tvCategoryLocation.setTextColor(android.graphics.Color.parseColor("#F57C00"))
            binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#E65100"))
            binding.btnDecrement.setColorFilter(android.graphics.Color.parseColor("#E65100"))
            binding.btnIncrement.setColorFilter(android.graphics.Color.parseColor("#E65100"))
            binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#E65100"))
            binding.layoutQuantityStepper.visibility = View.VISIBLE
        } else {
            // 🟢 Stock Sain : Fond Bleu Pastel, Texte Bleu Marine Foncé
            binding.cardChip.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
            binding.cardChip.strokeColor = android.graphics.Color.parseColor("#1976D2")
            binding.tvIngredientName.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
            binding.tvCategoryLocation.setTextColor(android.graphics.Color.parseColor("#1565C0"))
            binding.tvQuantity.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
            binding.btnDecrement.setColorFilter(android.graphics.Color.parseColor("#0D47A1"))
            binding.btnIncrement.setColorFilter(android.graphics.Color.parseColor("#0D47A1"))
            binding.btnEditIngredient.setColorFilter(android.graphics.Color.parseColor("#0D47A1"))
            binding.layoutQuantityStepper.visibility = View.VISIBLE
        }

        val formattedQty = if (entry.currentQuantity % 1.0 == 0.0) entry.currentQuantity.toInt().toString() else entry.currentQuantity.toString()
        val unitStr = entry.item.defaultUnit?.let { " $it" } ?: ""
        binding.tvQuantity.text = "$formattedQty$unitStr"

        binding.cardChip.setOnClickListener {
            entry.isSelected = !entry.isSelected
            notifyItemChanged(holder.bindingAdapterPosition)
            onSelectionChanged(getSelectionCount())
        }

        binding.btnEditIngredient.setOnClickListener {
            onEditIngredientClicked?.invoke(entry.item)
        }

        binding.btnIncrement.setOnClickListener {
            entry.currentQuantity += 1.0
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        binding.btnDecrement.setOnClickListener {
            if (entry.currentQuantity > 1.0) {
                entry.currentQuantity -= 1.0
            } else {
                entry.isSelected = false
            }
            notifyItemChanged(holder.bindingAdapterPosition)
            onSelectionChanged(getSelectionCount())
        }
    }

    fun getSelectionCount(): Int {
        return currentList.count { it.isSelected }
    }

    fun getSelectedEntries(): List<SelectedCatalogEntry> {
        return currentList.filter { it.isSelected }
    }

    object DiffCallback : DiffUtil.ItemCallback<SelectedCatalogEntry>() {
        override fun areItemsTheSame(oldItem: SelectedCatalogEntry, newItem: SelectedCatalogEntry) = oldItem.item.name == newItem.item.name
        override fun areContentsTheSame(oldItem: SelectedCatalogEntry, newItem: SelectedCatalogEntry) = oldItem == newItem
    }
}
