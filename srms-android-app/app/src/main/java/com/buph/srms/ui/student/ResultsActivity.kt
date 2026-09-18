package com.buph.srms.ui.student

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.buph.srms.data.ApiClient
import com.buph.srms.data.models.AvailableSessionDto
import com.buph.srms.data.models.ResultDto
import com.buph.srms.databinding.ActivityResultsBinding
import com.buph.srms.ui.student.adapters.ResultAdapter
import kotlinx.coroutines.launch

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    private var availableSessions: List<AvailableSessionDto> = emptyList()
    private var currentResults: List<ResultDto> = emptyList()
    private var studentName = ""
    private var studentMatric = ""
    private var studentDept = ""
    private var currentSessionLabel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerResults.layoutManager = LinearLayoutManager(this)
        binding.btnDownload.setOnClickListener { downloadAsPdf() }

        loadProfile()
        loadResults(null, null)
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@ResultsActivity)
                val student = api.getProfile().body()?.student ?: return@launch
                studentName = student.full_name ?: student.matric_no
                studentMatric = student.matric_no
                studentDept = student.department ?: ""
                binding.tvStudentInfo.text = "Name: $studentName\nMatric No: $studentMatric\nDepartment: $studentDept"
            } catch (_: Exception) { }
        }
    }

    private fun loadResults(session: String?, semester: String?) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@ResultsActivity)
                val resp = api.getResults(session, semester).body()

                availableSessions = resp?.available_sessions ?: emptyList()
                currentResults = resp?.results ?: emptyList()
                currentSessionLabel = "${resp?.session ?: ""} — ${resp?.semester ?: ""} Semester"

                if (availableSessions.isNotEmpty() && binding.spinnerSessionSemester.adapter == null) {
                    val labels = availableSessions.map { "${it.session} — ${it.semester} Semester" }
                    binding.spinnerSessionSemester.adapter = ArrayAdapter(this@ResultsActivity, android.R.layout.simple_spinner_dropdown_item, labels)
                    binding.spinnerSessionSemester.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                            val chosen = availableSessions[pos]
                            loadResults(chosen.session, chosen.semester)
                        }
                        override fun onNothingSelected(p: AdapterView<*>?) {}
                    }
                }

                binding.recyclerResults.adapter = ResultAdapter(currentResults)
                binding.tvEmpty.visibility = if (currentResults.isEmpty()) View.VISIBLE else View.GONE
                binding.tvGpa.text = if (currentResults.isNotEmpty())
                    "Total Units: ${resp?.total_units ?: 0}   |   GPA: ${resp?.gpa ?: 0.0}" else ""
                binding.btnDownload.isEnabled = currentResults.isNotEmpty()

            } catch (e: Exception) {
                Toast.makeText(this@ResultsActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Builds a simple HTML result slip and hands it to Android's built-in
     *  print service, which lets the student "Save as PDF" — effectively
     *  downloading their result slip without needing a server-side PDF
     *  generator. */
    private fun downloadAsPdf() {
        val rowsHtml = currentResults.joinToString("") { r ->
            "<tr><td>${r.course_code}</td><td>${r.course_title}</td><td>${r.unit}</td>" +
            "<td>${r.ca_score}</td><td>${r.exam_score}</td><td>${r.total_score}</td><td>${r.grade}</td></tr>"
        }
        val html = """
            <html><body style="font-family:sans-serif;">
            <h2 style="text-align:center;">BINYAMINU USMAN POLYTECHNIC, HADEJIA</h2>
            <p style="text-align:center;">Official Result Slip — $currentSessionLabel</p>
            <p><b>Name:</b> $studentName<br><b>Matric No:</b> $studentMatric<br><b>Department:</b> $studentDept</p>
            <table border="1" cellpadding="6" cellspacing="0" style="width:100%; border-collapse:collapse;">
            <tr style="background:#0F5C3F;color:#fff;"><th>Code</th><th>Title</th><th>Unit</th><th>CA</th><th>Exam</th><th>Total</th><th>Grade</th></tr>
            $rowsHtml
            </table>
            </body></html>
        """.trimIndent()

        val webView = WebView(this)
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                val adapter = webView.createPrintDocumentAdapter("Result_$studentMatric")
                printManager.print("Result_$studentMatric", adapter, PrintAttributes.Builder().build())
            }
        }
    }
}
