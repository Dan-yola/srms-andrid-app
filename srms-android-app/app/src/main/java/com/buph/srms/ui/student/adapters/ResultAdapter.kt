package com.buph.srms.ui.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.data.models.ResultDto
import com.buph.srms.databinding.ItemResultBinding

class ResultAdapter(private val results: List<ResultDto>) : RecyclerView.Adapter<ResultAdapter.VH>() {

    inner class VH(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = results[position]
        holder.binding.tvCourse.text = "${r.course_code} — ${r.course_title} (${r.unit} unit${if (r.unit == 1) "" else "s"})"
        holder.binding.tvScores.text = "CA: ${r.ca_score}  |  Exam: ${r.exam_score}  |  Total: ${r.total_score}"
        holder.binding.tvGrade.text = r.grade
    }

    override fun getItemCount() = results.size
}
