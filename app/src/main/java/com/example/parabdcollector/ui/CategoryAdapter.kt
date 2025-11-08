package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.model.CategoryInfo

class CategoryAdapter(private val onItemClicked: (String) -> Unit) : ListAdapter<CategoryInfo, CategoryAdapter.VH>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(itemView as TextView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val categoryInfo = getItem(position)
        holder.bind(categoryInfo)
    }

    inner class VH(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        init {
            textView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position).name)
                }
            }
        }

        fun bind(categoryInfo: CategoryInfo) {
            textView.text = textView.context.getString(R.string.category_item_format, categoryInfo.name, categoryInfo.count)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}