package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Component HorizontalPager tự động cuộn trang với hiệu ứng mượt mà và tự reset thời gian khi tương tác.
 *
 * @param items Danh sách dữ liệu mẫu
 * @param autoScrollDurationMs Thời gian tự chuyển trang (mặc định 3000ms = 3s)
 * @param enablePageTransformer Có bật hiệu ứng scale/alpha khi cuộn hay không
 * @param pageContent Layout giao diện cho từng trang
 */
@Composable
fun <T> AutoScrollPager(
    items: List<T>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(pageCount = { items.size }),
    autoScrollDurationMs: Long = 3000L,
    enablePageTransformer: Boolean = true,
    pageContent: @Composable (item: T, page: Int) -> Unit,
) {
    if (items.isEmpty()) return

    // Theo dõi người dùng có đang vuốt ngón tay trực tiếp trên Slider không
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Coroutine tự động chuyển trang & tự reset khi chạm/vuốt
    LaunchedEffect(isDragged, pagerState.currentPage) {
        // Chỉ tự cuộn nếu người dùng KHÔNG chạm tay vào slider
        if (!isDragged) {
            delay(autoScrollDurationMs)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enablePageTransformer) {
                        Modifier.graphicsLayer {
                            val pageOffset = (
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).let { abs(it) }

                            val scale = lerp(
                                start = 0.85f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                            )
                            scaleX = scale
                            scaleY = scale

                            alpha = lerp(
                                start = 0.5f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            pageContent(items[page], page)
        }
    }
}

/**
 * Component AutoScrollPager kết hợp tự động chuyển trang VÀ tự động cập nhật Text phía dưới mượt mà khi vuốt/tự cuộn.
 *
 * @param items Danh sách phần tử
 * @param autoScrollDurationMs Thời gian đếm ngược tự chuyển trang
 * @param pagerContent Layout của phần hình ảnh/banner phía trên
 * @param bottomTextContent Layout của phần chữ/text phía dưới tự chuyển đổi theo trang
 */
@Composable
fun <T> AutoScrollPagerWithText(
    items: List<T>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(pageCount = { items.size }),
    autoScrollDurationMs: Long = 3000L,
    indicatorEffect: IndicatorEffect = IndicatorEffect.WORM,
    pagerContent: @Composable (item: T, page: Int) -> Unit,
    bottomTextContent: @Composable (item: T, page: Int) -> Unit,
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Phần Slider Banner tự động cuộn
        AutoScrollPager(
            items = items,
            pagerState = pagerState,
            autoScrollDurationMs = autoScrollDurationMs,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            pageContent = pagerContent,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Smooth Indicator
        SmoothPageIndicator(
            pagerState = pagerState,
            effect = indicatorEffect,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Phần Text phía dưới tự động thay đổi theo trang với hiệu ứng trượt mượt mà (AnimatedContent)
        AnimatedContent(
            targetState = pagerState.currentPage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(350)) + slideInVertically { height -> height / 2 }) togetherWith
                    (fadeOut(animationSpec = tween(350)) + slideOutVertically { height -> -height / 2 })
            },
            label = "pager_text_animation",
        ) { targetPage ->
            val safePage = targetPage.coerceIn(0, items.size - 1)
            val currentItem = items[safePage]
            bottomTextContent(currentItem, safePage)
        }
    }
}

data class SampleOnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int,
)

@Preview(showBackground = true, name = "AutoScrollPagerWithText Preview")
@Composable
fun AutoScrollPagerWithTextPreview() {
    val sampleList = listOf(
        SampleOnboardingItem(
            title = "Học Tiếng Anh Với AI",
            description = "Luyện phản xạ 1:1 cùng giáo viên AI Edupia mọi lúc mọi nơi.",
            imageRes = R.drawable.image_login,
        ),
        SampleOnboardingItem(
            title = "Lộ Trình Chuẩn Quốc Tế",
            description = "Chương trình học chuẩn khung Châu Âu CEFR & Bộ Giáo Dục.",
            imageRes = R.drawable.logo_splash,
        ),
        SampleOnboardingItem(
            title = "Học Mà Chơi - Chơi Mà Học",
            description = "Hệ thống bài học tương tác sinh động giúp bé say mê.",
            imageRes = R.drawable.bg_onboarding,
        ),
    )

    EdupiaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EdupiaColors.BackgroundWhite)
                .padding(24.dp),
        ) {
            AutoScrollPagerWithText(
                items = sampleList,
                autoScrollDurationMs = 3000L,
                pagerContent = { item, _ ->
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(EdupiaShapes.ShapeLarge),
                        contentScale = ContentScale.Fit,
                    )
                },
                bottomTextContent = { item, _ ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = item.title,
                            style = EdupiaTypography.headlineMedium,
                            color = EdupiaColors.Neutral900,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.description,
                            style = EdupiaTypography.bodyMedium,
                            color = EdupiaColors.Neutral600,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
            )
        }
    }
}
