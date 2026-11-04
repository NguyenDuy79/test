package com.example.fitness.myapplication.feature.auth.forgotpassword

import com.example.fitness.myapplication.core.base.BaseViewModel

/**
 * ViewModel màn Quên mật khẩu (Skeleton chưa code logic sâu theo yêu cầu).
 */
class ForgotPasswordViewModel : BaseViewModel<ForgotPasswordState, ForgotPasswordIntent, ForgotPasswordEffect>(
    initialState = ForgotPasswordState()
) {

    override fun handleIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.PhoneChanged -> setState { copy(phone = intent.phone) }
            is ForgotPasswordIntent.SubmitForgotPassword -> {
                // TODO: Logic gửi yêu cầu OTP quên mật khẩu (để đấy)
            }
            is ForgotPasswordIntent.NavigateBack -> sendEffect(ForgotPasswordEffect.NavigateBack)
        }
    }
}
