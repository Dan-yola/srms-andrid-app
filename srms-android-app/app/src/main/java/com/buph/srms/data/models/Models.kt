package com.buph.srms.data.models

// ---------- Common ----------
data class SimpleResponse(
    val success: Boolean,
    val message: String? = null
)

// ---------- Student ----------
data class StudentDto(
    val id: Int,
    val matric_no: String,
    val email: String,
    val full_name: String?,
    val phone: String?,
    val department: String?,
    val level: String?,
    val passport_photo_url: String?,
    val profile_completed: Boolean
)

data class StudentAuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val student: StudentDto? = null
)

data class ProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val student: StudentDto? = null
)

// ---------- Registration window / courses ----------
data class WindowDto(
    val session: String,
    val semester: String,
    val open_date: String,
    val close_date: String,
    val is_open: Int
)

data class SessionsResponse(
    val success: Boolean,
    val windows: List<WindowDto> = emptyList()
)

data class RegistrationWindowResponse(
    val success: Boolean,
    val exists: Boolean = false,
    val open: Boolean = false,
    val open_date: String? = null,
    val close_date: String? = null,
    val server_time: String? = null,
    val message: String? = null
)

data class CourseDto(
    val id: Int,
    val course_code: String,
    val course_title: String,
    val unit: Int,
    val department: String,
    val level: String,
    val semester: String,
    val session: String,
    val already_registered: Boolean = false
)

data class CoursesResponse(
    val success: Boolean,
    val courses: List<CourseDto> = emptyList(),
    val message: String? = null
)

data class RegisterCoursesResponse(
    val success: Boolean,
    val message: String? = null,
    val remita_rrr: String? = null,
    val remita_account: String? = null,
    val registered_course_ids: List<Int> = emptyList()
)

// ---------- Projects ----------
data class ProjectDto(
    val id: Int,
    val course_code: String,
    val title: String,
    val is_duplicate: Boolean,
    val uploaded_at: String,
    val file_url: String?
)

data class ProjectsResponse(
    val success: Boolean,
    val projects: List<ProjectDto> = emptyList()
)

data class UploadProjectResponse(
    val success: Boolean,
    val is_duplicate: Boolean = false,
    val message: String? = null
)

// ---------- Results ----------
data class ResultDto(
    val course_code: String,
    val course_title: String,
    val unit: Int,
    val ca_score: Double,
    val exam_score: Double,
    val total_score: Double,
    val grade: String
)

data class AvailableSessionDto(
    val session: String,
    val semester: String
)

data class ResultsResponse(
    val success: Boolean,
    val session: String? = null,
    val semester: String? = null,
    val available_sessions: List<AvailableSessionDto> = emptyList(),
    val results: List<ResultDto> = emptyList(),
    val total_units: Int = 0,
    val gpa: Double = 0.0
)

// ---------- Materials ----------
data class MaterialDto(
    val id: Int,
    val course_code: String,
    val title: String,
    val uploaded_at: String,
    val file_url: String?
)

data class MaterialsResponse(
    val success: Boolean,
    val materials: List<MaterialDto> = emptyList()
)

// ---------- Admin ----------
data class AdminDto(
    val id: Int,
    val username: String,
    val full_name: String
)

data class AdminAuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val admin: AdminDto? = null
)

data class StatsDto(
    val total_students: Int,
    val total_courses: Int,
    val total_registrations: Int,
    val total_projects: Int,
    val duplicate_projects: Int
)

data class AdminDashboardResponse(
    val success: Boolean,
    val stats: StatsDto? = null
)

data class StudentSummaryDto(
    val id: Int,
    val matric_no: String,
    val full_name: String?,
    val email: String,
    val department: String?,
    val level: String?,
    val profile_completed: Boolean,
    val status: String
)

data class AdminStudentsResponse(
    val success: Boolean,
    val students: List<StudentSummaryDto> = emptyList()
)

data class RegistrationRowDto(
    val course_code: String,
    val course_title: String,
    val session: String,
    val semester: String,
    val remita_rrr: String
)

data class ResultRowDto(
    val course_code: String,
    val course_title: String,
    val session: String,
    val semester: String,
    val ca_score: String,
    val exam_score: String,
    val total_score: String,
    val grade: String
)

data class AdminProjectRowDto(
    val id: Int,
    val course_code: String,
    val title: String,
    val uploaded_at: String,
    val is_duplicate: Boolean,
    val file_url: String?
)

data class AdminStudentDetailResponse(
    val success: Boolean,
    val student: StudentSummaryDto? = null,
    val registrations: List<RegistrationRowDto> = emptyList(),
    val results: List<ResultRowDto> = emptyList(),
    val projects: List<AdminProjectRowDto> = emptyList()
)

data class ToggleStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val status: String? = null
)

data class AdminWindowDto(
    val id: Int,
    val session: String,
    val semester: String,
    val level: String,
    val open_date: String,
    val close_date: String,
    val is_open: Int,
    val currently_open: Boolean
)

data class AdminWindowsResponse(
    val success: Boolean,
    val windows: List<AdminWindowDto> = emptyList()
)

data class CreateCourseResponse(
    val success: Boolean,
    val message: String? = null,
    val id: Int? = null
)

data class AdminUploadResultResponse(
    val success: Boolean,
    val message: String? = null,
    val total_score: Double? = null,
    val grade: String? = null
)

data class AdminProjectDto(
    val id: Int,
    val full_name: String,
    val matric_no: String,
    val course_code: String,
    val title: String,
    val uploaded_at: String,
    val is_duplicate: Boolean,
    val file_url: String?
)

data class AdminProjectsResponse(
    val success: Boolean,
    val projects: List<AdminProjectDto> = emptyList()
)
