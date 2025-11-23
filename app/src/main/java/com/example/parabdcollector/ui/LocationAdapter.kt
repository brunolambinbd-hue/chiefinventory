package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.databinding.ItemLocationBinding

class LocationAdapter(private val onItemClicked: (DisplayLocation) -> Unit) : ListAdapter<DisplayLocation, LocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current)
    }

    class LocationViewHolder(private val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(displayLocation: DisplayLocation) {
            binding.locationName.text = displayLocation.location.name

            // Ajout de l'indentation
            val indentation = (displayLocation.depth * 50) // 50px par niveau de profondeur
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = indentation
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DisplayLocation>() {
            override fun areItemsTheSame(oldItem: DisplayLocation, newItem: DisplayLocation):
                    Boolean = oldItem.location.id == newItem.location.id

            override fun areContentsTheSame(oldItem: DisplayLocation, newItem: DisplayLocation):
                    Boolean = oldItem == newItem
        }
    }
}
