package com.buph.srms.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buph.srms.databinding.ActivityRoleSelectBinding
import com.buph.srms.ui.admin.AdminLoginActivity
import com.buph.srms.ui.student.StudentLoginActivity

class RoleSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoleSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStudent.setOnClickListener {
            startActivity(Intent(this, StudentLoginActivity::class.java))
        }
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
        binding.btnServerSettings.setOnClickListener {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
        }
    }
}
