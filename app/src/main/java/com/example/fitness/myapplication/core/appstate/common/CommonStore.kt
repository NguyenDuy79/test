package com.example.fitness.myapplication.core.appstate.common

import com.example.fitness.myapplication.core.appstate.auth.AuthIntent
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.base.BaseStore
import com.example.fitness.myapplication.core.local.AppPreferences
import com.example.fitness.myapplication.core.local.PrefsKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Store MVI app-wide cho state dùng chung — tương đương `CommonBloc` (`@lazySingleton`).
 * Global loading, version, mic iSpeak, cờ onboarding…
 */
class CommonStore(
    private val preferences: AppPreferences,
    private val authStore: AuthStore,
    appVersion: String,
    scope: CoroutineScope,
) : BaseStore<CommonState, CommonIntent, CommonEffect>(
    initialState = CommonState(
        currentVersion = appVersion,
        enableOnboarding = preferences.getBoolean(PrefsKey.ENABLE_ONBOARDING, defaultValue = true),
    ),
    scope = scope,
) {

    override suspend fun handleIntent(intent: CommonIntent) {
        when (intent) {
            is CommonIntent.Init -> init()

            // ≈ CommonBloc.changeLoadingState(isShow:)
            is CommonIntent.ChangeLoading -> setState { copy(isShowLoading = intent.isShow) }

            is CommonIntent.RequestListenISpeak -> setState {
                if (iSpeakStatus == ISpeakStatus.READY_TO_SPEAK) this
                else copy(iSpeakStatus = ISpeakStatus.READY_TO_SPEAK)
            }

            is CommonIntent.OffMicISpeak -> setState {
                if (iSpeakStatus == ISpeakStatus.RE_WAITING || iSpeakStatus == ISpeakStatus.WAITING) this
                else copy(iSpeakStatus = ISpeakStatus.RE_WAITING)
            }

            // Không early-return (xem comment `resetMicForNewQuestion` bên Flutter).
            is CommonIntent.ResetMicForNewQuestion -> setState {
                copy(iSpeakStatus = ISpeakStatus.WAITING)
            }

            is CommonIntent.SetEnableOnboarding -> {
                preferences.setBoolean(PrefsKey.ENABLE_ONBOARDING, intent.enable)
                setState { copy(enableOnboarding = intent.enable) }
            }
        }
    }

    /** ≈ `CommonBloc.init()`. */
    private suspend fun init() {
        setState { copy(isInitLoadFinished = false, initLoadException = null) }
        // TODO: loadConfig / loadGrades / saveLog / device info khi port xong API.
        authStore.dispatch(AuthIntent.RefreshToken).join()
        setState { copy(isInitLoadFinished = true) }
        currentState.initLoadException?.let { sendEffect(CommonEffect.ShowMessage(it.message)) }
    }
}

/**
 * Bọc một tác vụ bằng global loading, luôn tắt loading kể cả khi lỗi / bị huỷ.
 * Chỉ là tổ hợp hai intent `ChangeLoading(true/false)`.
 */
suspend fun <T> CommonStore.withGlobalLoading(block: suspend () -> T): T {
    dispatch(CommonIntent.ChangeLoading(true)).join()
    return try {
        block()
    } finally {
        withContext(NonCancellable) { dispatch(CommonIntent.ChangeLoading(false)).join() }
    }
}
