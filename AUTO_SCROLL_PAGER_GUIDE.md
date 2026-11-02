# 🚀 Hướng Dẫn Làm Auto-Scrolling PageView + Tự Động Đổi Text Phía Dưới Trong Jetpack Compose

Tài liệu này hướng dẫn chi tiết cách xây dựng một **Auto-Scrolling PageView** (Banner / Slider tự động chuyển trang) trong **Jetpack Compose** với các tính năng:
1. **Tự động chạy (Auto-scroll)** sau một khoảng thời gian thiết lập.
2. **Tạm dừng & Reset bộ đếm thời gian khi chạm/vuốt**: Khi người dùng chạm ngón tay vào cuộn Slider, bộ đếm thời gian dừng lại. Khi nhả tay ra, thời gian được đếm lại từ đầu.
3. **Phần Text phía dưới tự động đổi theo trang**: Khi Pager tự động chạy hoặc khi người dùng vuốt ngón tay chuyển trang, đoạn Text (Tiêu đề, Mô tả) ở phía dưới sẽ **tự động chuyển đổi mượt mà với hiệu ứng Animation** (`AnimatedContent` với `fadeIn` + `slideInVertically`).
4. **Hiệu ứng chuyển trang sinh động (Page Transformer)**: Phóng to / mờ dần các trang khi cuộn (`Scale & Alpha Animation`).

---

## 🛠️ 1. Nguyên Lý Kỹ Thuật Tự Động Đổi Text Khi Vuốt

- **Tự động đồng bộ Text theo `pagerState.currentPage`**: 
  Sử dụng `AnimatedContent(targetState = pagerState.currentPage)` để quan sát trang hiện tại của Pager. Mỗi khi `pagerState.currentPage` thay đổi (do tự động cuộn hoặc do ngón tay người dùng vuốt), `AnimatedContent` sẽ tự động kích hoạt hiệu ứng trượt chữ mượt mà.
- **Hiệu ứng trượt chữ (Text Transition)**:
  `transitionSpec` kết hợp giữa `fadeIn` + `slideInVertically` và `fadeOut` + `slideOutVertically` tạo cảm giác văn bản phía dưới trượt lên mượt mà theo từng slider.

---

## 💻 2. Mã Nguồn Hoàn Chỉnh (`AutoScrollPager.kt`)

Component `AutoScrollPagerWithText` đã được cài đặt sẵn trong dự án tại file `core/designsystem/component/AutoScrollPager.kt`:

```kotlin
package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Component HorizontalPager tự động cuộn trang.
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

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged, pagerState.currentPage) {
        if (!isDragged) {
            delay(autoScrollDurationMs)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
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
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleX = scale
                            scaleY = scale

                            alpha = lerp(
                                start = 0.5f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            pageContent(items[page], page)
        }
    }
}

/**
 * Component AutoScrollPager kết hợp tự động chuyển trang VÀ tự động cập nhật Text phía dưới mượt mà khi vuốt/tự cuộn.
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Phần Slider Banner tự động cuộn
        AutoScrollPager(
            items = items,
            pagerState = pagerState,
            autoScrollDurationMs = autoScrollDurationMs,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            pageContent = pagerContent
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Smooth Indicator (Worm / Expanding Dot)
        SmoothPageIndicator(
            pagerState = pagerState,
            effect = indicatorEffect
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Phần Text phía dưới tự động chuyển đổi mượt mà theo trang khi vuốt / tự cuộn
        AnimatedContent(
            targetState = pagerState.currentPage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(350)) + slideInVertically { height -> height / 2 }) togetherWith
                (fadeOut(animationSpec = tween(350)) + slideOutVertically { height -> -height / 2 })
            },
            label = "pager_text_animation"
        ) { targetPage ->
            val safePage = targetPage.coerceIn(0, items.size - 1)
            val currentItem = items[safePage]
            bottomTextContent(currentItem, safePage)
        }
    }
}
```

---

## 📖 3. Ví Dụ Sử Dụng Trong Màn Hình

```kotlin
data class OnboardingData(val title: String, val desc: String, val imageRes: Int)

@Composable
fun OnboardingSliderExample() {
    val listData = listOf(
        OnboardingData("Học Tiếng Anh Với AI", "Luyện phản xạ 1:1 cùng giáo viên AI Edupia.", R.drawable.image_login),
        OnboardingData("Lộ Trình Chuẩn Quốc Tế", "Chương trình học bám sát khung tham chiếu CEFR.", R.drawable.logo_splash),
        OnboardingData("Học Mà Chơi - Chơi Mà Học", "Bài học tương tác sinh động giúp bé say mê.", R.drawable.bg_onboarding)
    )

    AutoScrollPagerWithText(
        items = listData,
        autoScrollDurationMs = 3000L,
        pagerContent = { item, _ ->
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize()
            )
        },
        bottomTextContent = { item, _ ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = item.desc, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}
```

---

## 🔍 4. Giải Thích Luồng Cập Nhật Text

1. **Khi Pager tự động chuyển trang**: `pagerState.currentPage` đổi từ `0 -> 1`.
2. **Khi người dùng vuốt ngón tay**: `pagerState.currentPage` cập nhật theo trang gần nhất.
3. **`AnimatedContent` lắng nghe `pagerState.currentPage`**: Khi thấy giá trị `currentPage` thay đổi, nó sẽ kích hoạt animation đẩy văn bản cũ biến mất và đẩy văn bản mới hiện lên từ bên dưới cực kỳ mượt mà.
