package com.buph.srms.ui.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.data.models.MaterialDto
import com.buph.srms.databinding.ItemMaterialBinding

class MaterialAdapter(
    private val materials: List<MaterialDto>,
    private val onDownload: (MaterialDto) -> Unit
) : RecyclerView.Adapter<MaterialAdapter.VH>() {

    inner class VH(val binding: ItemMaterialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMaterialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = materials[position]
        holder.binding.tvTitle.text = m.title
        holder.binding.tvSubtitle.text = "${m.course_code} • ${m.uploaded_at}"
        holder.binding.btnDownload.setOnClickListener { onDownload(m) }
    }

    override fun getItemCount() = materials.size
}
