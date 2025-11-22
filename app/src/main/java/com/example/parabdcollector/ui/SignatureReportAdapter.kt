package com.example.parabdcollector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemSignatureReportBinding
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.SignatureUtils

class SignatureReportAdapter(
    private val onItemClicked: (CollectionItem) -> Unit
) : ListAdapter<CollectionItem, SignatureReportAdapter.SignatureViewHolder>(DiffCallback) {

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
                    
                    // Affichage de la miniature et de l'aperçu
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
        private val DiffCallback = object : DiffUtil.ItemCallback<CollectionItem>() {
            override fun areItemsTheSame(oldItem: CollectionItem, newItem: CollectionItem):
                    Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CollectionItem, newItem: CollectionItem):
                    Boolean = oldItem == newItem
        }
    }
}
