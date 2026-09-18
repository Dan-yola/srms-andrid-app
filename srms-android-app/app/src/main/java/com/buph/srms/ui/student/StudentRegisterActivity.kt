package com.buph.srms.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.data.ApiClient
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityStudentRegisterBinding
import kotlinx.coroutines.launch

class StudentRegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentRegisterBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.tvGoLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val matric = binding.etMatric.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (matric.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@StudentRegisterActivity)
                val resp = api.studentRegister(
                    mapOf(
                        "matric_no" to matric,
                        "email" to email,
                        "password" to password,
                        "confirm_password" to confirm
                    )
                )
                val body = resp.body()

                if (resp.isSuccessful && body?.success == true && body.token != null) {
                    session.token = body.token
                    session.role = "student"
                    session.displayName = matric
                    session.profileComplete = false

                    Toast.makeText(this@StudentRegisterActivity, "Account created! Please complete your profile.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@StudentRegisterActivity, StudentProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@StudentRegisterActivity, body?.message ?: "Registration failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentRegisterActivity, "Network error: ${e.message}. Check Server Settings.", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
