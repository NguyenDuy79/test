package com.example.fitness.myapplication.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness.myapplication.core.designsystem.component.AutoScrollPagerWithText
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.component.IndicatorEffect
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    OnboardingContent(
        state = state,
        onIntent = viewModel::sendIntent,
        effectFlow = viewModel.effect,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSignUp = onNavigateToSignUp,
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    effectFlow: Flow<OnboardingEffect>? = null,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.pages.size },
    )

    // Đồng bộ Pager swipe với ViewModel State
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            onIntent(OnboardingIntent.PageChanged(page))
        }
    }

    // Lắng nghe Effect chuyển trang hoặc chuyển màn hình
    LaunchedEffect(effectFlow) {
        effectFlow?.collectLatest { effect ->
            when (effect) {
                is OnboardingEffect.ScrollToPage -> {
                    pagerState.animateScrollToPage(effect.page)
                }
                is OnboardingEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is OnboardingEffect.NavigateToSignUp -> {
                    onNavigateToSignUp()
                }
            }
        }
    }

    Scaffold(
        containerColor = EdupiaColors.BackgroundWhite,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Top Bar: Nút "Bỏ qua" (Skip)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (!state.isLastPage) {
                    Text(
                        text = "Bỏ qua",
                        style = EdupiaTypography.labelLarge,
                        color = EdupiaColors.Neutral600,
                        modifier = Modifier.clickable { onIntent(OnboardingIntent.Skip) },
                    )
                }
            }

            // Auto-Scrolling Pager + Smooth Indicator + Dynamic Animated Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                AutoScrollPagerWithText(
                    items = state.pages,
                    pagerState = pagerState,
                    autoScrollDurationMs = 3500L,
                    indicatorEffect = IndicatorEffect.WORM,
                    pagerContent = { item, _ ->
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(EdupiaShapes.ShapeLarge),
                            contentScale = ContentScale.Fit,
                        )
                    },
                    bottomTextContent = { item, _ ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = item.title,
                                style = EdupiaTypography.headlineMedium,
                                color = EdupiaColors.Neutral900,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = item.description,
                                style = EdupiaTypography.bodyMedium,
                                color = EdupiaColors.Neutral600,
                                textAlign = TextAlign.Center,
                                lineHeight = EdupiaTypography.bodyMedium.lineHeight,
                            )
                        }
                    },
                )
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.isLastPage) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        EdupiaButton(
                            text = "ĐĂNG NHẬP",
                            onClick = { onIntent(OnboardingIntent.NavigateToLogin) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(EdupiaShapes.ShapeMedium)
                                .border(1.dp, EdupiaColors.Primary, EdupiaShapes.ShapeMedium)
                                .clickable { onIntent(OnboardingIntent.NavigateToSignUp) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "ĐĂNG KÝ TÀI KHOẢN MỚI",
                                style = EdupiaTypography.labelLarge,
                                color = EdupiaColors.Primary,
                            )
                        }
                    }
                } else {
                    EdupiaButton(
                        text = "TIẾP TỤC",
                        onClick = { onIntent(OnboardingIntent.NextPage) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Onboarding Screen - First Page")
@Composable
fun OnboardingScreenPreview() {
    EdupiaTheme {
        OnboardingContent(
            state = OnboardingState(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding Screen - Last Page")
@Composable
fun OnboardingScreenLastPagePreview() {
    EdupiaTheme {
        OnboardingContent(
            state = OnboardingState(currentPage = 2),
            onIntent = {},
        )
    }
}
