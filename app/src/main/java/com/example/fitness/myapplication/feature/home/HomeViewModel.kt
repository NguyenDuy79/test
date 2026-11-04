package com.example.fitness.myapplication.feature.home

import androidx.lifecycle.viewModelScope
import com.example.fitness.myapplication.core.appstate.auth.AuthIntent
import com.example.fitness.myapplication.core.appstate.auth.AuthStore
import com.example.fitness.myapplication.core.appstate.common.CommonStore
import com.example.fitness.myapplication.core.appstate.common.withGlobalLoading
import com.example.fitness.myapplication.core.base.BaseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Ví dụ ViewModel màn hình dùng state app-wide:
 * - Subscribe [AuthStore.state] trong `viewModelScope` (≈ Cubit local listen `AuthBloc.stream`),
 *   map sang state riêng của màn → UI chỉ phụ thuộc [HomeState].
 * - Action app-wide = dispatch intent vào store, bọc global loading của [CommonStore].
 * - `viewModelScope` tự huỷ subscription khi màn đóng (≈ `close()` cancel).
 */
class HomeViewModel(
    private val authStore: AuthStore,
    private val commonStore: CommonStore,
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(
    initialState = HomeState()
) {

    init {
        viewModelScope.launch {
            authStore.state
                .map { auth ->
                    HomeState(
                        profileName = auth.currentProfile.name.ifEmpty { auth.user.fullName },
                        phone = auth.user.phone,
                        profileCount = auth.user.profiles.size,
                        activeLicenseCount = auth.licenses.count { it.isActive },
                        isLoadingLicenses = auth.isLoadingLicenses,
                    )
                }
                .distinctUntilChanged()
                .collect { mapped -> setState { mapped } }
        }
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            // Đang refresh thì bỏ qua các lần kéo-làm-mới tiếp theo.
            is HomeIntent.Refresh -> launchDroppable(intent) {
                commonStore.withGlobalLoading {
                    authStore.dispatch(AuthIntent.LoadUser).join()
                    authStore.dispatch(AuthIntent.LoadLicenses).join()
                }
            }

            is HomeIntent.SignOut -> launchDroppable(intent) {
                commonStore.withGlobalLoading { authStore.dispatch(AuthIntent.SignOut).join() }
                // Không tự điều hướng: root nhận AuthEffect.SignedOut và đưa về Login.
                sendEffect(HomeEffect.ShowToast("Đã đăng xuất"))
            }
        }
    }
}
