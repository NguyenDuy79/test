package com.example.fitness.myapplication.core.appstate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.appstate.common.CommonStore

/**
 * ≈ `MultiBlocProvider` ở `MyApp` bên Flutter.
 *
 * Đưa [AuthStore] / [CommonStore] xuống toàn bộ cây Compose để composable nhỏ
 * (header, avatar, badge…) đọc được mà không cần ViewModel:
 *
 * ```
 * val auth by LocalAuthStore.current.state.collectAsStateWithLifecycle()   // ≈ BlocBuilder
 * ```
 *
 * Quy ước:
 * - ViewModel **không** lấy store từ đây — nhận qua constructor từ `AppContainer`.
 * - Composable chỉ nên **đọc** state; action có logic thì đi qua ViewModel của màn.
 */
val LocalAuthStore = staticCompositionLocalOf<AuthStore> {
    error("AuthStore chưa được cung cấp — bọc UI trong AppStateProvider")
}

val LocalCommonStore = staticCompositionLocalOf<CommonStore> {
    error("CommonStore chưa được cung cấp — bọc UI trong AppStateProvider")
}

@Composable
fun AppStateProvider(
    authStore: AuthStore,
    commonStore: CommonStore,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAuthStore provides authStore,
        LocalCommonStore provides commonStore,
        content = content,
    )
}
