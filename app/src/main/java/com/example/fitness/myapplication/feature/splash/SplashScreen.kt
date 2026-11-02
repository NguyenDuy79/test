package com.example.fitness.myapplication.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SplashEffect.NavigateToHome -> onNavigateToHome()
                is SplashEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    SplashContent(state = state)
}

@Composable
fun SplashContent(
    state: SplashState,
) {
    Scaffold(
        containerColor = EdupiaColors.BackgroundWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EdupiaColors.BackgroundWhite),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_splash),
                    contentDescription = "Edupia Logo Splash",
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                CircularProgressIndicator(
                    color = EdupiaColors.Primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Bottom branding text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Edupia AI • Tiếng Anh Chất Lượng Cao",
                    style = EdupiaTypography.bodyLarge,
                    color = EdupiaColors.Neutral500
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Splash Screen")
@Composable
fun SplashScreenPreview() {
    EdupiaTheme {
        SplashContent(
            state = SplashState()
        )
    }
}
