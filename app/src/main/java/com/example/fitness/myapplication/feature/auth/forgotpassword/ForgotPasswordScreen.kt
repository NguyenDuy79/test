package com.example.fitness.myapplication.feature.auth.forgotpassword

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness.myapplication.core.designsystem.component.EdupiaTopBar
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    ForgotPasswordContent(
        state = state,
        onIntent = viewModel::sendIntent,
        onBack = onNavigateBack
    )
}

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            EdupiaTopBar(
                title = "Quên Mật Khẩu",
                onBackClick = onBack
            )
        },
        containerColor = EdupiaColors.BackgroundWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Skeleton màn hình Quên Mật Khẩu (chưa code nội dung)
            Text(text = "Forgot Password Screen (Skeleton)")
        }
    }
}

@Preview(showBackground = true, name = "ForgotPassword Screen")
@Composable
fun ForgotPasswordScreenPreview() {
    EdupiaTheme {
        ForgotPasswordContent(
            state = ForgotPasswordState(),
            onIntent = {},
            onBack = {}
        )
    }
}
