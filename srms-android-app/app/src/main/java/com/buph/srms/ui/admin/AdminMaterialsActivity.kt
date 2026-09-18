package com.buph.srms.ui.admin

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.data.models.CourseDto
import com.buph.srms.databinding.ActivityAdminMaterialsBinding
import com.buph.srms.ui.admin.adapters.AdminMaterialAdapter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AdminMaterialsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMaterialsBinding
    private var courses: List<CourseDto> = emptyList()
    private var pickedFileUri: Uri? = null
    private var pickedFileName: String = ""

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedFileUri = uri
            pickedFileName = queryFileName(uri) ?: "material_file"
            binding.tvFileName.text = pickedFileName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerMaterials.layoutManager = LinearLayoutManager(this)
        binding.btnChooseFile.setOnClickListener { pickFile.launch("*/*") }
        binding.btnUpload.setOnClickListener { doUpload() }

        loadCourses()
        loadMaterials()
    }

    private fun loadCourses() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminMaterialsActivity)
                courses = api.adminGetCourses().body()?.courses ?: emptyList()
                val labels = courses.map { "${it.course_code} — ${it.course_title}" }
                binding.spinnerCourse.adapter = ArrayAdapter(this@AdminMaterialsActivity, android.R.layout.simple_spinner_dropdown_item, labels)
            } catch (e: Exception) {
                Toast.makeText(this@AdminMaterialsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMaterials() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminMaterialsActivity)
                val materials = api.adminGetMaterials().body()?.materials ?: emptyList()
                binding.recyclerMaterials.adapter = AdminMaterialAdapter(materials) { material ->
                    deleteMaterial(material.id)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminMaterialsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doUpload() {
        val courseIndex = binding.spinnerCourse.selectedItemPosition
        val title = binding.etTitle.text.toString().trim()
        val fileUri = pickedFileUri

        if (courses.isEmpty() || courseIndex < 0) {
            Toast.makeText(this, "No course selected.", Toast.LENGTH_SHORT).show(); return
        }
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show(); return
        }
        if (fileUri == null) {
            Toast.makeText(this, "Please choose a file.", Toast.LENGTH_SHORT).show(); return
        }

        val courseId = courses[courseIndex].id
        binding.progress.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false

        lifecycleScope.launch {
            try {
                val tempFile = uriToTempFile(fileUri, pickedFileName)
                if (tempFile == null) {
                    Toast.makeText(this@AdminMaterialsActivity, "Could not read file.", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val api = ApiClient.getService(this@AdminMaterialsActivity)
                val reqFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("material_file", tempFile.name, reqFile)

                val resp = api.adminUploadMaterial(
                    courseId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    filePart
                )
                val body = resp.body()
                if (resp.isSuccessful && body?.success == true) {
                    Toast.makeText(this@AdminMaterialsActivity, body.message ?: "Uploaded.", Toast.LENGTH_SHORT).show()
                    binding.etTitle.setText("")
                    binding.tvFileName.text = "No file chosen"
                    pickedFileUri = null
                    loadMaterials()
                } else {
                    Toast.makeText(this@AdminMaterialsActivity, body?.message ?: "Upload failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminMaterialsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnUpload.isEnabled = true
            }
        }
    }

    private fun deleteMaterial(id: Int) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@AdminMaterialsActivity)
                api.adminDeleteMaterial(id)
                Toast.makeText(this@AdminMaterialsActivity, "Material deleted.", Toast.LENGTH_SHORT).show()
                loadMaterials()
            } catch (e: Exception) {
                Toast.makeText(this@AdminMaterialsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) name = cursor.getString(idx)
        }
        return name
    }

    private fun uriToTempFile(uri: Uri, suggestedName: String): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val safeName = if (suggestedName.contains(".")) suggestedName else "$suggestedName.dat"
            val tempFile = File(cacheDir, "material_${System.currentTimeMillis()}_$safeName")
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
