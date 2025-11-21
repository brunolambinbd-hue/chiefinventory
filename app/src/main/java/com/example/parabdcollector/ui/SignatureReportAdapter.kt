package com.example.parabdcollector.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ItemSignatureReportBinding
import com.example.parabdcollector.model.CollectionItem
import java.nio.ByteBuffer

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

            when {
                item.imageEmbedding == null -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_missing)
                    binding.tvSignatureStatus.setTextColor(Color.RED)
                }
                item.imageEmbedding.isEmpty() -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_empty)
                    binding.tvSignatureStatus.setTextColor("#FFA500".toColorInt()) // Orange
                }
                else -> {
                    binding.tvSignatureStatus.text = context.getString(R.string.signature_status_valid)
                    binding.tvSignatureStatus.setTextColor("#008000".toColorInt()) // Green
                    
                    // Affichage de la miniature et de l'aperçu
                    binding.ivThumbnail.visibility = View.VISIBLE
                    binding.tvSignaturePreview.visibility = View.VISIBLE

                    binding.ivThumbnail.load(item.imageUri) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher)
                        error(R.mipmap.ic_launcher)
                    }

                    // Conversion des 20 premiers bytes en 5 floats pour l'aperçu
                    val byteBuffer = ByteBuffer.wrap(item.imageEmbedding)
                    val preview = (1..5).map { "%.2f".format(byteBuffer.float) }.joinToString(", ")
                    binding.tvSignaturePreview.text = context.getString(R.string.signature_preview_format, preview)
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
