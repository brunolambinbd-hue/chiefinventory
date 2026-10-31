package com.chefinventory.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ItemIngredientBinding
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.effectiveMinThreshold
import com.chefinventory.app.model.effectiveQuantity
import com.chefinventory.app.utils.setOnSingleClickListener
import com.parabd.lib.traceability.utils.LogManager
import com.parabd.lib.traceability.utils.uException

private data class StockStyle(
    @get:ColorRes val bgColorRes: Int,
    @get:ColorRes val strokeColorRes: Int,
    @get:ColorRes val textColorRes: Int,
    val statusText: String
)

class IngredientAdapter(
    private val onClick: (Ingredient) -> Unit,
    private val onEditClick: ((Ingredient) -> Unit)? = null,
    private var locationNamesMap: Map<Long, String> = emptyMap()
) : ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DiffCallback) {

    @Suppress("NotifyDataSetChanged")
    fun setLocationNamesMap(map: Map<Long, String>) {
        this.locationNamesMap = map
        val size = itemCount
        if (size > 0) {
            notifyItemRangeChanged(0, size)
        } else {
            notifyDataSetChanged()
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean = oldItem == newItem
    }

    class ViewHolder(internal val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {

        @Suppress("SetTextI18n")
        fun bind(
            ingredient: Ingredient,
            onClick: (Ingredient) -> Unit,
            onEditClick: ((Ingredient) -> Unit)?,
            locationNamesMap: Map<Long, String>
        ) {
            val context = binding.root.context
            binding.tvName.text = ingredient.name

            val qtyVal = ingredient.effectiveQuantity
            val formattedQty = formatDoubleValue(qtyVal)

            val cleanUnit = ingredient.unit.orEmpty().trim()
            val unitStr = if (cleanUnit.isNotEmpty()) " $cleanUnit" else ""
            binding.tvQuantity.text = "$formattedQty$unitStr"

            val thresholdVal = ingredient.effectiveMinThreshold
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
            binding.tvLocation.setTextColor(textColor)
            binding.tvStockPolicy.setTextColor(textColor)
            binding.tvSupplementalInfo.setTextColor(textColor)
            binding.btnEditIngredient.setColorFilter(textColor)

            val rawHomeLocName = resolveLocationName(ingredient.locationId, locationNamesMap, ingredient.category, context)
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

            binding.root.setOnSingleClickListener { onClick(ingredient) }

            if (onEditClick != null) {
                binding.btnEditIngredient.setOnSingleClickListener { onEditClick(ingredient) }
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
        private const val TAG = "IngredientAdapter"

        fun formatDoubleValue(value: Double): String {
            return if (value % 1.0 == 0.0) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        }

        fun formatPackInfo(packSize: Double, context: Context): String {
            val packVal = if (packSize > 0.0) packSize else 1.0
            if (packVal <= 1.0) return ""

            val formatted = formatDoubleValue(packVal)
            return context.getString(R.string.ingredient_pack_info_format, formatted)
        }

        fun resolveHomeLocationIcon(locationName: String?, context: Context): String {
            val iconFrigo = context.getString(R.string.icon_location_frigo)
            val iconPlacard = context.getString(R.string.icon_location_placard)
            val iconFreezer = context.getString(R.string.icon_location_freezer)
            val iconCave = context.getString(R.string.icon_location_cave)

            if (locationName.isNullOrBlank()) return iconFrigo

            val lower = locationName.lowercase()
            val res = context.resources

            val frigoKeywords = res.getStringArray(R.array.keywords_location_frigo)
            val placardKeywords = res.getStringArray(R.array.keywords_location_placard)
            val surgelatorKeywords = res.getStringArray(R.array.keywords_location_surgele)
            val caveKeywords = res.getStringArray(R.array.keywords_location_cave)

            return when {
                frigoKeywords.any { it in lower } -> iconFrigo
                placardKeywords.any { it in lower } -> iconPlacard
                surgelatorKeywords.any { it in lower } -> iconFreezer
                caveKeywords.any { it in lower } -> iconCave
                else -> throw LogManager.uException(TAG, context.getString(R.string.error_unknown_location_icon, lower))
            }
        }

        @Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
        fun resolveLocationName(
            locationId: Long?,
            locationNamesMap: Map<Long, String>,
            category: String? = null,
            context: Context? = null
        ): String {
            if (locationId != null) {
                val mappedName = locationNamesMap[locationId]
                if (!mappedName.isNullOrBlank()) {
                    return mappedName
                }
            }

            if (context == null) {
                throw LogManager.uException(TAG, "Le contexte fourni est nul lors de la résolution de l'emplacement")
            }

            val cleanCat = if (!category.isNullOrBlank()) {
                category.lowercase().trim()
            } else {
                ""
            }
            val res = context.resources

            val frigoKeywords = res.getStringArray(R.array.keywords_location_frigo)
            val surgelatorKeywords = res.getStringArray(R.array.keywords_location_surgele)
            val caveKeywords = res.getStringArray(R.array.keywords_location_cave)
            val placardKeywords = res.getStringArray(R.array.keywords_location_placard)

            val defaultFrigo = context.getString(R.string.location_default_frigo)
            val defaultSurgele = context.getString(R.string.location_default_surgele)
            val defaultCave = context.getString(R.string.location_default_cave)
            val defaultPlacard = context.getString(R.string.location_default_placard)

            return when {
                frigoKeywords.any { it in cleanCat } -> defaultFrigo
                surgelatorKeywords.any { it in cleanCat } -> defaultSurgele
                caveKeywords.any { it in cleanCat } -> defaultCave
                placardKeywords.any { it in cleanCat } || cleanCat.isEmpty() -> defaultPlacard
                else -> throw LogManager.uException(TAG, context.getString(R.string.error_unknown_location_category, cleanCat))
            }
        }

        fun resolveCategoryName(category: String?, context: Context): String {
            if (category.isNullOrBlank()) {
                return context.getString(R.string.category_default_epicerie)
            }
            return category
        }

        fun resolveSupplementalInfoText(supplementalInfo: String?, context: Context): String? {
            if (supplementalInfo.isNullOrBlank()) return null
            return context.getString(R.string.ingredient_info_format, supplementalInfo)
        }
    }
}
