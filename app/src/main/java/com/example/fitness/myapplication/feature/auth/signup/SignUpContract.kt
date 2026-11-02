package com.example.fitness.myapplication.feature.auth.signup

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class SignUpState(
    val phone: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val canSubmit: Boolean
        get() = phone.isNotBlank() && fullName.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
}

sealed interface SignUpIntent : UiIntent {
    data class PhoneChanged(val phone: String) : SignUpIntent
    data class FullNameChanged(val fullName: String) : SignUpIntent
    data class PasswordChanged(val password: String) : SignUpIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpIntent
    object TogglePasswordVisibility : SignUpIntent
    object SubmitSignUp : SignUpIntent
    object NavigateToLogin : SignUpIntent
}

sealed interface SignUpEffect : UiEffect {
    object NavigateToLogin : SignUpEffect
    object NavigateToHome : SignUpEffect
    data class ShowToast(val message: String) : SignUpEffect
}
