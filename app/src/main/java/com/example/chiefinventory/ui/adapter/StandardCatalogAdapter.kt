package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
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

enum class CatalogStockState {
    OUT_OF_STOCK,
    LOW_STOCK,
    HEALTHY_STOCK
}

private data class CatalogChipStyle(
    @ColorRes val bgColorRes: Int,
    @ColorRes val strokeColorRes: Int,
    @ColorRes val textColorRes: Int,
    val stepperVisibility: Int
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

    object DiffCallback : DiffUtil.ItemCallback<SelectedCatalogEntry>() {
        override fun areItemsTheSame(oldItem: SelectedCatalogEntry, newItem: SelectedCatalogEntry) = oldItem.item.name == newItem.item.name
        override fun areContentsTheSame(oldItem: SelectedCatalogEntry, newItem: SelectedCatalogEntry) = oldItem == newItem
    }

    class ViewHolder(val binding: ItemCatalogChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCatalogChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvIngredientName.text = entry.item.name
        binding.tvCategoryLocation.text = "${entry.item.category} • ${entry.item.defaultLocationName}"

        val effectiveThreshold = entry.item.minThreshold
        val stockState = determineStockState(entry.isSelected, entry.currentQuantity, effectiveThreshold)

        val style = when (stockState) {
            CatalogStockState.OUT_OF_STOCK -> CatalogChipStyle(
                bgColorRes = R.color.stock_out_of_stock_bg,
                strokeColorRes = R.color.stock_out_of_stock_stroke,
                textColorRes = R.color.stock_out_of_stock_text,
                stepperVisibility = View.GONE
            )
            CatalogStockState.LOW_STOCK -> CatalogChipStyle(
                bgColorRes = R.color.stock_low_bg,
                strokeColorRes = R.color.stock_low_stroke,
                textColorRes = R.color.stock_low_text,
                stepperVisibility = View.VISIBLE
            )
            CatalogStockState.HEALTHY_STOCK -> CatalogChipStyle(
                bgColorRes = R.color.stock_healthy_bg,
                strokeColorRes = R.color.stock_healthy_stroke,
                textColorRes = R.color.stock_healthy_text,
                stepperVisibility = View.VISIBLE
            )
        }

        val textColor = ContextCompat.getColor(context, style.textColorRes)
        binding.cardChip.setCardBackgroundColor(ContextCompat.getColor(context, style.bgColorRes))
        binding.cardChip.strokeColor = ContextCompat.getColor(context, style.strokeColorRes)
        binding.tvIngredientName.setTextColor(textColor)
        binding.tvCategoryLocation.setTextColor(textColor)
        binding.tvQuantity.setTextColor(textColor)
        binding.btnDecrement.setColorFilter(textColor)
        binding.btnIncrement.setColorFilter(textColor)
        binding.btnEditIngredient.setColorFilter(textColor)
        binding.layoutQuantityStepper.visibility = style.stepperVisibility

        val formattedQty = IngredientAdapter.formatDoubleValue(entry.currentQuantity)
        val defaultUnit = entry.item.defaultUnit
        val unitStr = if (!defaultUnit.isNullOrBlank()) {
            " $defaultUnit"
        } else {
            ""
        }
        binding.tvQuantity.text = "$formattedQty$unitStr"

        binding.cardChip.setOnClickListener {
            entry.isSelected = !entry.isSelected
            notifyItemChanged(holder.bindingAdapterPosition)
            onSelectionChanged(getSelectionCount())
        }

        if (onEditIngredientClicked != null) {
            binding.btnEditIngredient.setOnClickListener {
                onEditIngredientClicked.invoke(entry.item)
            }
        } else {
            binding.btnEditIngredient.setOnClickListener(null)
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

    companion object {
        fun determineStockState(isSelected: Boolean, currentQuantity: Double, threshold: Double): CatalogStockState {
            if (!isSelected || currentQuantity <= 0.0) {
                return CatalogStockState.OUT_OF_STOCK
            } else if (currentQuantity <= threshold) {
                return CatalogStockState.LOW_STOCK
            } else {
                return CatalogStockState.HEALTHY_STOCK
            }
        }
    }
}
