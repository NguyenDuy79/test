package com.example.fitness.myapplication.core.base

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
 * Store MVI cho state **app-wide** (≈ `Bloc` `@lazySingleton` bên Flutter).
 *
 * Cùng mô hình với [BaseViewModel] nhưng sống theo process thay vì theo màn hình:
 *
 * ```
 *            dispatch(Intent)
 *  caller ───────────────────► handleIntent ──► setState { copy(...) } ──► state: StateFlow<S>
 *                                    │
 *                                    └────────► sendEffect(E) ─────────► effect: Flow<E>
 * ```
 *
 * - [dispatch] là **cửa vào duy nhất**. Trả về [Job] để caller cần chờ kết quả
 *   (vd. Splash chờ `InitLoad`) có thể `join()`.
 * - State chỉ đổi qua [setState] (reducer thuần, atomic nhờ `MutableStateFlow.update`).
 * - [effect] là event một lần (Channel) — **chỉ một nơi collect** (root của app).
 */
abstract class BaseStore<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
    private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    val currentState: S get() = _state.value

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    fun dispatch(intent: I): Job = scope.launch { handleIntent(intent) }

    protected fun setState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected suspend fun sendEffect(effect: E) {
        _effect.send(effect)
    }

    protected abstract suspend fun handleIntent(intent: I)
}
