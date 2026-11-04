package com.example.fitness.myapplication.feature.onboarding

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness.myapplication.core.designsystem.component.CarouselOptions
import com.example.fitness.myapplication.core.designsystem.component.CarouselSliderWithText
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.component.IndicatorEffect
import com.example.fitness.myapplication.core.designsystem.component.rememberCarouselController
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingContent(
        state = state,
        onIntent = viewModel::onIntent,
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
    // Khởi tạo CarouselController điều khiển CarouselSlider chuẩn Flutter
    val carouselController = rememberCarouselController()

    // Lắng nghe Effect từ ViewModel để điều khiển cuộn trang hoặc chuyển màn hình
    LaunchedEffect(effectFlow) {
        effectFlow?.collectLatest { effect ->
            when (effect) {
                is OnboardingEffect.ScrollToPage -> {
                    carouselController.animateToPage(effect.page)
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
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {



            // CarouselSlider tích hợp Smooth Indicator & Text Animation trượt phía dưới
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CarouselSliderWithText(
                    items = state.pages,
                    modifier = Modifier.fillMaxSize(),
                    controller = carouselController,
                    // Indicator + chữ giữ kích thước cố định, ảnh chiếm toàn bộ phần còn lại
                    imageFillsRemainingSpace = true,
                    bottomContentHeight = 120.dp,
                    options = CarouselOptions(
                        viewportFraction = 1f,
                        autoPlay = true,                    // Tự động cuộn trang
                        autoPlayInterval = 3500L,           // 3.5 giây đổi trang
                        enableInfiniteScroll = true,        // Cuộn vô hạn vòng tròn
                        initialPage = state.currentPage,
                        onPageChanged = { index, _ ->
                            onIntent(OnboardingIntent.PageChanged(index))
                        },
                    ),
                    indicatorEffect = IndicatorEffect.WORM,
                    dotWidth = 8.dp,
                    dotHeight = 8.dp,
                    indicatorTopSpacing = 16.dp,
                    textTopSpacing = 16.dp,
                    itemContent = { item, _, _ ->
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(EdupiaShapes.ShapeLarge),
                            contentScale = ContentScale.Crop,
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
                                color = EdupiaColors.Primary600,
                            )
                        }
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
