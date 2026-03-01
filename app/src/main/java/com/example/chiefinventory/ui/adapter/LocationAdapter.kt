package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chiefinventory.databinding.ItemLocationBinding
import com.example.chiefinventory.ui.model.DisplayLocation

class LocationAdapter(
    private val onToggleExpand: (Long) -> Unit,
    private val onEdit: (Long) -> Unit,
    private val onItemCountClick: (Long) -> Unit
) : ListAdapter<DisplayLocation, LocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val current = getItem(position)
        
        holder.binding.expandIcon.setOnClickListener {
            onToggleExpand(current.location.id)
        }

        val navigationClickListener = View.OnClickListener {
            onItemCountClick(current.location.id)
        }
        holder.binding.locationName.setOnClickListener(navigationClickListener)
        holder.binding.locationItemCount.setOnClickListener(navigationClickListener)

        holder.binding.editIcon.setOnClickListener { 
            onEdit(current.location.id)
        }

        holder.itemView.setOnClickListener(null)
        
        holder.bind(current)
    }

    class LocationViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisplayLocation) {
            binding.locationName.text = item.location.name

            binding.indentation.updateLayoutParams<ViewGroup.LayoutParams> { 
                width = item.depth * 50
            }

            binding.childIndicator.visibility = if (item.depth > 0) View.VISIBLE else View.GONE

            // For now, we don't have hasChildren in DisplayLocation. We can improve this later.
            binding.expandIcon.visibility = View.INVISIBLE 

            // We also don't have itemCount directly. This will be updated from the activity.
            binding.locationItemCount.visibility = View.GONE
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
