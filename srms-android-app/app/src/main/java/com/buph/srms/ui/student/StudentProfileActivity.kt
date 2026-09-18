package com.buph.srms.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.buph.srms.data.ApiClient
import com.buph.srms.data.SessionManager
import com.buph.srms.databinding.ActivityStudentProfileBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class StudentProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentProfileBinding
    private lateinit var session: SessionManager
    private var pickedPhotoUri: Uri? = null
    private var matricNo: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedPhotoUri = uri
            Picasso.get().load(uri).into(binding.ivPhoto)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        val levels = arrayOf("ND1", "ND2", "HND1", "HND2")
        binding.spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)

        binding.btnChoosePhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.btnSave.setOnClickListener { saveProfile() }

        loadProfile()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@StudentProfileActivity)
                val resp = api.getProfile()
                val student = resp.body()?.student ?: return@launch

                matricNo = student.matric_no
                binding.tvMatric.text = student.matric_no
                binding.etFullName.setText(student.full_name ?: "")
                binding.etPhone.setText(student.phone ?: "")
                binding.etDepartment.setText(student.department ?: "")
                student.level?.let { lvl ->
                    val idx = (binding.spinnerLevel.adapter as ArrayAdapter<String>).getPosition(lvl)
                    if (idx >= 0) binding.spinnerLevel.setSelection(idx)
                }
                if (!student.passport_photo_url.isNullOrEmpty()) {
                    Picasso.get().load(student.passport_photo_url).into(binding.ivPhoto)
                }
                binding.tvBanner.visibility = if (student.profile_completed) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this@StudentProfileActivity, "Could not load profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val department = binding.etDepartment.text.toString().trim()
        val level = binding.spinnerLevel.selectedItem?.toString() ?: "ND1"

        if (fullName.isEmpty() || phone.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@StudentProfileActivity)

                var photoPart: MultipartBody.Part? = null
                pickedPhotoUri?.let { uri ->
                    val tempFile = uriToTempFile(uri)
                    if (tempFile != null) {
                        val reqFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        photoPart = MultipartBody.Part.createFormData("passport_photo", tempFile.name, reqFile)
                    }
                }

                val resp = api.completeProfile(
                    fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
                    phone.toRequestBody("text/plain".toMediaTypeOrNull()),
                    department.toRequestBody("text/plain".toMediaTypeOrNull()),
                    level.toRequestBody("text/plain".toMediaTypeOrNull()),
                    photoPart
                )
                val body = resp.body()

                if (resp.isSuccessful && body?.success == true) {
                    session.profileComplete = true
                    session.displayName = fullName
                    Toast.makeText(this@StudentProfileActivity, "Profile saved.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@StudentProfileActivity, StudentDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@StudentProfileActivity, body?.message ?: "Could not save profile.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentProfileActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
    }
}
