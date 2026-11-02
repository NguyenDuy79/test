package com.example.fitness.myapplication.core.appstate.auth

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState
import com.example.fitness.myapplication.core.model.license.License
import com.example.fitness.myapplication.core.model.user.Profile
import com.example.fitness.myapplication.core.model.user.User
import com.example.fitness.myapplication.core.network.AppException

/** ≈ `AuthStatus` của Flutter. */
enum class AuthStatus { INITIAL, AUTHENTICATED, UNAUTHENTICATED }

/** ≈ `AuthState` (freezed) bên Flutter. */
data class AuthState(
    val status: AuthStatus = AuthStatus.INITIAL,
    val user: User = User(),
    val currentProfile: Profile = Profile(),
    val licenses: List<License> = emptyList(),
    val isLoadingUser: Boolean = false,
    val isLoadingLicenses: Boolean = false,
    val isInitLoadFinished: Boolean = false,
    val initLoadException: AppException? = null,
) : UiState {
    val isAuthenticated: Boolean get() = status == AuthStatus.AUTHENTICATED
    val hasProfile: Boolean get() = currentProfile.id != 0
    val hasActiveLicense: Boolean get() = licenses.any { it.isActive }
}

/** ≈ các method public của `AuthBloc` — mỗi hành động là một Intent. */
sealed interface AuthIntent : UiIntent {
    /** Mở app: khôi phục phiên từ token đã lưu. */
    object InitLoad : AuthIntent
    object LoadUser : AuthIntent
    object LoadLicenses : AuthIntent
    object RefreshToken : AuthIntent

    /** Login / sign up / reset password thành công (token đã được repository lưu). */
    data class SignInSucceeded(
        val user: User,
        val profiles: List<Profile> = user.profiles,
    ) : AuthIntent

    data class SetProfile(val profile: Profile) : AuthIntent
    data class SwitchProfileSucceeded(val profile: Profile) : AuthIntent
    object SignOut : AuthIntent
}

/** Event một lần, root (`EdupiaAppRoot`) collect. */
sealed interface AuthEffect : UiEffect {
    /** Đã đăng xuất / hết phiên ⇒ root đưa về Login. */
    object SignedOut : AuthEffect
}
