package com.buph.srms.ui.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.data.models.CourseDto
import com.buph.srms.databinding.ItemAdminCourseBinding

class AdminCourseAdapter(
    private val courses: List<CourseDto>,
    private val onDelete: (CourseDto) -> Unit
) : RecyclerView.Adapter<AdminCourseAdapter.VH>() {

    inner class VH(val binding: ItemAdminCourseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAdminCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = courses[position]
        holder.binding.tvCode.text = c.course_code
        holder.binding.tvTitle.text = c.course_title
        holder.binding.tvMeta.text = "${c.level} • ${c.semester} Semester • ${c.session} • ${c.unit} Unit(s)"
        holder.binding.btnDelete.setOnClickListener { onDelete(c) }
    }

    override fun getItemCount() = courses.size
}
