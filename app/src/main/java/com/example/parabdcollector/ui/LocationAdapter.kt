package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.databinding.ItemLocationBinding
import com.example.parabdcollector.model.Location

class LocationAdapter(private val onItemClicked: (Location) -> Unit) : ListAdapter<Location, LocationAdapter.LocationViewHolder>(DiffCallback) {

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
        fun bind(location: Location) {
            binding.locationName.text = location.name
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Location>() {
            override fun areItemsTheSame(oldItem: Location, newItem: Location):
                    Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Location, newItem: Location):
                    Boolean = oldItem == newItem
        }
    }
}
