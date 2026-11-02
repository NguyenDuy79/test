package com.example.fitness.myapplication.feature.home

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class HomeState(
    val profileName: String = "",
    val phone: String = "",
    val profileCount: Int = 0,
    val activeLicenseCount: Int = 0,
    val isLoadingLicenses: Boolean = false,
) : UiState

sealed interface HomeIntent : UiIntent {
    object Refresh : HomeIntent
    object SignOut : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class ShowToast(val message: String) : HomeEffect
}
