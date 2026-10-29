package com.example.chiefinventory.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemIngredientBinding
import com.example.chiefinventory.model.Ingredient

private data class StockStyle(
    @ColorRes val bgColorRes: Int,
    @ColorRes val strokeColorRes: Int,
    @ColorRes val textColorRes: Int,
    val statusText: String
)

class IngredientAdapter(
    private val onClick: (Ingredient) -> Unit,
    private val onEditClick: ((Ingredient) -> Unit)? = null,
    private var locationNamesMap: Map<Long, String> = emptyMap()
) : ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DiffCallback) {

    fun setLocationNamesMap(map: Map<Long, String>) {
        this.locationNamesMap = map
        notifyDataSetChanged()
    }

    object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) = oldItem == newItem
    }

    class ViewHolder(internal val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            ingredient: Ingredient,
            onClick: (Ingredient) -> Unit,
            onEditClick: ((Ingredient) -> Unit)?,
            locationNamesMap: Map<Long, String>
        ) {
            val context = binding.root.context
            binding.tvName.text = ingredient.name

            val qtyVal = if (ingredient.quantity != null) {
                ingredient.quantity
            } else {
                0.0
            }

            val formattedQty = formatDoubleValue(qtyVal)

            val unitStr = if (!ingredient.unit.isNullOrBlank()) {
                " ${ingredient.unit}"
            } else {
                ""
            }
            binding.tvQuantity.text = "$formattedQty$unitStr"

            val thresholdVal = if (ingredient.minThreshold != null) {
                ingredient.minThreshold
            } else {
                0.0
            }

            val isOutOfStock = qtyVal <= 0.0
            val isLowStock = qtyVal <= thresholdVal && qtyVal > 0.0

            val formattedThreshold = formatDoubleValue(thresholdVal)
            val packInfoStr = formatPackInfo(ingredient.packSize, context)

            val style = when {
                isOutOfStock -> StockStyle(
                    bgColorRes = R.color.stock_out_of_stock_bg,
                    strokeColorRes = R.color.stock_out_of_stock_stroke,
                    textColorRes = R.color.stock_out_of_stock_text,
                    statusText = context.getString(
                        R.string.stock_status_out_of_stock,
                        context.getString(R.string.icon_status_out_of_stock),
                        formattedThreshold,
                        unitStr,
                        packInfoStr
                    )
                )
                isLowStock -> StockStyle(
                    bgColorRes = R.color.stock_low_bg,
                    strokeColorRes = R.color.stock_low_stroke,
                    textColorRes = R.color.stock_low_text,
                    statusText = context.getString(
                        R.string.stock_status_low,
                        context.getString(R.string.icon_status_low_stock),
                        formattedThreshold,
                        unitStr,
                        packInfoStr
                    )
                )
                else -> StockStyle(
                    bgColorRes = R.color.stock_healthy_bg,
                    strokeColorRes = R.color.stock_healthy_stroke,
                    textColorRes = R.color.stock_healthy_text,
                    statusText = context.getString(
                        R.string.stock_status_healthy,
                        context.getString(R.string.icon_status_healthy_stock),
                        formattedThreshold,
                        unitStr,
                        packInfoStr
                    )
                )
            }

            val textColor = ContextCompat.getColor(context, style.textColorRes)
            binding.cardIngredient.setCardBackgroundColor(ContextCompat.getColor(context, style.bgColorRes))
            binding.cardIngredient.strokeColor = ContextCompat.getColor(context, style.strokeColorRes)
            binding.tvStockStatus.text = style.statusText
            binding.tvStockStatus.setTextColor(textColor)
            binding.tvName.setTextColor(textColor)
            binding.tvQuantity.setTextColor(textColor)
            binding.btnEditIngredient.setColorFilter(textColor)

            val rawHomeLocName = resolveLocationName(ingredient.locationId, locationNamesMap, context)
            val homeLocIcon = resolveHomeLocationIcon(rawHomeLocName, context)
            val categoryStr = resolveCategoryName(ingredient.category, context)
            val pinIcon = context.getString(R.string.icon_location_default)

            binding.tvLocation.text = context.getString(
                R.string.ingredient_location_category_format,
                homeLocIcon,
                rawHomeLocName,
                pinIcon,
                categoryStr
            )
            binding.tvStockPolicy.text = context.getString(
                R.string.ingredient_stock_policy_format,
                context.getString(R.string.icon_refresh),
                ingredient.stockPolicy.uppercase()
            )

            val infoText = resolveSupplementalInfoText(ingredient.supplementalInfo, context)
            if (infoText != null) {
                binding.tvSupplementalInfo.isVisible = true
                binding.tvSupplementalInfo.text = infoText
            } else {
                binding.tvSupplementalInfo.isVisible = false
            }

            binding.root.setOnClickListener { onClick(ingredient) }

            if (onEditClick != null) {
                binding.btnEditIngredient.setOnClickListener { onEditClick.invoke(ingredient) }
            } else {
                binding.btnEditIngredient.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onEditClick, locationNamesMap)
    }

    companion object {
        fun formatDoubleValue(value: Double): String {
            if (value % 1.0 == 0.0) {
                return value.toInt().toString()
            } else {
                return value.toString()
            }
        }

        fun formatPackInfo(packSize: Double, context: Context? = null): String {
            val packVal = if (packSize > 0.0) {
                packSize
            } else {
                1.0
            }

            if (packVal > 1.0) {
                val formatted = formatDoubleValue(packVal)
                return context?.getString(R.string.ingredient_pack_info_format, formatted) ?: " • Pack: $formatted"
            } else {
                return ""
            }
        }

        fun resolveHomeLocationIcon(locationName: String?, context: Context? = null): String {
            val defaultIcon = context?.getString(R.string.icon_location_frigo) ?: "❄️"
            if (locationName.isNullOrBlank()) {
                return defaultIcon
            }

            val lower = locationName.lowercase()

            if ("frigo" in lower || "fridge" in lower) {
                return defaultIcon
            }

            if ("placard" in lower || "cupboard" in lower || "pantry" in lower) {
                return context?.getString(R.string.icon_location_placard) ?: "🧺"
            }

            if ("surgélateur" in lower || "freezer" in lower || "congélateur" in lower) {
                return context?.getString(R.string.icon_location_freezer) ?: "🧊"
            }

            if ("cave" in lower || "cellar" in lower) {
                return context?.getString(R.string.icon_location_cave) ?: "🍷"
            }

            return context?.getString(R.string.icon_location_default) ?: "📍"
        }

        fun resolveLocationName(locationId: Long?, locationNamesMap: Map<Long, String>, context: Context? = null): String {
            if (locationId == null) {
                return context?.getString(R.string.location_default_frigo) ?: "Frigo"
            }

            val mappedName = locationNamesMap[locationId]
            if (mappedName.isNullOrBlank()) {
                return context?.getString(R.string.location_default_frigo) ?: "Frigo"
            }

            return mappedName
        }

        fun resolveCategoryName(category: String?, context: Context? = null): String {
            if (category.isNullOrBlank()) {
                return context?.getString(R.string.category_default_epicerie) ?: "Épicerie"
            }

            return category
        }

        fun resolveSupplementalInfoText(supplementalInfo: String?, context: Context? = null): String? {
            if (supplementalInfo.isNullOrBlank()) {
                return null
            }

            return context?.getString(R.string.ingredient_info_format, supplementalInfo) ?: "Info : $supplementalInfo"
        }
    }
}
