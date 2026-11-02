package com.example.fitness.myapplication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.fitness.myapplication.core.appstate.AppStateProvider
import com.example.fitness.myapplication.core.appstate.auth.AuthEffect
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.appstate.common.CommonEffect
import com.example.fitness.myapplication.core.appstate.common.CommonStore
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Root của app — tương đương `MyApp` bên Flutter:
 *
 * ```
 * Flutter                                     Android (file này)
 * MultiBlocProvider(AuthBloc, CommonBloc)  →  AppStateProvider(authStore, commonStore)
 * BlocListener<AuthBloc>                   →  collect authStore.effect: SignedOut ⇒ onSignedOut()
 * BlocListener<CommonBloc>                 →  collect commonStore.effect: ShowMessage ⇒ Toast
 * BlocBuilder<CommonBloc>(isShowLoading)   →  collectAsStateWithLifecycle + map/distinct
 * Stack[ MaterialApp, LoadingOverlay ]     →  Box { content(); if (loading) GlobalLoadingOverlay() }
 * ```
 *
 * Effect của store là Channel ⇒ CHỈ root collect (một consumer). Collect trong
 * `repeatOnLifecycle(STARTED)`: app ở nền thì effect được giữ trong buffer, quay lại mới xử lý.
 */
@Composable
fun EdupiaAppRoot(
    authStore: AuthStore,
    commonStore: CommonStore,
    onSignedOut: () -> Unit,
    content: @Composable () -> Unit,
) {
    AppStateProvider(authStore = authStore, commonStore = commonStore) {

        // ≈ MultiBlocListener ở root: xử lý Effect app-wide.
        // Đăng xuất / hết phiên ở BẤT KỲ đâu ⇒ quay về Login, màn hình không cần tự điều hướng.
        val currentOnSignedOut by rememberUpdatedState(onSignedOut)
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(authStore, commonStore, lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    launch {
                        authStore.effect.collect { effect ->
                            when (effect) {
                                is AuthEffect.SignedOut -> currentOnSignedOut()
                            }
                        }
                    }
                    launch {
                        commonStore.effect.collect { effect ->
                            when (effect) {
                                is CommonEffect.ShowMessage ->
                                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        // ≈ BlocBuilder<CommonBloc, CommonState>(buildWhen: isShowLoading thay đổi)
        val isShowLoading by remember(commonStore) {
            commonStore.state.map { it.isShowLoading }.distinctUntilChanged()
        }.collectAsStateWithLifecycle(initialValue = commonStore.currentState.isShowLoading)

        Box(modifier = Modifier.fillMaxSize()) {
            content()
            if (isShowLoading) GlobalLoadingOverlay()
        }
    }
}

/** Overlay phủ toàn app, chặn thao tác bên dưới khi đang loading. */
@Composable
private fun GlobalLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = EdupiaColors.Primary)
    }
}
