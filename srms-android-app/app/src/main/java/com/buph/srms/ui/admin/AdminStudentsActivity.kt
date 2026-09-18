package com.buph.srms.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityAdminStudentsBinding
import com.buph.srms.ui.admin.adapters.StudentAdapter
import kotlinx.coroutines.launch

class AdminStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminStudentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerStudents.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener { loadStudents(binding.etSearch.text.toString()) }
        binding.btnSearch.setOnClickListener { loadStudents(binding.etSearch.text.toString()) }

        loadStudents(null)
    }

    override fun onResume() {
        super.onResume()
        loadStudents(binding.etSearch.text?.toString())
    }

    private fun loadStudents(query: String?) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminStudentsActivity)
                val students = api.adminGetStudents(query?.ifBlank { null }).body()?.students ?: emptyList()
                binding.recyclerStudents.adapter = StudentAdapter(students) { student ->
                    val intent = Intent(this@AdminStudentsActivity, AdminStudentDetailActivity::class.java)
                    intent.putExtra("student_id", student.id)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminStudentsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
