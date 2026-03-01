package com.example.chiefinventory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ItemSignatureReportBinding
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.utils.SignatureUtils

/**
 * A RecyclerView adapter for displaying a report of items and their image signature status.
 *
 * This adapter highlights the status of each item's signature (Valid, Empty, Missing) using
 * different colors and text, and shows an image thumbnail and signature preview for valid items.
 *
 * @param onItemClicked A lambda function invoked when an item in the list is clicked.
 */
class SignatureReportAdapter(
    private val onItemClicked: (CollectionItem) -> Unit
) : ListAdapter<CollectionItem, SignatureReportAdapter.SignatureViewHolder>(DiffCallback) {

    /**
     * Creates and returns a new [SignatureViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignatureViewHolder {
        val binding = ItemSignatureReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SignatureViewHolder(binding)
    }

    /**
     * Binds the data at the specified position to the [SignatureViewHolder].
     */
    override fun onBindViewHolder(holder: SignatureViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener { onItemClicked(current) }
        holder.bind(current)
    }

    /**
     * ViewHolder for a single item in the signature report.
     * @param binding The view binding for the item layout.
     */
    class SignatureViewHolder(val binding: ItemSignatureReportBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds a [CollectionItem] to the views, setting text and visibility based on signature status.
         * @param item The [CollectionItem] to display.
         */
        fun bind(item: CollectionItem) {
            val context = binding.root.context
            binding.tvRemoteId.text = context.getString(R.string.report_item_id, item.remoteId)
            binding.tvItemTitle.text = item.titre

            binding.ivThumbnail.visibility = View.GONE
            binding.tvSignaturePreview.visibility = View.GONE

            val sigInfo = SignatureUtils.formatSignaturePreview(context, item.imageEmbedding)

            when {
                item.imageEmbedding == null -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_missing)
                    binding.tvSignatureStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error))
                }
                item.imageEmbedding.isEmpty() -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_empty)
                    binding.tvSignatureStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning))
                }
                else -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_valid)
                    binding.tvSignatureStatus.setTextColor(ContextCompat.getColor(context, R.color.status_ok))

                    // Show thumbnail and preview for valid signatures.
                    binding.ivThumbnail.visibility = View.VISIBLE
                    binding.tvSignaturePreview.visibility = View.VISIBLE

                    binding.ivThumbnail.load(item.imageUri) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher)
                        error(R.mipmap.ic_launcher)
                    }

                    binding.tvSignaturePreview.text = sigInfo
                }
            }
        }
    }

    companion object {
        /**
         * A DiffUtil.ItemCallback for calculating the difference between two [CollectionItem]s.
         */
        private val DiffCallback = object : DiffUtil.ItemCallback<CollectionItem>() {
            override fun areItemsTheSame(oldItem: CollectionItem, newItem: CollectionItem):
                    Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CollectionItem, newItem: CollectionItem):
                    Boolean = oldItem == newItem
        }
    }
}
