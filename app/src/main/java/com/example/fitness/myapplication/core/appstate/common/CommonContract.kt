package com.example.fitness.myapplication.core.appstate.common

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState
import com.example.fitness.myapplication.core.network.AppException

/** ≈ `ISpeakStatus` của Flutter. */
enum class ISpeakStatus { WAITING, READY_TO_SPEAK, RE_WAITING }

/**
 * ≈ `CommonState` (freezed) bên Flutter.
 * TODO: bổ sung `config` / `grades` khi có API tương ứng bên Android.
 */
data class CommonState(
    val currentVersion: String = "",
    val isAppReview: Boolean = false,
    val isInitLoadFinished: Boolean = false,
    val initLoadException: AppException? = null,
    val isShowLoading: Boolean = false,
    val iSpeakStatus: ISpeakStatus = ISpeakStatus.WAITING,
    val enableOnboarding: Boolean = true,
) : UiState

/** ≈ các method public của `CommonBloc`. */
sealed interface CommonIntent : UiIntent {
    /** Mở app: load cấu hình chung + refresh token. */
    object Init : CommonIntent
    data class ChangeLoading(val isShow: Boolean) : CommonIntent
    object RequestListenISpeak : CommonIntent
    object OffMicISpeak : CommonIntent
    object ResetMicForNewQuestion : CommonIntent
    data class SetEnableOnboarding(val enable: Boolean) : CommonIntent
}

/** Event một lần, root (`EdupiaAppRoot`) collect. */
sealed interface CommonEffect : UiEffect {
    data class ShowMessage(val message: String) : CommonEffect
}
