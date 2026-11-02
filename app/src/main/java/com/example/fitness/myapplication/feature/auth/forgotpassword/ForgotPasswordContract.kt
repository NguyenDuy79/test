package com.example.fitness.myapplication.feature.auth.forgotpassword

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class ForgotPasswordState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface ForgotPasswordIntent : UiIntent {
    data class PhoneChanged(val phone: String) : ForgotPasswordIntent
    object SubmitForgotPassword : ForgotPasswordIntent
    object NavigateBack : ForgotPasswordIntent
}

sealed interface ForgotPasswordEffect : UiEffect {
    object NavigateBack : ForgotPasswordEffect
    data class ShowToast(val message: String) : ForgotPasswordEffect
}
