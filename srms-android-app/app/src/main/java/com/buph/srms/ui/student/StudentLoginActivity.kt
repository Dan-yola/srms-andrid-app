package com.buph.srms.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.data.ApiClient
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityStudentLoginBinding
import kotlinx.coroutines.launch

class StudentLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, StudentRegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val matric = binding.etMatric.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (matric.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter matric number and password.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@StudentLoginActivity)
                val resp = api.studentLogin(mapOf("matric_no" to matric, "password" to password))
                val body = resp.body()

                if (resp.isSuccessful && body?.success == true && body.token != null && body.student != null) {
                    session.token = body.token
                    session.role = "student"
                    session.displayName = body.student.full_name ?: body.student.matric_no
                    session.profileComplete = body.student.profile_completed

                    val next = if (body.student.profile_completed) StudentDashboardActivity::class.java
                               else StudentProfileActivity::class.java
                    startActivity(Intent(this@StudentLoginActivity, next))
                    finish()
                } else {
                    Toast.makeText(this@StudentLoginActivity, body?.message ?: "Login failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentLoginActivity, "Network error: ${e.message}. Check Server Settings.", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
