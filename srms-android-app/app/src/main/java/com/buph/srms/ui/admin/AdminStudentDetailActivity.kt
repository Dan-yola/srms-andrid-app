package com.buph.srms.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.R
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityAdminStudentDetailBinding
import com.buph.srms.ui.admin.adapters.SimpleLineAdapter
import com.buph.srms.ui.admin.adapters.SimpleRow
import kotlinx.coroutines.launch

class AdminStudentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminStudentDetailBinding
    private var studentId = 0
    private var currentStatus = "active"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getIntExtra("student_id", 0)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerRegistrations.layoutManager = LinearLayoutManager(this)
        binding.recyclerResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerProjects.layoutManager = LinearLayoutManager(this)

        binding.btnToggleStatus.setOnClickListener { toggleStatus() }

        loadDetail()
    }

    private fun loadDetail() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminStudentDetailActivity)
                val resp = api.adminGetStudentDetail(studentId).body() ?: return@launch
                val student = resp.student ?: return@launch

                currentStatus = student.status
                binding.tvName.text = student.full_name ?: student.matric_no
                binding.tvDetails.text = "Matric No: ${student.matric_no}\nEmail: ${student.email}\nDepartment: ${student.department ?: "—"}\nLevel: ${student.level ?: "—"}"

                binding.tvStatus.text = currentStatus.replaceFirstChar { it.uppercase() }
                binding.tvStatus.setBackgroundColor(getColor(if (currentStatus == "active") R.color.success else R.color.danger))
                binding.btnToggleStatus.text = if (currentStatus == "active") "Suspend Student" else "Reactivate Student"

                val regRows = resp.registrations.map {
                    SimpleRow("${it.course_code} — ${it.session}, ${it.semester} Semester\nRRR: ${it.remita_rrr}")
                }
                binding.recyclerRegistrations.adapter = SimpleLineAdapter(regRows)
                binding.tvNoRegs.visibility = if (regRows.isEmpty()) View.VISIBLE else View.GONE

                val resultRows = resp.results.map {
                    SimpleRow(
                        "${it.course_code} — ${it.session}, ${it.semester} Semester\nCA: ${it.ca_score}  Exam: ${it.exam_score}  Total: ${it.total_score}",
                        badgeText = it.grade, badgeIsPositive = true
                    )
                }
                binding.recyclerResults.adapter = SimpleLineAdapter(resultRows)
                binding.tvNoResults.visibility = if (resultRows.isEmpty()) View.VISIBLE else View.GONE

                val projectRows = resp.projects.map {
                    SimpleRow(
                        "${it.course_code} — ${it.title}\nUploaded: ${it.uploaded_at}",
                        badgeText = if (it.is_duplicate) "Duplicate" else "Unique",
                        badgeIsPositive = !it.is_duplicate,
                        fileUrl = it.file_url
                    )
                }
                binding.recyclerProjects.adapter = SimpleLineAdapter(projectRows)
                binding.tvNoProjects.visibility = if (projectRows.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Toast.makeText(this@AdminStudentDetailActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleStatus() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminStudentDetailActivity)
                val resp = api.adminToggleStudentStatus(mapOf("id" to studentId, "toggle_status" to true))
                val body = resp.body()
                if (resp.isSuccessful && body?.success == true) {
                    Toast.makeText(this@AdminStudentDetailActivity, body.message ?: "Status updated.", Toast.LENGTH_SHORT).show()
                    loadDetail()
                } else {
                    Toast.makeText(this@AdminStudentDetailActivity, body?.message ?: "Could not update status.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminStudentDetailActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
