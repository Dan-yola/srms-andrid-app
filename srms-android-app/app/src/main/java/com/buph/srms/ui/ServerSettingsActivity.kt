package com.buph.srms.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityServerSettingsBinding

class ServerSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServerSettingsBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.etServerUrl.setText(session.baseUrl)

        binding.btnSave.setOnClickListener {
            var url = binding.etServerUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!url.endsWith("/")) url += "/"
            session.baseUrl = url
            Toast.makeText(this, "Server URL saved.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
