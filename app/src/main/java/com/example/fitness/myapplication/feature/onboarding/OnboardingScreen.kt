package com.example.fitness.myapplication.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    OnboardingContent(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Skeleton màn hình Onboarding (chưa có nội dung)
            Text(text = "Onboarding Screen (Skeleton)")
        }
    }
}
