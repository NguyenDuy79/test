package com.example.fitness.myapplication.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel MVI theo Unidirectional Data Flow (UDF) chuẩn Android / Jetpack Compose.
 *
 * ```
 *   UI ──onIntent(intent)──► handleIntent ──► setState { copy(...) } ──► uiState ──► collectAsStateWithLifecycle()
 *                                  │
 *                                  └────────► sendEffect(effect) ────► effect ───► CollectEffect { ... }
 * ```
 *
 * - [onIntent] được UI gọi trên main thread và xử lý **đồng bộ**: các thay đổi state đơn giản
 *   (gõ chữ, toggle...) cập nhật ngay trong cùng frame. Điều này quan trọng với `TextField`
 *   của Compose: cập nhật state bất đồng bộ dễ làm nhảy con trỏ hoặc mất ký tự.
 * - [handleIntent] **không phải suspend**. Việc bất đồng bộ tự `launch` trong [viewModelScope],
 *   nên một request 5 giây không bao giờ chặn intent khác.
 * - Chống bấm trùng / huỷ request cũ: [launchDroppable], [launchLatest].
 * - `when (intent)` trong [handleIntent] là exhaustive ⇒ quên xử lý một intent sẽ lỗi lúc compile.
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)

    /** Event một lần (navigation, toast...). Chỉ một nơi collect, dùng [CollectEffect]. */
    val effect: Flow<E> = _effect.receiveAsFlow()

    val currentState: S
        get() = _uiState.value

    private val keyedJobs = mutableMapOf<Any, Job>()

    /** Điểm vào duy nhất từ UI. */
    fun onIntent(intent: I) = handleIntent(intent)

    protected abstract fun handleIntent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    /**
     * Bỏ qua nếu job cùng [key] đang chạy. Dùng cho submit, thanh toán, refresh.
     */
    protected fun launchDroppable(key: Any, block: suspend CoroutineScope.() -> Unit): Job? {
        if (keyedJobs[key]?.isActive == true) return null
        return startKeyed(key, block)
    }

    /**
     * Huỷ job cũ cùng [key] rồi chạy job mới. Dùng cho search, filter.
     */
    protected fun launchLatest(key: Any, block: suspend CoroutineScope.() -> Unit): Job {
        keyedJobs[key]?.cancel()
        return startKeyed(key, block)
    }

    private fun startKeyed(key: Any, block: suspend CoroutineScope.() -> Unit): Job {
        val job = viewModelScope.launch(block = block)
        keyedJobs[key] = job
        job.invokeOnCompletion { if (keyedJobs[key] === job) keyedJobs.remove(key) }
        return job
    }
}
