package com.example.fitness.myapplication.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lớp ViewModel cơ sở theo chuẩn MVI phản ứng với UiIntent, phát UiState và UiEffect.
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
    protected val dispatchers: AppDispatchers = AppDispatchers(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _intent = MutableSharedFlow<I>()
    val intent: SharedFlow<I> = _intent.asSharedFlow()

    init {
        viewModelScope.launch(dispatchers.default) {
            _intent.collect { handleIntent(it) }
        }
    }

    val currentState: S
        get() = _uiState.value

    fun sendIntent(intent: I) {
        viewModelScope.launch(dispatchers.main) {
            _intent.emit(intent)
        }
    }

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch(dispatchers.main) {
            _effect.send(effect)
        }
    }

    protected abstract suspend fun handleIntent(intent: I)
}
