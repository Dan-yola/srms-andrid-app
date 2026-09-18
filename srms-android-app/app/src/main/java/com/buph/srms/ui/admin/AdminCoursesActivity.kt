package com.buph.srms.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityAdminCoursesBinding
import com.buph.srms.ui.admin.adapters.AdminCourseAdapter
import kotlinx.coroutines.launch

class AdminCoursesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCoursesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerCourses.layoutManager = LinearLayoutManager(this)

        binding.spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("ND1", "ND2", "HND1", "HND2"))
        binding.spinnerSemester.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("First", "Second"))

        binding.btnAdd.setOnClickListener { addCourse() }

        loadCourses()
    }

    private fun loadCourses() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminCoursesActivity)
                val courses = api.adminGetCourses().body()?.courses ?: emptyList()
                binding.recyclerCourses.adapter = AdminCourseAdapter(courses) { course ->
                    deleteCourse(course.id)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminCoursesActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addCourse() {
        val code = binding.etCode.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val unit = binding.etUnit.text.toString().trim().ifEmpty { "2" }
        val dept = binding.etDepartment.text.toString().trim()
        val level = binding.spinnerLevel.selectedItem.toString()
        val semester = binding.spinnerSemester.selectedItem.toString()
        val session = binding.etSession.text.toString().trim()

        if (code.isEmpty() || title.isEmpty() || dept.isEmpty() || session.isEmpty()) {
            Toast.makeText(this, "Please fill in all course fields.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.btnAdd.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminCoursesActivity)
                val body = mapOf(
                    "course_code" to code, "course_title" to title, "unit" to (unit.toIntOrNull() ?: 2),
                    "department" to dept, "level" to level, "semester" to semester, "session" to session
                )
                val resp = api.adminCreateCourse(body)
                val result = resp.body()
                if (resp.isSuccessful && result?.success == true) {
                    Toast.makeText(this@AdminCoursesActivity, result.message ?: "Course added.", Toast.LENGTH_SHORT).show()
                    binding.etCode.setText(""); binding.etTitle.setText(""); binding.etSession.setText("")
                    loadCourses()
                } else {
                    Toast.makeText(this@AdminCoursesActivity, result?.message ?: "Could not add course.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminCoursesActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnAdd.isEnabled = true
            }
        }
    }

    private fun deleteCourse(id: Int) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminCoursesActivity)
                api.adminDeleteCourse(id)
                Toast.makeText(this@AdminCoursesActivity, "Course deleted.", Toast.LENGTH_SHORT).show()
                loadCourses()
            } catch (e: Exception) {
                Toast.makeText(this@AdminCoursesActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
