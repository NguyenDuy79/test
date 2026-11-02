package com.example.fitness.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.feature.auth.forgotpassword.ForgotPasswordScreen
import com.example.fitness.myapplication.feature.auth.forgotpassword.ForgotPasswordViewModel
import com.example.fitness.myapplication.feature.auth.login.LoginScreen
import com.example.fitness.myapplication.feature.auth.login.LoginViewModel
import com.example.fitness.myapplication.feature.auth.signup.SignUpScreen
import com.example.fitness.myapplication.feature.auth.signup.SignUpViewModel
import com.example.fitness.myapplication.feature.home.HomeScreen
import com.example.fitness.myapplication.feature.home.HomeViewModel
import com.example.fitness.myapplication.feature.onboarding.OnboardingScreen
import com.example.fitness.myapplication.feature.onboarding.OnboardingViewModel
import com.example.fitness.myapplication.feature.splash.SplashScreen
import com.example.fitness.myapplication.feature.splash.SplashViewModel

enum class AppDestination {
    SPLASH,
    LOGIN,
    SIGN_UP,
    FORGOT_PASSWORD,
    ONBOARDING,
    HOME,
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as EdupiaApplication).container

        setContent {
            EdupiaTheme {
                var currentScreen by remember { mutableStateOf(AppDestination.SPLASH) }

                // Bọc toàn app bằng AuthStore + CommonStore (≈ MultiBlocProvider ở MyApp).
                EdupiaAppRoot(
                    authStore = container.authStore,
                    commonStore = container.commonStore,
                    onSignedOut = { currentScreen = AppDestination.LOGIN },
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "AppScreenTransition"
                    ) { destination ->
                        when (destination) {
                            AppDestination.SPLASH -> SplashScreen(
                                viewModel = viewModel {
                                    SplashViewModel(
                                        authStore = container.authStore,
                                        commonStore = container.commonStore,
                                    )
                                },
                                onNavigateToOnboarding = { currentScreen = AppDestination.ONBOARDING },
                                onNavigateToHome = { currentScreen = AppDestination.HOME },
                            )

                            AppDestination.LOGIN -> LoginScreen(
                                viewModel = viewModel {
                                    LoginViewModel(
                                        authRepository = container.authRepository,
                                        authStore = container.authStore,
                                    )
                                },
                                onNavigateToHome = { currentScreen = AppDestination.HOME },
                                onNavigateToSignUp = { currentScreen = AppDestination.SIGN_UP },
                                onNavigateToForgotPassword = {
                                    currentScreen = AppDestination.FORGOT_PASSWORD
                                },
                            )

                            AppDestination.SIGN_UP -> SignUpScreen(
                                viewModel = viewModel {
                                    SignUpViewModel(authRepository = container.authRepository)
                                },
                                onNavigateToLogin = { currentScreen = AppDestination.LOGIN },
                            )

                            AppDestination.FORGOT_PASSWORD -> ForgotPasswordScreen(
                                viewModel = viewModel<ForgotPasswordViewModel>(),
                                onNavigateBack = { currentScreen = AppDestination.LOGIN },
                            )

                            AppDestination.ONBOARDING -> OnboardingScreen(
                                viewModel = viewModel<OnboardingViewModel>(),
                                onNavigateToLogin = { currentScreen = AppDestination.LOGIN },
                                onNavigateToSignUp = { currentScreen = AppDestination.SIGN_UP },
                            )

                            AppDestination.HOME -> HomeScreen(
                                viewModel = viewModel {
                                    HomeViewModel(
                                        authStore = container.authStore,
                                        commonStore = container.commonStore,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
