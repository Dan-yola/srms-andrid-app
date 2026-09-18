package com.buph.srms.ui.admin.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.R
import com.buph.srms.data.models.AdminProjectDto
import com.buph.srms.databinding.ItemAdminProjectBinding

class AdminProjectAdapter(private val projects: List<AdminProjectDto>) : RecyclerView.Adapter<AdminProjectAdapter.VH>() {

    inner class VH(val binding: ItemAdminProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAdminProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = projects[position]
        holder.binding.tvTitle.text = "${p.full_name} (${p.matric_no})"
        holder.binding.tvMeta.text = "${p.course_code} — ${p.title}\nUploaded: ${p.uploaded_at}"

        holder.binding.tvStatus.text = if (p.is_duplicate) "⚠ Duplicate" else "Unique"
        holder.binding.tvStatus.setBackgroundColor(
            holder.itemView.context.getColor(if (p.is_duplicate) R.color.danger else R.color.success)
        )

        holder.binding.tvView.setOnClickListener {
            p.file_url?.let { url ->
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    override fun getItemCount() = projects.size
}
