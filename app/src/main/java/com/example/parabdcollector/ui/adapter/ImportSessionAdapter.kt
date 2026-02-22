package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemImportSessionBinding
import com.example.parabdcollector.model.ImportSession
import java.text.SimpleDateFormat
import java.util.*

class ImportSessionAdapter(
    private val onSessionClicked: (ImportSession) -> Unit,
    private val onSessionLongClicked: (ImportSession) -> Unit
) : ListAdapter<ImportSession, ImportSessionAdapter.SessionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemImportSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onSessionClicked(current) }
        holder.itemView.setOnLongClickListener {
            onSessionLongClicked(current)
            true
        }
        holder.bind(current, position)
    }

    class SessionViewHolder(private val binding: ItemImportSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault())

        fun bind(session: ImportSession, position: Int) {
            val context = binding.root.context
            binding.tvFileName.text = session.fileName
            binding.tvDate.text = dateFormat.format(Date(session.timestamp))
            binding.tvStatus.text = session.status
            
            binding.tvAddedCount.text = context.getString(R.string.import_added_format, session.itemsAdded)
            binding.tvUpdatedCount.text = context.getString(R.string.import_updated_format, session.itemsUpdated)

            // Status color styling
            when (session.status) {
                "SUCCESS" -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.status_background)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                }
                "IN_PROGRESS" -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.status_background_sought) // Reuse yellow/orange
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
                else -> { // ERROR
                    binding.tvStatus.setBackgroundResource(R.drawable.status_background_sought) // Should ideally be red
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
            }

            // Zebra striping
            val backgroundColor = if (position % 2 != 0) {
                ContextCompat.getColor(context, R.color.zebra_stripe_background)
            } else {
                ContextCompat.getColor(context, android.R.color.transparent)
            }
            binding.innerLayout.setBackgroundColor(backgroundColor)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ImportSession>() {
            override fun areItemsTheSame(oldItem: ImportSession, newItem: ImportSession): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ImportSession, newItem: ImportSession): Boolean =
                oldItem == newItem
        }
    }
}
