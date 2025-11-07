package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.model.CategoryInfo

class CategoryAdapter(private val onItemClicked: (String) -> Unit) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private val categories = mutableListOf<CategoryInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(itemView as TextView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val categoryInfo = categories[position]
        holder.bind(categoryInfo)
    }

    override fun getItemCount() = categories.size

    fun submitList(newCategories: List<CategoryInfo>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class VH(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        init {
            textView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(categories[position].name)
                }
            }
        }

        fun bind(categoryInfo: CategoryInfo) {
            textView.text = "${categoryInfo.name} (${categoryInfo.count})"
        }
    }
}