package com.buph.srms.data

import com.buph.srms.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- Student auth ----------
    @POST("student_register.php")
    suspend fun studentRegister(@Body body: Map<String, String>): Response<StudentAuthResponse>

    @POST("student_login.php")
    suspend fun studentLogin(@Body body: Map<String, String>): Response<StudentAuthResponse>

    @GET("get_profile.php")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @POST("complete_profile.php")
    suspend fun completeProfile(
        @Part("full_name") fullName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("department") department: RequestBody,
        @Part("level") level: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<ProfileResponse>

    // ---------- Registration ----------
    @GET("get_sessions.php")
    suspend fun getSessions(): Response<SessionsResponse>

    @GET("get_registration_window.php")
    suspend fun getRegistrationWindow(
        @Query("session") session: String,
        @Query("semester") semester: String
    ): Response<RegistrationWindowResponse>

    @GET("get_courses.php")
    suspend fun getCourses(
        @Query("session") session: String,
        @Query("semester") semester: String
    ): Response<CoursesResponse>

    @POST("register_courses.php")
    suspend fun registerCourses(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<RegisterCoursesResponse>

    // ---------- Projects ----------
    @GET("get_my_courses.php")
    suspend fun getMyCourses(): Response<CoursesResponse>

    @Multipart
    @POST("upload_project.php")
    suspend fun uploadProject(
        @Part("course_id") courseId: RequestBody,
        @Part("title") title: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadProjectResponse>

    @GET("get_my_projects.php")
    suspend fun getMyProjects(): Response<ProjectsResponse>

    // ---------- Results & materials ----------
    @GET("get_results.php")
    suspend fun getResults(
        @Query("session") session: String? = null,
        @Query("semester") semester: String? = null
    ): Response<ResultsResponse>

    @GET("get_materials.php")
    suspend fun getMaterials(): Response<MaterialsResponse>

    // ---------- Admin auth ----------
    @POST("admin_login.php")
    suspend fun adminLogin(@Body body: Map<String, String>): Response<AdminAuthResponse>

    @GET("admin_dashboard.php")
    suspend fun adminDashboard(): Response<AdminDashboardResponse>

    @GET("admin_get_students.php")
    suspend fun adminGetStudents(@Query("q") query: String? = null): Response<AdminStudentsResponse>

    @GET("admin_get_student_detail.php")
    suspend fun adminGetStudentDetail(@Query("id") id: Int): Response<AdminStudentDetailResponse>

    @POST("admin_get_student_detail.php")
    suspend fun adminToggleStudentStatus(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<ToggleStatusResponse>

    @GET("admin_courses.php")
    suspend fun adminGetCourses(): Response<CoursesResponse>

    @POST("admin_courses.php")
    suspend fun adminCreateCourse(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<CreateCourseResponse>

    @GET("admin_courses.php")
    suspend fun adminDeleteCourse(@Query("delete") id: Int): Response<SimpleResponse>

    @GET("admin_registration_settings.php")
    suspend fun adminGetWindows(): Response<AdminWindowsResponse>

    @POST("admin_registration_settings.php")
    suspend fun adminSaveWindow(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<SimpleResponse>

    @POST("admin_registration_settings.php")
    suspend fun adminToggleWindow(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<SimpleResponse>

    @POST("admin_upload_result.php")
    suspend fun adminUploadResult(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<AdminUploadResultResponse>

    @Multipart
    @POST("admin_upload_material.php")
    suspend fun adminUploadMaterial(
        @Part("course_id") courseId: RequestBody,
        @Part("title") title: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<SimpleResponse>

    @GET("admin_upload_material.php")
    suspend fun adminGetMaterials(): Response<MaterialsResponse>

    @GET("admin_upload_material.php")
    suspend fun adminDeleteMaterial(@Query("delete") id: Int): Response<SimpleResponse>

    @GET("admin_projects.php")
    suspend fun adminGetProjects(@Query("filter") filter: String = "all"): Response<AdminProjectsResponse>
}
