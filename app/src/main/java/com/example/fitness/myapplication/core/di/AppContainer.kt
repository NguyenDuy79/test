package com.example.fitness.myapplication.core.di

import android.content.Context
import com.example.fitness.myapplication.BuildConfig
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.appstate.common.CommonStore
import com.example.fitness.myapplication.core.local.AppPreferences
import com.example.fitness.myapplication.core.network.AppEnv
import com.example.fitness.myapplication.core.network.AppUrl
import com.example.fitness.myapplication.core.network.HttpClientFactory
import com.example.fitness.myapplication.core.network.RetrofitFactory
import com.example.fitness.myapplication.core.network.TokenStore
import com.example.fitness.myapplication.core.network.TokenStoreImpl
import com.example.fitness.myapplication.core.network.api.AIFreeTalkApiService
import com.example.fitness.myapplication.core.network.api.AuthApiService
import com.example.fitness.myapplication.core.network.api.CourseApiService
import com.example.fitness.myapplication.core.network.api.SessionApiService
import com.example.fitness.myapplication.core.network.api.UserApiService
import com.example.fitness.myapplication.core.repository.AIFreeTalkRepository
import com.example.fitness.myapplication.core.repository.AIFreeTalkRepositoryImpl
import com.example.fitness.myapplication.core.repository.AuthRepository
import com.example.fitness.myapplication.core.repository.AuthRepositoryImpl
import com.example.fitness.myapplication.core.repository.CourseRepository
import com.example.fitness.myapplication.core.repository.CourseRepositoryImpl
import com.example.fitness.myapplication.core.repository.SessionRepository
import com.example.fitness.myapplication.core.repository.SessionRepositoryImpl
import com.example.fitness.myapplication.core.repository.UserRepository
import com.example.fitness.myapplication.core.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Service Locator / Dependency Injection Container trung tâm cho toàn bộ ứng dụng Edupia AI.
 */
class AppContainer(private val context: Context) {

    /**
     * Scope sống theo process (≈ vòng đời của các `@lazySingleton` bên Flutter).
     * Dùng cho tác vụ app-wide không được huỷ khi một màn hình đóng.
     */
    val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val preferences: AppPreferences by lazy {
        AppPreferences.getInstance(context)
    }

    val tokenStore: TokenStore by lazy {
        TokenStoreImpl(preferences)
    }

    val appEnv: AppEnv by lazy {
        AppEnv.instance
    }

    val okHttpClient: OkHttpClient by lazy {
        HttpClientFactory.create(tokenStore = tokenStore, isDebug = true)
    }

    // Retrofit instance cho Mass LMS API
    val lmsRetrofit: Retrofit by lazy {
        RetrofitFactory.create(
            baseUrl = AppUrl.lmsUrl(),
            okHttpClient = okHttpClient
        )
    }

    // Retrofit instance cho Mass Core Base API
    val baseRetrofit: Retrofit by lazy {
        RetrofitFactory.create(
            baseUrl = AppUrl.baseUrl(),
            okHttpClient = okHttpClient
        )
    }

    // ==================== API Services ====================

    val authApiService: AuthApiService by lazy {
        baseRetrofit.create(AuthApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        baseRetrofit.create(UserApiService::class.java)
    }

    val courseApiService: CourseApiService by lazy {
        lmsRetrofit.create(CourseApiService::class.java)
    }

    val sessionApiService: SessionApiService by lazy {
        lmsRetrofit.create(SessionApiService::class.java)
    }

    val aiFreeTalkApiService: AIFreeTalkApiService by lazy {
        lmsRetrofit.create(AIFreeTalkApiService::class.java)
    }

    // ==================== Repositories ====================

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, tokenStore)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userApiService)
    }

    val courseRepository: CourseRepository by lazy {
        CourseRepositoryImpl(courseApiService)
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(sessionApiService)
    }

    val aiFreeTalkRepository: AIFreeTalkRepository by lazy {
        AIFreeTalkRepositoryImpl(aiFreeTalkApiService)
    }

    // ==================== App-wide state (≈ AuthBloc / CommonBloc) ====================
    // Một instance duy nhất cho toàn app — giống `BlocProvider.value(appDi<AuthBloc>())`:
    // ViewModel và root UI luôn trỏ cùng một store.

    val authStore: AuthStore by lazy {
        AuthStore(
            authRepository = authRepository,
            userRepository = userRepository,
            tokenStore = tokenStore,
            scope = applicationScope,
        )
    }

    val commonStore: CommonStore by lazy {
        CommonStore(
            preferences = preferences,
            authStore = authStore,
            appVersion = BuildConfig.VERSION_NAME,
            scope = applicationScope,
        )
    }
}
