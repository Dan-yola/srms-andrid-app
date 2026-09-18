package com.buph.srms.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.buph.srms.R
import com.buph.srms.data.SessionManager
import com.buph.srms.ui.admin.AdminDashboardActivity
import com.buph.srms.ui.student.StudentDashboardActivity
import com.buph.srms.ui.student.StudentProfileActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            val target = when {
                !session.isLoggedIn -> RoleSelectActivity::class.java
                session.role == "admin" -> AdminDashboardActivity::class.java
                session.role == "student" && !session.profileComplete -> StudentProfileActivity::class.java
                else -> StudentDashboardActivity::class.java
            }
            startActivity(Intent(this, target))
            finish()
        }, 1200)
    }
}
