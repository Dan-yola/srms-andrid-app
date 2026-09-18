package com.buph.srms.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityMaterialsBinding
import com.buph.srms.ui.student.adapters.MaterialAdapter
import kotlinx.coroutines.launch

class MaterialsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaterialsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerMaterials.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener { loadMaterials() }

        loadMaterials()
    }

    private fun loadMaterials() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@MaterialsActivity)
                val materials = api.getMaterials().body()?.materials ?: emptyList()

                binding.recyclerMaterials.adapter = MaterialAdapter(materials) { material ->
                    material.file_url?.let { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
                binding.tvEmpty.visibility = if (materials.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(this@MaterialsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
