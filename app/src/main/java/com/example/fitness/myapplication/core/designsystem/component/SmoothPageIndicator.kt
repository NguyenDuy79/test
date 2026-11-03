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
    EXPANDING, // Mở rộng chấm active thành dạng viên thuốc (capsule / pill)
    SCALE,     // Phóng to kích thước đường kính chấm active
    WORM,      // Kéo dài chuyển động liên tục theo khoảng cách vuốt ngón tay
    SIMPLE,    // Đổi màu sắc đơn giản không thay đổi hình dạng
}

/**
 * Reusable Smooth Page Indicator Component cho Jetpack Compose.
 *
 * @param pagerState Trạng thái PagerState của HorizontalPager hoặc CarouselSlider
 * @param modifier Modifier tùy chỉnh khoảng cách hoặc vị trí
 * @param pageCount Số lượng trang thực tế (mặc định lấy từ pagerState.pageCount)
 * @param effect Kiểu hiệu ứng chuyển động chấm chỉ báo (EXPANDING, SCALE, WORM, SIMPLE)
 * @param activeColor Màu sắc đại diện cho trang đang được chọn (Active)
 * @param inactiveColor Màu sắc cho các trang chưa chọn (Inactive)
 * @param dotWidth Chiều rộng tiêu chuẩn cho mỗi chấm khi ở trạng thái Inactive
 * @param dotHeight Chiều cao tiêu chuẩn cho mỗi chấm
 * @param expandedDotWidth Chiều rộng tối đa khi chấm mở rộng (cho kiểu EXPANDING/WORM)
 * @param spacing Khoảng cách giữa các chấm chỉ báo liền kề
 * @param onDotClick Hành động khi người dùng nhấn trực tiếp vào từng chấm chỉ báo
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
    // Kiểm tra số lượng trang hợp lệ (Nếu cuộn vô hạn với Int.MAX_VALUE thì dùng tham số pageCount truyền vào)
    val actualPageCount = if (pageCount == Int.MAX_VALUE || pageCount <= 0) {
        pagerState.pageCount
    } else {
        pageCount
    }

    // Nếu không có trang nào hoặc số trang vô hạn chưa được xử lý thì thoát sớm
    if (actualPageCount <= 0 || actualPageCount == Int.MAX_VALUE) return

    // Tính toán chỉ số trang thực tế nằm trong dải [0 .. actualPageCount - 1]
    val currentRealIndex = (pagerState.currentPage % actualPageCount + actualPageCount) % actualPageCount

    // Dựng hàng ngang chứa danh sách các chấm chỉ báo trang
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing), // Đặt khoảng cách giữa các chấm
        verticalAlignment = Alignment.CenterVertically,        // Căn giữa theo chiều dọc
    ) {
        // Lặp vẽ từng chấm chỉ báo đại diện cho từng trang thực tế
        repeat(actualPageCount) { index ->
            // Tính toán khoảng cách lệch giữa trang hiện tại và chấm chỉ báo thứ `index`
            val pageOffset = (
                (currentRealIndex - index) + pagerState.currentPageOffsetFraction
            ).let { abs(it) }

            // Xกำหนด chấm hiện tại có đang được chọn (Active) hay không
            val isSelected = index == currentRealIndex

            // Animation co giãn chiều rộng chấm chỉ báo mượt mà bằng animateDpAsState
            val width by animateDpAsState(
                targetValue = when (effect) {
                    // Kiểu EXPANDING: Chấm active sẽ mở rộng thành chiều rộng expandedDotWidth
                    IndicatorEffect.EXPANDING -> if (isSelected) expandedDotWidth else dotWidth
                    // Kiểu WORM: Chiều rộng thay đổi liên tục theo tỉ lệ vuốt ngón tay (fraction)
                    IndicatorEffect.WORM -> {
                        val fraction = (1f - pageOffset.coerceIn(0f, 1f))
                        dotWidth + (expandedDotWidth - dotWidth) * fraction
                    }
                    // Các kiểu khác giữ nguyên chiều rộng tiêu chuẩn dotWidth
                    else -> dotWidth
                },
                animationSpec = tween(durationMillis = 300), // Thời gian chạy animation 300ms
                label = "indicator_width",
            )

            // Animation co giãn chiều cao chấm chỉ báo mượt mà
            val height by animateDpAsState(
                targetValue = when (effect) {
                    // Kiểu SCALE: Chấm active sẽ phóng to 1.5 lần chiều cao tiêu chuẩn
                    IndicatorEffect.SCALE -> if (isSelected) dotHeight * 1.5f else dotHeight
                    // Các kiểu khác giữ nguyên chiều cao dotHeight
                    else -> dotHeight
                },
                animationSpec = tween(durationMillis = 300),
                label = "indicator_height",
            )

            // Animation chuyên đổi màu sắc chấm chỉ báo từ inactiveColor sang activeColor mượt mà
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 300),
                label = "indicator_color",
            )

            // Dựng hình dạng hình tròn / capsule cho từng chấm chỉ báo
            Box(
                modifier = Modifier
                    .height(height)           // Áp dụng chiều cao đã có animation
                    .width(width)             // Áp dụng chiều rộng đã có animation
                    .clip(CircleShape)        // Bo tròn các góc thành hình tròn/capsule
                    .background(color)        // Đổ màu background đã animation
                    .then(
                        // Nếu có truyền sự kiện click thì thêm hiệu ứng nhấn clickable
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
