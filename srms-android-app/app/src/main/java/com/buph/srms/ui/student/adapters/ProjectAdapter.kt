package com.buph.srms.ui.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.R
import com.buph.srms.data.models.ProjectDto
import com.buph.srms.databinding.ItemProjectBinding

class ProjectAdapter(private val projects: List<ProjectDto>) : RecyclerView.Adapter<ProjectAdapter.VH>() {

    inner class VH(val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = projects[position]
        holder.binding.tvTitle.text = p.title
        holder.binding.tvCourse.text = p.course_code
        if (p.is_duplicate) {
            holder.binding.tvStatus.text = "⚠ Flagged Duplicate"
            holder.binding.tvStatus.setBackgroundColor(holder.itemView.context.getColor(R.color.danger))
        } else {
            holder.binding.tvStatus.text = "Received"
            holder.binding.tvStatus.setBackgroundColor(holder.itemView.context.getColor(R.color.success))
        }
        holder.binding.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
    }

    override fun getItemCount() = projects.size
}
