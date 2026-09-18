package com.buph.srms.ui.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.R
import com.buph.srms.data.models.AdminWindowDto
import com.buph.srms.databinding.ItemWindowBinding

class WindowAdapter(
    private val windows: List<AdminWindowDto>,
    private val onToggle: (AdminWindowDto) -> Unit
) : RecyclerView.Adapter<WindowAdapter.VH>() {

    inner class VH(val binding: ItemWindowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWindowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val w = windows[position]
        holder.binding.tvTitle.text = "${w.level} — ${w.semester} Semester, ${w.session}"
        holder.binding.tvRange.text = "${w.open_date} → ${w.close_date}"

        holder.binding.tvStatus.text = if (w.currently_open) "Open" else "Closed"
        holder.binding.tvStatus.setBackgroundColor(
            holder.itemView.context.getColor(if (w.currently_open) R.color.success else R.color.danger)
        )

        holder.binding.btnToggle.text = if (w.is_open == 1) "Close" else "Reopen"
        holder.binding.btnToggle.setOnClickListener { onToggle(w) }
    }

    override fun getItemCount() = windows.size
}
