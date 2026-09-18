package com.buph.srms.ui.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.data.models.CourseDto
import com.buph.srms.databinding.ItemCourseCheckboxBinding

class CourseSelectAdapter(
    private val courses: List<CourseDto>,
    private val selectedIds: MutableSet<Int>
) : RecyclerView.Adapter<CourseSelectAdapter.VH>() {

    inner class VH(val binding: ItemCourseCheckboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCourseCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val course = courses[position]
        holder.binding.tvCode.text = course.course_code
        holder.binding.tvTitle.text = course.course_title
        holder.binding.tvUnit.text = "${course.unit} Unit(s)"

        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = course.already_registered || selectedIds.contains(course.id)
        holder.binding.checkbox.isEnabled = !course.already_registered

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedIds.add(course.id) else selectedIds.remove(course.id)
        }
    }

    override fun getItemCount() = courses.size
}
