package com.example.fitness.myapplication.feature.auth.login

import com.example.fitness.myapplication.core.appstate.auth.AuthIntent
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.base.BaseViewModel
import com.example.fitness.myapplication.core.model.user.User
import com.example.fitness.myapplication.core.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val authStore: AuthStore,
) : BaseViewModel<LoginState, LoginIntent, LoginEffect>(
    initialState = LoginState()
) {

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged -> {
                setState { copy(phone = intent.phone, errorMessage = null) }
            }
            is LoginIntent.PasswordChanged -> {
                setState { copy(password = intent.password, errorMessage = null) }
            }
            is LoginIntent.TogglePasswordVisibility -> {
                setState { copy(isPasswordVisible = !isPasswordVisible) }
            }
            is LoginIntent.SubmitLogin -> {
                val currentPhone = currentState.phone.trim()
                val currentPassword = currentState.password.trim()

                if (currentPhone.isEmpty()) {
                    setState { copy(errorMessage = "Vui lòng nhập số điện thoại") }
                    return
                }

                if (currentPassword.isEmpty()) {
                    setState { copy(errorMessage = "Vui lòng nhập mật khẩu") }
                    return
                }

                setState { copy(isLoading = true, errorMessage = null) }

                val result = authRepository.login(currentPhone, currentPassword)

                result.fold(
                    onSuccess = { response ->
                        // Cập nhật phiên app-wide (≈ context.read<AuthBloc>().onSignInSucceed(user))
                        authStore.dispatch(
                            AuthIntent.SignInSucceeded(
                                user = response.user ?: User(),
                                profiles = response.profiles,
                            )
                        )
                        setState { copy(isLoading = false) }
                        sendEffect(LoginEffect.ShowToast("Đăng nhập thành công!"))
                        sendEffect(LoginEffect.NavigateToHome)
                    },
                    onFailure = { exception ->
                        val message = exception.message.ifEmpty { "Đăng nhập không thành công. Vui lòng thử lại!" }
                        setState { copy(isLoading = false, errorMessage = message) }
                        sendEffect(LoginEffect.ShowToast(message))
                    }
                )
            }
            is LoginIntent.SocialLogin -> {
                sendEffect(LoginEffect.ShowToast("Đang phát triển đăng nhập qua ${intent.provider}"))
            }
            is LoginIntent.NavigateToSignUp -> {
                sendEffect(LoginEffect.NavigateToSignUp)
            }
            is LoginIntent.NavigateToForgotPassword -> {
                sendEffect(LoginEffect.NavigateToForgotPassword)
            }
            is LoginIntent.ClearError -> {
                setState { copy(errorMessage = null) }
            }
        }
    }
}
