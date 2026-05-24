package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.FullHierarchyItem
import com.example.parabdcollector.databinding.ItemCollectionPlanCardBinding
import com.google.android.material.chip.Chip

class CollectionPlanAdapter(
    private val onCategoryClicked: (superCat: String, cat: String) -> Unit
) : RecyclerView.Adapter<CollectionPlanAdapter.ViewHolder>() {

    private var groupedData: Map<String, List<FullHierarchyItem>> = emptyMap()
    private var superCategories: List<String> = emptyList()

    fun submitList(list: List<FullHierarchyItem>) {
        groupedData = list.groupBy { it.superCategorie }
        superCategories = groupedData.keys.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCollectionPlanCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val superCat = superCategories[position]
        val categories = groupedData[superCat] ?: emptyList()
        holder.bind(superCat, categories)
    }

    override fun getItemCount(): Int = superCategories.size

    inner class ViewHolder(private val binding: ItemCollectionPlanCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(superCat: String, categories: List<FullHierarchyItem>) {
            binding.tvSuperCategory.text = superCat
            binding.cgCategories.removeAllViews()

            for (item in categories) {
                // Utilisation d'un TextView simple avec taille confort
                val textView = TextView(binding.root.context).apply {
                    text = "${item.categorie} (${item.possessedCount}/${item.totalCount})"
                    textSize = 14f // Taille standard confortable
                    setTypeface(null, android.graphics.Typeface.BOLD) // Gras pour la lisibilité
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    setPadding(16, 10, 16, 10) // Plus d'espace pour cliquer
                    setBackgroundResource(R.drawable.status_background_light)
                    setOnClickListener { onCategoryClicked(superCat, item.categorie) }
                }
                
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8, 8)
                }
                textView.layoutParams = params
                binding.cgCategories.addView(textView)
            }
        }
    }
}
