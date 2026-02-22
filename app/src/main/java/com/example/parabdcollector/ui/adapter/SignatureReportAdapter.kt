package com.example.parabdcollector.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.SignatureReportItem
import com.example.parabdcollector.databinding.ItemSignatureReportBinding

/**
 * A RecyclerView adapter for displaying a report of items and their image signature status.
 */
class SignatureReportAdapter(
    private val onItemClicked: (SignatureReportItem) -> Unit
) : ListAdapter<SignatureReportItem, SignatureReportAdapter.SignatureViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignatureViewHolder {
        val binding = ItemSignatureReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SignatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SignatureViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current)
    }

    class SignatureViewHolder(val binding: ItemSignatureReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SignatureReportItem) {
            val context = binding.root.context
            binding.tvRemoteId.text = context.getString(R.string.report_item_id, item.id)
            binding.tvItemTitle.text = item.titre

            binding.ivThumbnail.visibility = View.GONE
            binding.tvSignaturePreview.visibility = View.GONE

            if (item.hasEmbedding) {
                binding.tvSignatureStatus.text = context.getString(R.string.signature_status_valid)
                binding.tvSignatureStatus.setTextColor(ContextCompat.getColor(context, R.color.status_ok))

                binding.ivThumbnail.visibility = View.VISIBLE
                binding.ivThumbnail.load(item.imageUri) {
                    crossfade(true)
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                }
            } else {
                binding.tvSignatureStatus.text = context.getString(R.string.signature_status_missing)
                binding.tvSignatureStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error))
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SignatureReportItem>() {
            override fun areItemsTheSame(oldItem: SignatureReportItem, newItem: SignatureReportItem):
                    Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SignatureReportItem, newItem: SignatureReportItem):
                    Boolean = oldItem == newItem
        }
    }
}
