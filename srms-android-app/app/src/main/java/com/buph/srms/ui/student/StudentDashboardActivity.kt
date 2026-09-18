package com.buph.srms.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityStudentDashboardBinding
import com.buph.srms.ui.RoleSelectActivity

class StudentDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.tvWelcome.text = "Welcome, ${session.displayName ?: "Student"} 👋"

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == com.buph.srms.R.id.action_logout) {
                session.clearSession()
                startActivity(Intent(this, RoleSelectActivity::class.java))
                finish()
                true
            } else false
        }

        binding.cardRegister.setOnClickListener { startActivity(Intent(this, CourseRegistrationActivity::class.java)) }
        binding.cardProject.setOnClickListener { startActivity(Intent(this, UploadProjectActivity::class.java)) }
        binding.cardResults.setOnClickListener { startActivity(Intent(this, ResultsActivity::class.java)) }
        binding.cardMaterials.setOnClickListener { startActivity(Intent(this, MaterialsActivity::class.java)) }
        binding.cardProfile.setOnClickListener { startActivity(Intent(this, StudentProfileActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        // Re-check profile completion in case it changed
        if (!session.profileComplete) {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }
    }
}
