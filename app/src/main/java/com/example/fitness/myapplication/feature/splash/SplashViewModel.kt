package com.example.fitness.myapplication.feature.splash

import com.example.fitness.myapplication.core.appstate.auth.AuthIntent
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.appstate.common.CommonIntent
import com.example.fitness.myapplication.core.appstate.common.CommonStore
import com.example.fitness.myapplication.core.base.BaseViewModel
import kotlinx.coroutines.delay

/**
 * ViewModel màn Splash.
 * Khởi tạo state app-wide giống Flutter: `CommonBloc.init()` → `AuthBloc.initLoad()`,
 * rồi điều hướng theo trạng thái đăng nhập.
 */
class SplashViewModel(
    private val authStore: AuthStore,
    private val commonStore: CommonStore,
) : BaseViewModel<SplashState, SplashIntent, SplashEffect>(
    initialState = SplashState()
) {

    init {
        onIntent(SplashIntent.CheckAuthStatus)
    }

    override fun handleIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.CheckAuthStatus -> launchDroppable(intent) { bootstrap() }
        }
    }

    private suspend fun bootstrap() {
        setState { copy(isLoading = true) }
        val startedAt = System.currentTimeMillis()

        // dispatch trả về Job ⇒ join() để chờ store xử lý xong intent.
        commonStore.dispatch(CommonIntent.Init).join()
        authStore.dispatch(AuthIntent.InitLoad).join()

        // Giữ Splash tối thiểu 1.5s cho hiệu ứng thương hiệu.
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < MIN_SPLASH_MS) delay(MIN_SPLASH_MS - elapsed)

        setState { copy(isLoading = false) }
        sendEffect(
            if (authStore.currentState.isAuthenticated) SplashEffect.NavigateToHome
            else SplashEffect.NavigateToOnboarding
        )
    }

    private companion object {
        const val MIN_SPLASH_MS = 1_500L
    }
}
