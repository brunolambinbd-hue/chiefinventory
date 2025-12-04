package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemLocationBinding
import com.example.parabdcollector.ui.model.ExpandableLocation

/**
 * A RecyclerView adapter for displaying a hierarchical list of locations.
 *
 * This adapter is responsible for rendering the tree structure, including indentation
 * and expand/collapse icons, based on the properties of the [ExpandableLocation] items.
 *
 * @param onToggleExpand A lambda function invoked when the user clicks an item to expand or collapse it.
 * @param onEdit A lambda function invoked when the user clicks the edit icon for an item.
 */
class LocationAdapter(
    private val onToggleExpand: (Long) -> Unit,
    private val onEdit: (Long) -> Unit
) : ListAdapter<ExpandableLocation, LocationAdapter.LocationViewHolder>(DiffCallback) {

    /**
     * Creates and returns a new [LocationViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    /**
     * Binds the data at the specified position to the [LocationViewHolder] and sets up click listeners.
     */
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val current = getItem(position)
        // Clicking the whole item toggles expansion if it has children.
        holder.itemView.setOnClickListener { 
            if (current.hasChildren) {
                onToggleExpand(current.location.id)
            }
        }
        // Clicking the edit icon triggers the edit action.
        holder.binding.editIcon.setOnClickListener { 
            onEdit(current.location.id)
        }
        holder.bind(current)
    }

    /**
     * ViewHolder for a single location item, handling the data binding and view manipulation.
     * @param binding The view binding for the item layout.
     */
    class LocationViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds an [ExpandableLocation] item to the views.
         * This function translates the item's properties (depth, expanded state, item count) into visual changes.
         * @param item The data to display.
         */
        fun bind(item: ExpandableLocation) {
            binding.locationName.text = item.location.name

            // Set indentation based on the item's depth in the tree.
            binding.indentation.updateLayoutParams<ViewGroup.LayoutParams> { 
                width = item.depth * 50 // 50px per level of depth
            }

            // Show the connector line if the item is a child.
            binding.childIndicator.visibility = if (item.depth > 0) View.VISIBLE else View.GONE

            // Manage the expand/collapse icon visibility and rotation.
            if (item.hasChildren) {
                binding.expandIcon.visibility = View.VISIBLE
                binding.expandIcon.rotation = if (item.isExpanded) 90f else 0f
            } else {
                binding.expandIcon.visibility = View.INVISIBLE
            }

            // Display the item count if it's greater than zero.
            if (item.itemCount > 0) {
                binding.locationItemCount.visibility = View.VISIBLE
                binding.locationItemCount.text = "(${item.itemCount})"
            } else {
                binding.locationItemCount.visibility = View.GONE
            }
        }
    }

    companion object {
        /**
         * A DiffUtil.ItemCallback for calculating the difference between two [ExpandableLocation] items.
         */
        private val DiffCallback = object : DiffUtil.ItemCallback<ExpandableLocation>() {
            override fun areItemsTheSame(oldItem: ExpandableLocation, newItem: ExpandableLocation):
                    Boolean = oldItem.location.id == newItem.location.id

            override fun areContentsTheSame(oldItem: ExpandableLocation, newItem: ExpandableLocation):
                    Boolean = oldItem == newItem
        }
    }
}
