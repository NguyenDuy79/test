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

    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged ->
                onPhoneChange(currentState.isPushLoginButton, intent.phone)

            is LoginIntent.PasswordChanged ->
                onPasswordChange(currentState.isPushLoginButton, intent.password)

            is LoginIntent.TogglePasswordVisibility ->
                setState { copy(isPasswordVisible = !isPasswordVisible) }

            // Đang gọi API thì bỏ qua các lần bấm tiếp theo.
            is LoginIntent.SubmitLogin -> launchDroppable(intent) { submitLogin() }

            is LoginIntent.SocialLogin ->
                sendEffect(LoginEffect.ShowToast("Đang phát triển đăng nhập qua ${intent.provider}"))

            is LoginIntent.NavigateToSignUp -> sendEffect(LoginEffect.NavigateToSignUp)
            is LoginIntent.NavigateToForgotPassword -> sendEffect(LoginEffect.NavigateToForgotPassword)
            is LoginIntent.ClearError -> setState { copy() }
        }
    }

    private suspend fun submitLogin() {
        validatePassword(currentState.password)
        validatePhone(currentState.phone)
        if (currentState.errorPhone != null || currentState.errorPassword != null) return

        setState { copy(isLoading = true) }

        val result = authRepository.login(currentState.phone, currentState.password)

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
                val message =
                    exception.message.ifEmpty { "Đăng nhập không thành công. Vui lòng thử lại!" }
                setState { copy(isLoading = false) }
                sendEffect(LoginEffect.ShowToast(message))
            }
        )
    }

    fun onPhoneChange(isPushLoginButton: Boolean, phone: String) {
        if (isPushLoginButton) {
            validatePhone(phone.trim())
        } else {
            setState { copy(phone = phone.trim(), errorPhone = null) }

        }
    }

    fun onPasswordChange(isPushLoginButton: Boolean, password: String) {

        if (isPushLoginButton) {
            validatePassword(password.trim())
        } else {
            setState { copy(password = password.trim(), errorPassword = null) }
        }
    }

    fun validatePhone(phone: String) {

        if (phone.isEmpty()) {
            setState { copy(phone = phone, errorPhone = "Số điện thoại không được để trống") }

        } else if (phone.length !in 10..11) {

            setState {
                copy(phone = phone, errorPhone = "Sai định dạng số điện thoại")

            }
        } else {
            setState { copy(phone = phone, errorPhone = null) }

        }
    }

    fun validatePassword(password: String) {
        if (password.isEmpty()) {
            setState {
                copy(
                    password = password,
                    errorPassword = "Mật khẩu không được để trống"
                )
            }
        } else if (password.length < 6) {
            setState {
                copy(
                    password = password,
                    errorPassword = "Mật khẩu phải có ít nhất 6 ký tự"
                )
            }
        } else {
            setState { copy(password = password , errorPassword = null) }
        }
    }

}
