package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.databinding.ItemLocationBinding

class LocationAdapter(
    private val onToggleExpand: (Long) -> Unit,
    private val onEdit: (Long) -> Unit
) : ListAdapter<ExpandableLocation, LocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val current = getItem(position)
        // Le clic sur toute la ligne déclenche le dépliage/repliage
        holder.itemView.setOnClickListener { 
            if (current.hasChildren) { // On ne peut déplier que s'il y a des enfants
                onToggleExpand(current.location.id)
            }
        }
        // Le clic sur l'icône déclenche l'édition
        holder.binding.editIcon.setOnClickListener { 
            onEdit(current.location.id)
        }
        holder.bind(current)
    }

    class LocationViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpandableLocation) {
            binding.locationName.text = item.location.name

            // Gestion de l'indentation
            binding.indentation.updateLayoutParams<ViewGroup.LayoutParams> { 
                width = item.depth * 50 // 50px par niveau de profondeur
            }

            // Afficher le connecteur si l'élément est un enfant
            binding.childIndicator.visibility = if (item.depth > 0) View.VISIBLE else View.GONE

            // Gestion de l'icône de dépliage
            if (item.hasChildren) {
                binding.expandIcon.visibility = View.VISIBLE
                binding.expandIcon.rotation = if (item.isExpanded) 90f else 0f
            } else {
                binding.expandIcon.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ExpandableLocation>() {
            override fun areItemsTheSame(oldItem: ExpandableLocation, newItem: ExpandableLocation):
                    Boolean = oldItem.location.id == newItem.location.id

            override fun areContentsTheSame(oldItem: ExpandableLocation, newItem: ExpandableLocation):
                    Boolean = oldItem == newItem
        }
    }
}
