package com.example.fitness.myapplication.feature.auth.login

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class LoginState(
    val phone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isPushLoginButton: Boolean = true,
    val errorPhone: String? = null,
    val errorPassword: String? = null,
) : UiState {

}

sealed interface LoginIntent : UiIntent {
    data class PhoneChanged(val phone: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    object TogglePasswordVisibility : LoginIntent
    object SubmitLogin : LoginIntent
    data class SocialLogin(val provider: String) : LoginIntent
    object NavigateToSignUp : LoginIntent
    object NavigateToForgotPassword : LoginIntent
    object ClearError : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    object NavigateToHome : LoginEffect
    object NavigateToSignUp : LoginEffect
    object NavigateToForgotPassword : LoginEffect
    data class ShowToast(val message: String) : LoginEffect
}
