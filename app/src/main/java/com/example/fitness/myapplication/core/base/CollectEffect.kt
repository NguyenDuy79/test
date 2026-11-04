package com.example.fitness.myapplication.core.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Thu effect một lần (navigation, toast...) từ ViewModel theo lifecycle.
 *
 * - Chỉ collect khi màn hình >= STARTED. Khi app ở nền, effect nằm chờ trong Channel,
 *   quay lại mới xử lý (không navigate lúc Activity đã stop).
 * - `Dispatchers.Main.immediate` để không lỡ effect trong khoảnh khắc chuyển lifecycle.
 * - Dùng `collect` (không phải `collectLatest`) để effect sau không huỷ effect trước.
 */
@Composable
fun <T> CollectEffect(
    flow: Flow<T>,
    vararg keys: Any?,
    onEffect: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(flow, lifecycleOwner, *keys) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect { currentOnEffect(it) }
            }
        }
    }
}
