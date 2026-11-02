package com.example.fitness.myapplication.feature.auth.signup

import com.example.fitness.myapplication.core.base.BaseViewModel
import com.example.fitness.myapplication.core.repository.AuthRepository

class SignUpViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel<SignUpState, SignUpIntent, SignUpEffect>(
    initialState = SignUpState()
) {

    override suspend fun handleIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.PhoneChanged -> {
                setState { copy(phone = intent.phone, errorMessage = null) }
            }
            is SignUpIntent.FullNameChanged -> {
                setState { copy(fullName = intent.fullName, errorMessage = null) }
            }
            is SignUpIntent.PasswordChanged -> {
                setState { copy(password = intent.password, errorMessage = null) }
            }
            is SignUpIntent.ConfirmPasswordChanged -> {
                setState { copy(confirmPassword = intent.confirmPassword, errorMessage = null) }
            }
            is SignUpIntent.TogglePasswordVisibility -> {
                setState { copy(isPasswordVisible = !isPasswordVisible) }
            }
            is SignUpIntent.SubmitSignUp -> {
                val phone = currentState.phone.trim()
                val name = currentState.fullName.trim()
                val pass = currentState.password.trim()
                val confirmPass = currentState.confirmPassword.trim()

                if (phone.length < 9) {
                    setState { copy(errorMessage = "Số điện thoại không hợp lệ") }
                    return
                }

                if (name.isEmpty()) {
                    setState { copy(errorMessage = "Vui lòng nhập họ và tên") }
                    return
                }

                if (pass.length < 6) {
                    setState { copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự") }
                    return
                }

                if (pass != confirmPass) {
                    setState { copy(errorMessage = "Mật khẩu xác nhận không khớp") }
                    return
                }

                setState { copy(isLoading = true, errorMessage = null) }

                // Giả lập đăng ký / gọi auth API
                sendEffect(SignUpEffect.ShowToast("Đăng ký thành công! Đang chuyển đến màn hình chính..."))
                sendEffect(SignUpEffect.NavigateToHome)
                setState { copy(isLoading = false) }
            }
            is SignUpIntent.NavigateToLogin -> {
                sendEffect(SignUpEffect.NavigateToLogin)
            }
        }
    }
}
