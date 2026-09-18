package com.buph.srms.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.R
import com.buph.srms.data.ApiClient
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityAdminDashboardBinding
import com.buph.srms.ui.RoleSelectActivity
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.tvWelcome.text = "Welcome, ${session.displayName ?: "Admin"} 👋"

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) {
                session.clearSession()
                startActivity(Intent(this, RoleSelectActivity::class.java))
                finish()
                true
            } else false
        }

        binding.rowStudents.setOnClickListener { startActivity(Intent(this, AdminStudentsActivity::class.java)) }
        binding.rowCourses.setOnClickListener { startActivity(Intent(this, AdminCoursesActivity::class.java)) }
        binding.rowWindow.setOnClickListener { startActivity(Intent(this, AdminRegistrationWindowActivity::class.java)) }
        binding.rowResults.setOnClickListener { startActivity(Intent(this, AdminUploadResultActivity::class.java)) }
        binding.rowMaterials.setOnClickListener { startActivity(Intent(this, AdminMaterialsActivity::class.java)) }
        binding.rowProjects.setOnClickListener { startActivity(Intent(this, AdminProjectsActivity::class.java)) }

        loadStats()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminDashboardActivity)
                val stats = api.adminDashboard().body()?.stats ?: return@launch
                binding.tvStudentsCount.text = stats.total_students.toString()
                binding.tvCoursesCount.text = stats.total_courses.toString()
                binding.tvRegsCount.text = stats.total_registrations.toString()
                binding.tvProjectsCount.text = "${stats.total_projects} (${stats.duplicate_projects})"
            } catch (_: Exception) { }
        }
    }
}
