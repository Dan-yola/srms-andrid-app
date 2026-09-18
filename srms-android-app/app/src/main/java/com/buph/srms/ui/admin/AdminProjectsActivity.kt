package com.buph.srms.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityAdminProjectsBinding
import com.buph.srms.ui.admin.adapters.AdminProjectAdapter
import kotlinx.coroutines.launch

class AdminProjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminProjectsBinding
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerProjects.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener { loadProjects() }

        binding.btnAll.setOnClickListener {
            currentFilter = "all"
            binding.btnAll.setBackgroundColor(getColor(com.buph.srms.R.color.primary))
            binding.btnDuplicates.setBackgroundColor(getColor(android.R.color.transparent))
            loadProjects()
        }
        binding.btnDuplicates.setOnClickListener {
            currentFilter = "duplicate"
            loadProjects()
        }

        loadProjects()
    }

    private fun loadProjects() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminProjectsActivity)
                val projects = api.adminGetProjects(currentFilter).body()?.projects ?: emptyList()
                binding.recyclerProjects.adapter = AdminProjectAdapter(projects)
                binding.tvEmpty.visibility = if (projects.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(this@AdminProjectsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
