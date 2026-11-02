package com.example.fitness.myapplication.core.appstate.auth

import com.example.fitness.myapplication.core.base.BaseStore
import com.example.fitness.myapplication.core.model.user.Profile
import com.example.fitness.myapplication.core.model.user.User
import com.example.fitness.myapplication.core.network.TokenStore
import com.example.fitness.myapplication.core.repository.AuthRepository
import com.example.fitness.myapplication.core.repository.UserRepository
import kotlinx.coroutines.CoroutineScope

/**
 * Store MVI app-wide cho phiên đăng nhập — tương đương `AuthBloc` (`@lazySingleton`).
 *
 * - Một instance duy nhất trong `AppContainer`.
 * - Caller chỉ `dispatch(AuthIntent.Xxx)`, đọc [state], root collect [effect].
 */
class AuthStore(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore,
    scope: CoroutineScope,
) : BaseStore<AuthState, AuthIntent, AuthEffect>(
    initialState = AuthState(),
    scope = scope,
) {

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.InitLoad -> initLoad()
            is AuthIntent.LoadUser -> loadUser()
            is AuthIntent.LoadLicenses -> loadLicenses()
            is AuthIntent.RefreshToken -> refreshToken()
            is AuthIntent.SignInSucceeded -> onSignInSucceeded(intent.user, intent.profiles)
            is AuthIntent.SetProfile -> setProfile(intent.profile)
            is AuthIntent.SwitchProfileSucceeded -> {
                setProfile(intent.profile)
                loadUser()
                loadLicenses()
            }
            is AuthIntent.SignOut -> signOut()
        }
    }

    // ==================== Handlers ====================

    /** ≈ `AuthBloc.initLoad()`. */
    private suspend fun initLoad() {
        setState { copy(isInitLoadFinished = false, initLoadException = null) }

        if (!authRepository.isLoggedIn()) {
            setState { AuthState(status = AuthStatus.UNAUTHENTICATED, isInitLoadFinished = true) }
            return
        }

        loadUser()
        // Load user lỗi (vd. token hết hạn) và không có hồ sơ ⇒ coi như chưa đăng nhập.
        if (currentState.initLoadException != null && !currentState.hasProfile) {
            setState { copy(status = AuthStatus.UNAUTHENTICATED, isInitLoadFinished = true) }
            return
        }
        loadLicenses()

        setState { copy(status = AuthStatus.AUTHENTICATED, isInitLoadFinished = true) }
    }

    /** ≈ `AuthBloc.loadUser()`. */
    private suspend fun loadUser() {
        setState { copy(isLoadingUser = true) }
        userRepository.getUserInfo().fold(
            onSuccess = { user ->
                val profile = resolveCurrentProfile(user.profiles)
                setState {
                    val isProfileChanged = profile.id != currentProfile.id
                    copy(
                        user = user,
                        currentProfile = profile,
                        // Đổi con ⇒ license cũ không còn đúng (giống Flutter).
                        licenses = if (isProfileChanged) emptyList() else licenses,
                        isLoadingUser = false,
                    )
                }
            },
            onFailure = { e ->
                setState { copy(isLoadingUser = false, initLoadException = e) }
            },
        )
    }

    /** ≈ `AuthBloc.loadLicenses()`. */
    private suspend fun loadLicenses() {
        if (!currentState.hasProfile) return
        setState { copy(isLoadingLicenses = true) }
        userRepository.getLicenses().fold(
            onSuccess = { licenses ->
                setState { copy(licenses = licenses, isLoadingLicenses = false) }
            },
            onFailure = { e ->
                setState { copy(isLoadingLicenses = false, initLoadException = e) }
            },
        )
    }

    /** ≈ phần `refreshToken()` trong `CommonBloc.init()` bên Flutter. */
    private suspend fun refreshToken() {
        if (authRepository.isLoggedIn()) authRepository.refreshToken()
    }

    /** ≈ `AuthBloc.onSignInSucceed(user)`. */
    private suspend fun onSignInSucceeded(user: User, profiles: List<Profile>) {
        val allProfiles = profiles.ifEmpty { user.profiles }
        val profile = resolveCurrentProfile(allProfiles)
        setState {
            copy(
                status = AuthStatus.AUTHENTICATED,
                user = user.copy(profiles = allProfiles),
                currentProfile = profile,
                licenses = emptyList(),
                isInitLoadFinished = true,
                initLoadException = null,
            )
        }
        // Chạy trên scope của store ⇒ không bị huỷ khi màn Login đóng.
        loadLicenses()
    }

    /** ≈ `AuthBloc.setProfile()` / `updateCurrentProfile()`. */
    private fun setProfile(profile: Profile) {
        tokenStore.activeProfileId = profile.id
        setState { copy(currentProfile = profile) }
    }

    /** ≈ `AuthBloc.signOut()`. */
    private suspend fun signOut() {
        authRepository.logout()
        setState { AuthState(status = AuthStatus.UNAUTHENTICATED, isInitLoadFinished = true) }
        sendEffect(AuthEffect.SignedOut)
    }

    /** Ghim lại con đang dùng (≈ `pinRememberedProfile`), fallback hồ sơ mặc định. */
    private fun resolveCurrentProfile(profiles: List<Profile>): Profile {
        val remembered = tokenStore.activeProfileId
        val profile = profiles.firstOrNull { it.id == remembered && remembered != 0 }
            ?: profiles.firstOrNull { it.isPrimary }
            ?: profiles.firstOrNull()
            ?: Profile()
        if (profile.id != 0) tokenStore.activeProfileId = profile.id
        return profile
    }
}
