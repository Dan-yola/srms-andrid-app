package com.buph.srms.ui.student

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
import com.buph.srms.databinding.ActivityUploadProjectBinding
import com.buph.srms.ui.student.adapters.ProjectAdapter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class UploadProjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProjectBinding
    private var myCourses: List<CourseDto> = emptyList()
    private var pickedFileUri: Uri? = null
    private var pickedFileName: String = ""

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedFileUri = uri
            pickedFileName = queryFileName(uri) ?: "selected_file"
            binding.tvFileName.text = pickedFileName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerProjects.layoutManager = LinearLayoutManager(this)

        binding.btnChooseFile.setOnClickListener {
            pickFile.launch("*/*")
        }
        binding.btnUpload.setOnClickListener { doUpload() }

        loadMyCourses()
        loadMyProjects()
    }

    private fun loadMyCourses() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@UploadProjectActivity)
                val resp = api.getMyCourses()
                myCourses = resp.body()?.courses ?: emptyList()

                if (myCourses.isEmpty()) {
                    Toast.makeText(this@UploadProjectActivity, "You need to register for a course before uploading a project.", Toast.LENGTH_LONG).show()
                    binding.btnUpload.isEnabled = false
                } else {
                    val labels = myCourses.map { "${it.course_code} — ${it.course_title}" }
                    binding.spinnerCourse.adapter = ArrayAdapter(this@UploadProjectActivity, android.R.layout.simple_spinner_dropdown_item, labels)
                }
            } catch (e: Exception) {
                Toast.makeText(this@UploadProjectActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMyProjects() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@UploadProjectActivity)
                val resp = api.getMyProjects()
                val projects = resp.body()?.projects ?: emptyList()
                binding.recyclerProjects.adapter = ProjectAdapter(projects)
            } catch (e: Exception) {
                Toast.makeText(this@UploadProjectActivity, "Could not load submissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doUpload() {
        val title = binding.etTitle.text.toString().trim()
        val courseIndex = binding.spinnerCourse.selectedItemPosition

        if (myCourses.isEmpty() || courseIndex < 0) {
            Toast.makeText(this, "No course selected.", Toast.LENGTH_SHORT).show()
            return
        }
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a project title.", Toast.LENGTH_SHORT).show()
            return
        }
        val fileUri = pickedFileUri
        if (fileUri == null) {
            Toast.makeText(this, "Please choose a file to upload.", Toast.LENGTH_SHORT).show()
            return
        }

        val courseId = myCourses[courseIndex].id

        binding.progress.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false

        lifecycleScope.launch {
            try {
                val tempFile = uriToTempFile(fileUri, pickedFileName)
                if (tempFile == null) {
                    Toast.makeText(this@UploadProjectActivity, "Could not read the selected file.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val api = ApiClient.getService(this@UploadProjectActivity)
                val reqFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("project_file", tempFile.name, reqFile)

                val resp = api.uploadProject(
                    courseId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    filePart
                )
                val body = resp.body()

                if (resp.isSuccessful && body?.success == true) {
                    Toast.makeText(this@UploadProjectActivity, body.message ?: "Uploaded.", Toast.LENGTH_LONG).show()
                    binding.etTitle.setText("")
                    binding.tvFileName.text = "No file chosen"
                    pickedFileUri = null
                    loadMyProjects()
                } else {
                    Toast.makeText(this@UploadProjectActivity, body?.message ?: "Upload failed.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UploadProjectActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnUpload.isEnabled = true
            }
        }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) name = cursor.getString(nameIndex)
        }
        return name
    }

    private fun uriToTempFile(uri: Uri, suggestedName: String): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val safeName = if (suggestedName.contains(".")) suggestedName else "$suggestedName.dat"
            val tempFile = File(cacheDir, "upload_${System.currentTimeMillis()}_$safeName")
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
