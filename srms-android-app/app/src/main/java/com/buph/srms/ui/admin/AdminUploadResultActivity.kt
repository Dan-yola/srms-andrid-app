package com.buph.srms.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.data.ApiClient
import com.buph.srms.data.models.CourseDto
import com.buph.srms.data.models.StudentSummaryDto
import com.buph.srms.databinding.ActivityAdminUploadResultBinding
import kotlinx.coroutines.launch

class AdminUploadResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUploadResultBinding
    private var students: List<StudentSummaryDto> = emptyList()
    private var courses: List<CourseDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUploadResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveResult() }

        loadStudentsAndCourses()
    }

    private fun loadStudentsAndCourses() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminUploadResultActivity)
                students = api.adminGetStudents().body()?.students ?: emptyList()
                courses = api.adminGetCourses().body()?.courses ?: emptyList()

                val studentLabels = students.map { "${it.matric_no} — ${it.full_name ?: "No name"}" }
                binding.spinnerStudent.adapter = ArrayAdapter(this@AdminUploadResultActivity, android.R.layout.simple_spinner_dropdown_item, studentLabels)

                val courseLabels = courses.map { "${it.course_code} (${it.level}, ${it.semester} Sem, ${it.session})" }
                binding.spinnerCourse.adapter = ArrayAdapter(this@AdminUploadResultActivity, android.R.layout.simple_spinner_dropdown_item, courseLabels)
            } catch (e: Exception) {
                Toast.makeText(this@AdminUploadResultActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveResult() {
        val studentIdx = binding.spinnerStudent.selectedItemPosition
        val courseIdx = binding.spinnerCourse.selectedItemPosition
        val ca = binding.etCaScore.text.toString().trim()
        val exam = binding.etExamScore.text.toString().trim()

        if (studentIdx < 0 || courseIdx < 0 || ca.isEmpty() || exam.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val student = students[studentIdx]
        val course = courses[courseIdx]

        binding.progress.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.tvLastSaved.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminUploadResultActivity)
                val body = mapOf(
                    "student_id" to student.id, "course_id" to course.id,
                    "session" to course.session, "semester" to course.semester,
                    "ca_score" to (ca.toDoubleOrNull() ?: 0.0), "exam_score" to (exam.toDoubleOrNull() ?: 0.0)
                )
                val resp = api.adminUploadResult(body)
                val result = resp.body()
                if (resp.isSuccessful && result?.success == true) {
                    binding.tvLastSaved.visibility = View.VISIBLE
                    binding.tvLastSaved.text = "Saved: Total ${result.total_score}, Grade ${result.grade}"
                    binding.etCaScore.setText("")
                    binding.etExamScore.setText("")
                } else {
                    Toast.makeText(this@AdminUploadResultActivity, result?.message ?: "Could not save result.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUploadResultActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
}
