package com.buph.srms.ui.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.R
import com.buph.srms.data.models.StudentSummaryDto
import com.buph.srms.databinding.ItemStudentBinding

class StudentAdapter(
    private val students: List<StudentSummaryDto>,
    private val onClick: (StudentSummaryDto) -> Unit
) : RecyclerView.Adapter<StudentAdapter.VH>() {

    inner class VH(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = students[position]
        holder.binding.tvName.text = s.full_name ?: s.matric_no
        holder.binding.tvMeta.text = "${s.matric_no} • ${s.department ?: "—"} • ${s.level ?: "—"}"

        holder.binding.tvProfileStatus.text = if (s.profile_completed) "Profile Complete" else "Profile Incomplete"
        holder.binding.tvProfileStatus.setBackgroundColor(
            holder.itemView.context.getColor(if (s.profile_completed) R.color.success else R.color.danger)
        )

        holder.binding.tvAccountStatus.text = s.status.replaceFirstChar { it.uppercase() }
        holder.binding.tvAccountStatus.setBackgroundColor(
            holder.itemView.context.getColor(if (s.status == "active") R.color.success else R.color.danger)
        )

        holder.itemView.setOnClickListener { onClick(s) }
    }

    override fun getItemCount() = students.size
}
