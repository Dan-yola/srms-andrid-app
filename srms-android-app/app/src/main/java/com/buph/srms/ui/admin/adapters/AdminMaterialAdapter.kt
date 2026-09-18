package com.buph.srms.ui.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.buph.srms.data.models.MaterialDto
import com.buph.srms.databinding.ItemAdminMaterialBinding

class AdminMaterialAdapter(
    private val materials: List<MaterialDto>,
    private val onDelete: (MaterialDto) -> Unit
) : RecyclerView.Adapter<AdminMaterialAdapter.VH>() {

    inner class VH(val binding: ItemAdminMaterialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAdminMaterialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = materials[position]
        holder.binding.tvTitle.text = m.title
        holder.binding.tvSubtitle.text = "${m.course_code} • ${m.uploaded_at}"
        holder.binding.btnDelete.setOnClickListener { onDelete(m) }
    }

    override fun getItemCount() = materials.size
}
