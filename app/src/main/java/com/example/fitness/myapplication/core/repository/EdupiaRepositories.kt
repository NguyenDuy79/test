package com.example.fitness.myapplication.core.repository

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
import com.example.fitness.myapplication.core.network.ApiResult
import com.example.fitness.myapplication.core.network.BaseRepository
import com.example.fitness.myapplication.core.network.TokenStore
import com.example.fitness.myapplication.core.network.api.AIFreeTalkApiService
import com.example.fitness.myapplication.core.network.api.AuthApiService
import com.example.fitness.myapplication.core.network.api.CourseApiService
import com.example.fitness.myapplication.core.network.api.GenMessageRequest
import com.example.fitness.myapplication.core.network.api.LoginRequest
import com.example.fitness.myapplication.core.network.api.LoginResponse
import com.example.fitness.myapplication.core.network.api.RefreshTokenRequest
import com.example.fitness.myapplication.core.network.api.SessionApiService
import com.example.fitness.myapplication.core.network.api.UserApiService

// ==================== Auth Repository ====================

interface AuthRepository {
    suspend fun login(phone: String, password: String): ApiResult<LoginResponse>
    suspend fun refreshToken(): ApiResult<LoginResponse>
    suspend fun logout()
    fun isLoggedIn(): Boolean
}

class AuthRepositoryImpl(
    private val authApi: AuthApiService,
    private val tokenStore: TokenStore,
) : BaseRepository(), AuthRepository {

    override suspend fun login(phone: String, password: String): ApiResult<LoginResponse> {
        val result = callApiCatching {
            authApi.login(LoginRequest(phone = phone, password = password))
        }

        result.onSuccess { response ->
            if (response.access_token.isNotEmpty()) {
                tokenStore.accessToken = response.access_token
            }
            if (response.edupia_token.isNotEmpty()) {
                tokenStore.edupiaToken = response.edupia_token
            }
            response.user?.let { user ->
                tokenStore.accessUser = user.id.toString()
                tokenStore.activeAccountId = user.id
            }
            response.profiles.firstOrNull()?.let { firstProfile ->
                tokenStore.activeProfileId = firstProfile.id
            }
        }

        return result
    }

    override suspend fun refreshToken(): ApiResult<LoginResponse> {
        val currentToken = tokenStore.accessToken
        val result = callApiCatching {
            authApi.refreshToken(RefreshTokenRequest(access_token = currentToken))
        }

        result.onSuccess { response ->
            if (response.access_token.isNotEmpty()) {
                tokenStore.accessToken = response.access_token
            }
        }

        return result
    }

    override suspend fun logout() {
        tokenStore.clearAuth()
    }

    override fun isLoggedIn(): Boolean {
        return tokenStore.accessToken.isNotEmpty()
    }
}

// ==================== User Repository ====================

interface UserRepository {
    suspend fun getUserInfo(): ApiResult<User>
    suspend fun getProfiles(): ApiResult<List<Profile>>
    suspend fun getLicenses(): ApiResult<List<License>>
}

class UserRepositoryImpl(
    private val userApi: UserApiService,
) : BaseRepository(), UserRepository {

    override suspend fun getUserInfo(): ApiResult<User> = callApiCatching {
        userApi.getUserInfo()
    }

    override suspend fun getProfiles(): ApiResult<List<Profile>> = callApiListCatching {
        userApi.getProfiles()
    }

    override suspend fun getLicenses(): ApiResult<List<License>> = callApiListCatching {
        userApi.getLicenses()
    }
}

// ==================== Course Repository ====================

interface CourseRepository {
    suspend fun getCourses(gradeId: Int): ApiResult<List<Course>>
    suspend fun getLessons(courseId: Int): ApiResult<List<Lesson>>
}

class CourseRepositoryImpl(
    private val courseApi: CourseApiService,
) : BaseRepository(), CourseRepository {

    override suspend fun getCourses(gradeId: Int): ApiResult<List<Course>> = callApiListCatching {
        courseApi.getCourses(gradeId)
    }

    override suspend fun getLessons(courseId: Int): ApiResult<List<Lesson>> = callApiListCatching {
        courseApi.getLessons(courseId)
    }
}

// ==================== Session Repository ====================

interface SessionRepository {
    suspend fun getNextSession(profileId: Int): ApiResult<Session>
    suspend fun getSessionDetail(sessionId: Int): ApiResult<Session>
    suspend fun getSessionsByWeek(profileId: Int, week: Int, year: Int): ApiResult<List<Session>>
    suspend fun getBanners(): ApiResult<List<Banner>>
    suspend fun getGamification(profileId: Int): ApiResult<Gamification>
}

class SessionRepositoryImpl(
    private val sessionApi: SessionApiService,
) : BaseRepository(), SessionRepository {

    override suspend fun getNextSession(profileId: Int): ApiResult<Session> = callApiCatching {
        sessionApi.getNextSession(profileId)
    }

    override suspend fun getSessionDetail(sessionId: Int): ApiResult<Session> = callApiCatching {
        sessionApi.getSessionDetail(sessionId)
    }

    override suspend fun getSessionsByWeek(profileId: Int, week: Int, year: Int): ApiResult<List<Session>> =
        callApiListCatching {
            sessionApi.getSessionsByWeek(profileId, week, year)
        }

    override suspend fun getBanners(): ApiResult<List<Banner>> = callApiListCatching {
        sessionApi.getBanners()
    }

    override suspend fun getGamification(profileId: Int): ApiResult<Gamification> = callApiCatching {
        sessionApi.getGamification(profileId)
    }
}

// ==================== AI Free Talk Repository ====================

interface AIFreeTalkRepository {
    suspend fun getAIScriptById(scriptId: String, profileId: String): ApiResult<AIScript>
    suspend fun genMessage(
        scriptId: String,
        message: String,
        assistanceVoidId: String,
        threadId: String? = null,
    ): ApiResult<AIChatModel>
    suspend fun getReviewAnswer(sessionId: String): ApiResult<ReviewAnswer>
}

class AIFreeTalkRepositoryImpl(
    private val aiApi: AIFreeTalkApiService,
) : BaseRepository(), AIFreeTalkRepository {

    override suspend fun getAIScriptById(scriptId: String, profileId: String): ApiResult<AIScript> =
        callApiCatching {
            aiApi.getAIScriptById(scriptId, profileId)
        }

    override suspend fun genMessage(
        scriptId: String,
        message: String,
        assistanceVoidId: String,
        threadId: String?,
    ): ApiResult<AIChatModel> = callApiCatching {
        aiApi.genMessage(
            GenMessageRequest(
                scriptId = scriptId,
                message = message,
                assistanceVoidId = assistanceVoidId,
                threadId = threadId,
            )
        )
    }

    override suspend fun getReviewAnswer(sessionId: String): ApiResult<ReviewAnswer> = callApiCatching {
        aiApi.getReviewAnswer(sessionId)
    }
}
