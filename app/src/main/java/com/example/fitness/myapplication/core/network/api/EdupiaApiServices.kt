package com.example.fitness.myapplication.core.network.api

import com.example.fitness.myapplication.core.model.aichat.AIChatModel
import com.example.fitness.myapplication.core.model.aichat.AIScript
import com.example.fitness.myapplication.core.model.aichat.ReviewAnswer
import com.example.fitness.myapplication.core.model.classroom.Course
import com.example.fitness.myapplication.core.model.classroom.Lesson
import com.example.fitness.myapplication.core.model.classroom.Session
import com.example.fitness.myapplication.core.model.gamification.Banner
import com.example.fitness.myapplication.core.model.gamification.Gamification
import com.example.fitness.myapplication.core.model.license.License
import com.example.fitness.myapplication.core.model.user.Profile
import com.example.fitness.myapplication.core.model.user.User
import com.example.fitness.myapplication.core.network.ApiEnvelope
import com.example.fitness.myapplication.core.network.ApiListEnvelope
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ==================== Auth DTOs ====================
@Serializable
data class LoginRequest(
    val phone: String,
    val password: String? = null,
    val otp: String? = null,
)

@Serializable
data class RefreshTokenRequest(
    val access_token: String,
)

@Serializable
data class LoginResponse(
    val user: User? = null,
    val profiles: List<Profile> = emptyList(),
    val access_token: String = "",
    val edupia_token: String = "",
)

// ==================== AI Chat DTOs ====================
@Serializable
data class GenMessageRequest(
    val scriptId: String,
    val message: String,
    val assistanceVoidId: String,
    val threadId: String? = null,
)

// ==================== Retrofit API Services ====================

interface AuthApiService {
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("user/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiEnvelope<LoginResponse>
}

interface UserApiService {
    @GET("user/info")
    suspend fun getUserInfo(): ApiEnvelope<User>

    @GET("user/profiles")
    suspend fun getProfiles(): ApiListEnvelope<Profile>

    @GET("licenses/my-licenses")
    suspend fun getLicenses(): ApiListEnvelope<License>
}

interface CourseApiService {
    @GET("courses")
    suspend fun getCourses(@Query("grade_id") gradeId: Int): ApiListEnvelope<Course>

    @GET("courses/{id}/lessons")
    suspend fun getLessons(@Path("id") courseId: Int): ApiListEnvelope<Lesson>
}

interface SessionApiService {
    @GET("sessions/next-session")
    suspend fun getNextSession(@Query("profile_id") profileId: Int): ApiEnvelope<Session>

    @GET("sessions/{id}/detail")
    suspend fun getSessionDetail(@Path("id") sessionId: Int): ApiEnvelope<Session>

    @GET("sessions/week")
    suspend fun getSessionsByWeek(
        @Query("profile_id") profileId: Int,
        @Query("week") week: Int,
        @Query("year") year: Int,
    ): ApiListEnvelope<Session>

    @GET("banners")
    suspend fun getBanners(): ApiListEnvelope<Banner>

    @GET("gamification/my-stats")
    suspend fun getGamification(@Query("profile_id") profileId: Int): ApiEnvelope<Gamification>
}

interface AIFreeTalkApiService {
    @GET("ai-free-talk/script/find-by-id")
    suspend fun getAIScriptById(
        @Query("id") scriptId: String,
        @Query("profileId") profileId: String,
    ): ApiEnvelope<AIScript>

    @POST("ai-free-talk/script/gen-message")
    suspend fun genMessage(
        @Body request: GenMessageRequest,
    ): ApiEnvelope<AIChatModel>

    @GET("ai-free-talk/script/session-assessment")
    suspend fun getReviewAnswer(
        @Query("session_id") sessionId: String,
    ): ApiEnvelope<ReviewAnswer>
}
