package com.buph.srms.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.databinding.ActivityAdminRegistrationWindowBinding
import com.buph.srms.ui.admin.adapters.WindowAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminRegistrationWindowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRegistrationWindowBinding
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private var openCal: Calendar? = null
    private var closeCal: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRegistrationWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerWindows.layoutManager = LinearLayoutManager(this)

        binding.spinnerSemester.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("First", "Second"))
        binding.spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("ND1", "ND2", "HND1", "HND2"))

        binding.btnPickOpen.setOnClickListener { pickDateTime(true) }
        binding.btnPickClose.setOnClickListener { pickDateTime(false) }
        binding.btnSave.setOnClickListener { saveWindow() }

        loadWindows()
    }

    private fun pickDateTime(isOpen: Boolean) {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, hour, minute, 0)
                if (isOpen) {
                    openCal = cal
                    binding.tvOpenChosen.text = fmt.format(cal.time)
                } else {
                    closeCal = cal
                    binding.tvCloseChosen.text = fmt.format(cal.time)
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadWindows() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminRegistrationWindowActivity)
                val windows = api.adminGetWindows().body()?.windows ?: emptyList()
                binding.recyclerWindows.adapter = WindowAdapter(windows) { window ->
                    toggleWindow(window.id, window.is_open == 0)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminRegistrationWindowActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveWindow() {
        val session = binding.etSession.text.toString().trim()
        val semester = binding.spinnerSemester.selectedItem.toString()
        val level = binding.spinnerLevel.selectedItem.toString()
        val open = openCal
        val close = closeCal

        if (session.isEmpty() || open == null || close == null) {
            Toast.makeText(this, "Please fill in session, and pick both dates.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminRegistrationWindowActivity)
                val body = mapOf(
                    "session" to session, "semester" to semester, "level" to level,
                    "open_date" to fmt.format(open.time), "close_date" to fmt.format(close.time)
                )
                val resp = api.adminSaveWindow(body)
                val result = resp.body()
                if (resp.isSuccessful && result?.success == true) {
                    Toast.makeText(this@AdminRegistrationWindowActivity, result.message ?: "Saved.", Toast.LENGTH_SHORT).show()
                    binding.etSession.setText("")
                    binding.tvOpenChosen.text = ""
                    binding.tvCloseChosen.text = ""
                    openCal = null; closeCal = null
                    loadWindows()
                } else {
                    Toast.makeText(this@AdminRegistrationWindowActivity, result?.message ?: "Could not save window.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminRegistrationWindowActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun toggleWindow(id: Int, setOpen: Boolean) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminRegistrationWindowActivity)
                api.adminToggleWindow(mapOf("id" to id, "set_open" to setOpen))
                loadWindows()
            } catch (e: Exception) {
                Toast.makeText(this@AdminRegistrationWindowActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
