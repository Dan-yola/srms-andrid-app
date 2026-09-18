package com.buph.srms.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.data.ApiClient
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityAdminLoginBinding
import kotlinx.coroutines.launch

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = android.view.View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminLoginActivity)
                val resp = api.adminLogin(mapOf("username" to username, "password" to password))
                val body = resp.body()

                if (resp.isSuccessful && body?.success == true && body.token != null && body.admin != null) {
                    session.token = body.token
                    session.role = "admin"
                    session.displayName = body.admin.full_name
                    startActivity(Intent(this@AdminLoginActivity, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@AdminLoginActivity, body?.message ?: "Login failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminLoginActivity, "Network error: ${e.message}. Check Server Settings.", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = android.view.View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
