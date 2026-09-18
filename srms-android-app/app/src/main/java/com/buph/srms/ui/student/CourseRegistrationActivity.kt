package com.buph.srms.ui.student

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.data.models.CourseDto
import com.buph.srms.databinding.ActivityCourseRegistrationBinding
import com.buph.srms.ui.student.adapters.CourseSelectAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CourseRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseRegistrationBinding
    private val selectedIds = mutableSetOf<Int>()
    private var courses: List<CourseDto> = emptyList()

    private var openDateMs: Long = 0
    private var closeDateMs: Long = 0
    private var windowExists = false
    private var windowOpenNow = false

    private val tickHandler = Handler(Looper.getMainLooper())
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val tickRunnable = object : Runnable {
        override fun run() {
            updateCountdown()
            tickHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.spinnerSemester.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("First", "Second"))

        binding.recyclerCourses.layoutManager = LinearLayoutManager(this)
        binding.btnRegister.setOnClickListener { doRegister() }

        val reloadListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { loadWindowAndCourses() }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        binding.spinnerSession.onItemSelectedListener = reloadListener
        binding.spinnerSemester.onItemSelectedListener = reloadListener

        loadSessions()
    }

    private fun loadSessions() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@CourseRegistrationActivity)
                val resp = api.getSessions()
                val windows = resp.body()?.windows ?: emptyList()
                val sessions = windows.map { it.session }.distinct()

                if (sessions.isEmpty()) {
                    Toast.makeText(this@CourseRegistrationActivity, "No registration window has been configured yet for your level.", Toast.LENGTH_LONG).show()
                    return@launch
                }
                binding.spinnerSession.adapter = ArrayAdapter(this@CourseRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, sessions)
                loadWindowAndCourses()
            } catch (e: Exception) {
                Toast.makeText(this@CourseRegistrationActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun currentSession(): String = binding.spinnerSession.selectedItem?.toString() ?: ""
    private fun currentSemester(): String = binding.spinnerSemester.selectedItem?.toString() ?: "First"

    private fun loadWindowAndCourses() {
        val session = currentSession()
        val semester = currentSemester()
        if (session.isEmpty()) return

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@CourseRegistrationActivity)

                val winResp = api.getRegistrationWindow(session, semester).body()
                windowExists = winResp?.exists == true
                windowOpenNow = winResp?.open == true

                if (windowExists) {
                    openDateMs = parseDate(winResp?.open_date)
                    closeDateMs = parseDate(winResp?.close_date)
                    binding.tvWindowRange.text = "Window: ${winResp?.open_date} → ${winResp?.close_date}"
                    tickHandler.removeCallbacks(tickRunnable)
                    tickHandler.post(tickRunnable)
                } else {
                    binding.tvWindowRange.text = "No registration window configured for this session/semester."
                    binding.tvCdStatus.text = ""
                    binding.tvOpenBadge.text = ""
                }

                val courseResp = api.getCourses(session, semester).body()
                courses = courseResp?.courses ?: emptyList()
                selectedIds.clear()
                binding.recyclerCourses.adapter = CourseSelectAdapter(courses, selectedIds)
                binding.tvEmpty.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
                binding.btnRegister.isEnabled = windowOpenNow && courses.isNotEmpty()

            } catch (e: Exception) {
                Toast.makeText(this@CourseRegistrationActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseDate(s: String?): Long {
        if (s.isNullOrEmpty()) return 0
        return try { fmt.parse(s)?.time ?: 0 } catch (e: Exception) { 0 }
    }

    private fun updateCountdown() {
        if (!windowExists) return
        val now = System.currentTimeMillis()
        val target: Long
        val label: String

        when {
            now < openDateMs -> { target = openDateMs; label = "Registration opens in:" }
            now <= closeDateMs -> { target = closeDateMs; label = "Registration closes in:" }
            else -> {
                binding.tvCdStatus.text = "Registration window has closed."
                binding.tvDays.text = "00"; binding.tvHours.text = "00"; binding.tvMins.text = "00"; binding.tvSecs.text = "00"
                binding.tvOpenBadge.text = "CLOSED"
                binding.tvOpenBadge.setBackgroundColor(getColor(com.buph.srms.R.color.danger))
                binding.tvOpenBadge.setTextColor(getColor(android.R.color.white))
                tickHandler.removeCallbacks(tickRunnable)
                return
            }
        }

        binding.tvCdStatus.text = label
        val diff = (target - now).coerceAtLeast(0)
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24
        val mins = (diff / (1000 * 60)) % 60
        val secs = (diff / 1000) % 60

        binding.tvDays.text = "%02d".format(days)
        binding.tvHours.text = "%02d".format(hours)
        binding.tvMins.text = "%02d".format(mins)
        binding.tvSecs.text = "%02d".format(secs)

        if (windowOpenNow) {
            binding.tvOpenBadge.text = "OPEN"
            binding.tvOpenBadge.setBackgroundColor(getColor(com.buph.srms.R.color.success))
        } else {
            binding.tvOpenBadge.text = "CLOSED"
            binding.tvOpenBadge.setBackgroundColor(getColor(com.buph.srms.R.color.danger))
        }
        binding.tvOpenBadge.setTextColor(getColor(android.R.color.white))
    }

    private fun doRegister() {
        if (!windowOpenNow) {
            Toast.makeText(this, "Registration is not currently open.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one course.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@CourseRegistrationActivity)
                val body = mapOf(
                    "session" to currentSession(),
                    "semester" to currentSemester(),
                    "course_ids" to selectedIds.toList()
                )
                val resp = api.registerCourses(body)
                val result = resp.body()

                if (resp.isSuccessful && result?.success == true) {
                    binding.tvRrr.visibility = View.VISIBLE
                    binding.tvRrr.text = "✅ Registered! Your Remita Reference (RRR): ${result.remita_rrr}\nAccount: ${result.remita_account}"
                    Toast.makeText(this@CourseRegistrationActivity, result.message, Toast.LENGTH_LONG).show()
                    loadWindowAndCourses()
                } else {
                    Toast.makeText(this@CourseRegistrationActivity, result?.message ?: "Registration failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CourseRegistrationActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnRegister.isEnabled = windowOpenNow
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tickHandler.removeCallbacks(tickRunnable)
    }
}
