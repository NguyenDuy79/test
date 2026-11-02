package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlin.math.abs

/**
 * Các loại hiệu ứng chỉ báo trang (Page Indicator Animation Effects)
 * tương tự như thư viện `smooth_page_indicator` bên Flutter.
 */
enum class IndicatorEffect {
    EXPANDING, // Mở rộng chấm active thành dạng capsule
    SCALE,     // Phóng to kích thước chấm active
    WORM,      // Kéo dài liên tục theo khoảng cách vuốt ngón tay
    SIMPLE,    // Đổi màu đơn giản
}

/**
 * Reusable Smooth Page Indicator Component cho Jetpack Compose.
 *
 * @param pagerState PagerState của HorizontalPager
 * @param pageCount Số lượng trang
 * @param effect Kiểu hiệu ứng animation (EXPANDING, SCALE, WORM, SIMPLE)
 * @param activeColor Màu chấm active
 * @param inactiveColor Màu chấm inactive
 * @param dotWidth Chiều rộng chuẩn của chấm
 * @param dotHeight Chiều cao chuẩn của chấm
 * @param expandedDotWidth Chiều rộng tối đa khi mở rộng (cho kiểu EXPANDING/WORM)
 * @param spacing Khoảng cách giữa các chấm
 * @param onDotClick Action khi click vào từng chấm chỉ báo
 */
@Composable
fun SmoothPageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,
    effect: IndicatorEffect = IndicatorEffect.EXPANDING,
    activeColor: Color = EdupiaColors.Primary,
    inactiveColor: Color = EdupiaColors.Neutral300,
    dotWidth: Dp = 8.dp,
    dotHeight: Dp = 8.dp,
    expandedDotWidth: Dp = 24.dp,
    spacing: Dp = 8.dp,
    onDotClick: ((Int) -> Unit)? = null,
) {
    if (pageCount <= 0) return

    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val pageOffset = (
                (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
            ).let { abs(it) }

            val isSelected = index == pagerState.currentPage

            val width by animateDpAsState(
                targetValue = when (effect) {
                    IndicatorEffect.EXPANDING -> if (isSelected) expandedDotWidth else dotWidth
                    IndicatorEffect.WORM -> {
                        val fraction = (1f - pageOffset.coerceIn(0f, 1f))
                        dotWidth + (expandedDotWidth - dotWidth) * fraction
                    }
                    else -> dotWidth
                },
                animationSpec = tween(durationMillis = 300),
                label = "indicator_width",
            )

            val height by animateDpAsState(
                targetValue = when (effect) {
                    IndicatorEffect.SCALE -> if (isSelected) dotHeight * 1.5f else dotHeight
                    else -> dotHeight
                },
                animationSpec = tween(durationMillis = 300),
                label = "indicator_height",
            )

            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 300),
                label = "indicator_color",
            )

            Box(
                modifier = Modifier
                    .height(height)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (onDotClick != null) {
                            Modifier.clickable { onDotClick(index) }
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmoothPageIndicatorPreview() {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })

    EdupiaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Expanding Effect", style = EdupiaTypography.labelMedium)
            SmoothPageIndicator(pagerState = pagerState, effect = IndicatorEffect.EXPANDING)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Worm Effect", style = EdupiaTypography.labelMedium)
            SmoothPageIndicator(pagerState = pagerState, effect = IndicatorEffect.WORM)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Scale Effect", style = EdupiaTypography.labelMedium)
            SmoothPageIndicator(pagerState = pagerState, effect = IndicatorEffect.SCALE)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Simple Effect", style = EdupiaTypography.labelMedium)
            SmoothPageIndicator(pagerState = pagerState, effect = IndicatorEffect.SIMPLE)
        }
    }
}
