package com.buph.srms.ui.admin.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.R
import com.buph.srms.databinding.ItemSimpleLineBinding

data class SimpleRow(
    val line: String,
    val badgeText: String? = null,
    val badgeIsPositive: Boolean = true,
    val fileUrl: String? = null
)

class SimpleLineAdapter(private val rows: List<SimpleRow>) : RecyclerView.Adapter<SimpleLineAdapter.VH>() {

    inner class VH(val binding: ItemSimpleLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSimpleLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = rows[position]
        holder.binding.tvLine.text = row.line

        if (row.badgeText != null) {
            holder.binding.tvBadge.visibility = android.view.View.VISIBLE
            holder.binding.tvBadge.text = row.badgeText
            holder.binding.tvBadge.setBackgroundColor(
                holder.itemView.context.getColor(if (row.badgeIsPositive) R.color.success else R.color.danger)
            )
        } else {
            holder.binding.tvBadge.visibility = android.view.View.GONE
        }

        if (row.fileUrl != null) {
            holder.itemView.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(row.fileUrl)))
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount() = rows.size
}
