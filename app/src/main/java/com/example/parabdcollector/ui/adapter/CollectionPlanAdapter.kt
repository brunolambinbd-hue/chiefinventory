package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.FullHierarchyItem
import com.example.parabdcollector.databinding.ItemCollectionPlanCardBinding

class CollectionPlanAdapter(
    private val onCategoryClicked: (superCat: String, cat: String) -> Unit
) : ListAdapter<CollectionPlanAdapter.SuperCategoryGroup, CollectionPlanAdapter.ViewHolder>(DiffCallback) {

    /**
     * Représente un groupe de catégories pour une Super-Catégorie donnée.
     * Utilisé pour permettre à DiffUtil de comparer les données efficacement.
     */
    data class SuperCategoryGroup(
        val name: String,
        val items: List<FullHierarchyItem>
    )

    /**
     * Transforme la liste brute du DAO en liste groupée et la soumet à l'adapteur.
     */
    fun submitFullList(list: List<FullHierarchyItem>) {
        val grouped = list.groupBy { it.superCategorie }
            .map { SuperCategoryGroup(it.key, it.value) }
        submitList(grouped)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCollectionPlanCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group.name, group.items)
    }

    inner class ViewHolder(private val binding: ItemCollectionPlanCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(superCat: String, categories: List<FullHierarchyItem>) {
            binding.tvSuperCategory.text = superCat
            binding.cgCategories.removeAllViews()

            for (item in categories) {
                val textView = TextView(binding.root.context).apply {
                    text = context.getString(R.string.collection_plan_category_format, item.categorie, item.possessedCount, item.totalCount)
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    setPadding(16, 10, 16, 10)
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

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SuperCategoryGroup>() {
            override fun areItemsTheSame(oldItem: SuperCategoryGroup, newItem: SuperCategoryGroup): Boolean {
                // Identité basée sur le nom de la Super-Catégorie
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: SuperCategoryGroup, newItem: SuperCategoryGroup): Boolean {
                // Comparaison du contenu (grâce au data class SuperCategoryGroup)
                return oldItem == newItem
            }
        }
    }
}