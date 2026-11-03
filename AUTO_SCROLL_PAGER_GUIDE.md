# 🚀 Hướng Dẫn CarouselSlider Chuẩn Flutter `carousel_slider: 5.1.2` Trong Jetpack Compose

Tài liệu này hướng dẫn chi tiết cách sử dụng và xây dựng **`CarouselSlider`** và **`SmoothPageIndicator`** trong **Jetpack Compose** với thiết kế và bộ API tương đương 100% với package **`carousel_slider: 5.1.2`** của **Flutter**.

---

## 🎯 1. Bảng So Sánh Tính Năng (Flutter vs Jetpack Compose)

| Tính Năng (Flutter `carousel_slider: 5.1.2`) | Jetpack Compose (`CarouselSlider`) | Mô Tả |
| :--- | :--- | :--- |
| `CarouselOptions` | `CarouselOptions` | Đối tượng chứa toàn bộ cấu hình hiển thị & hiệu ứng |
| `viewportFraction` | `options.viewportFraction` (vd: `0.8f`) | Tỉ lệ bề rộng trang chính (lộ 2 phần trang 2 bên) |
| `enableInfiniteScroll` | `options.enableInfiniteScroll` (`true`/`false`) | Cuộn vô hạn liên tục (0 → 1 → 2 → 0 → 1...) |
| `autoPlay` | `options.autoPlay` (`true`/`false`) | Tự động cuộn sang trang tiếp theo |
| `autoPlayInterval` | `options.autoPlayInterval` (ms) | Thời gian dừng giữa mỗi trang (mặc định `4000ms`) |
| `autoPlayAnimationDuration` | `options.autoPlayAnimationDuration` (ms) | Thời gian thực hiện hiệu ứng chuyển trang (`800ms`) |
| `autoPlayCurve` | `options.autoPlayCurve` | Easing curve của animation (`FastOutSlowInEasing`) |
| `enlargeCenterPage` | `options.enlargeCenterPage` (`true`/`false`) | Phóng to trang ở giữa so với các trang 2 bên |
| `enlargeFactor` | `options.enlargeFactor` (`0.3f`) | Mức độ thu nhỏ của các trang ở 2 bên |
| `enlargeStrategy` | `options.enlargeStrategy` | Chiến lược phóng to: `SCALE`, `HEIGHT`, `ZOOM` |
| `scrollDirection` | `options.scrollDirection` | Hướng cuộn: `HORIZONTAL` hoặc `VERTICAL` |
| `pauseAutoPlayOnTouch` | `options.pauseAutoPlayOnTouch` (`true`) | Tạm dừng tự động cuộn khi người dùng chạm/vuốt |
| `onPageChanged` | `options.onPageChanged` | Callback `(index, reason)` thông báo chuyển trang |
| `CarouselController` | `CarouselController` | Bộ điều khiển: `nextPage()`, `previousPage()`, `animateToPage()`, `jumpToPage()`, `startAutoPlay()`, `stopAutoPlay()` |
| `CarouselPageChangedReason` | `CarouselPageChangedReason` | Nguồn chuyển trang: `TIMED`, `MANUAL`, `CONTROLLER` |

---

## 🛠️ 2. Giải Thích Chi Tiết Từng Dòng Code `CarouselSlider.kt`

Tệp tin nằm tại: `core/designsystem/component/CarouselSlider.kt`

```kotlin
package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

// Enum định nghĩa nguyên nhân dẫn đến thay đổi trang
enum class CarouselPageChangedReason {
    TIMED,      // Do tự động đếm ngược thời gian (Auto-play)
    MANUAL,     // Do người dùng trực tiếp dùng ngón tay vuốt
    CONTROLLER  // Do lập trình viên gọi thông qua CarouselController
}

// Enum định nghĩa kiểu phóng to trang trung tâm
enum class CenterPageEnlargeStrategy {
    SCALE,  // Co giãn cả chiều rộng (width) và chiều cao (height)
    HEIGHT, // Chỉ co giãn chiều cao (height)
    ZOOM    // Phóng to co giãn kết hợp hiệu ứng mờ dần (Alpha Fade)
}

// Enum định nghĩa hướng cuộn
enum class CarouselScrollDirection {
    HORIZONTAL, // Cuộn ngang
    VERTICAL    // Cuộn dọc
}

// Class chứa các tùy chọn cấu hình tương đương CarouselOptions bên Flutter
data class CarouselOptions(
    val height: Dp? = null,                                             // Chiều cao cố định (nếu null sẽ tính theo aspectRatio)
    val aspectRatio: Float = 16f / 9f,                                  // Tỉ lệ khung hình mặc định 16:9
    val viewportFraction: Float = 0.8f,                                 // Tỉ lệ bề rộng trang chính (0.8f = chiếm 80% màn hình)
    val initialPage: Int = 0,                                           // Trang khởi tạo đầu tiên (mặc định 0)
    val enableInfiniteScroll: Boolean = true,                           // Cho phép cuộn vô hạn vòng tròn
    val reverse: Boolean = false,                                       // Đảo ngược hướng cuộn
    val autoPlay: Boolean = false,                                      // Tự động cuộn trang
    val autoPlayInterval: Long = 4000L,                                 // Thời gian dừng ở mỗi trang (4s)
    val autoPlayAnimationDuration: Long = 800L,                         // Thời gian hiệu ứng chuyển trang (800ms)
    val autoPlayCurve: Easing = FastOutSlowInEasing,                   // Đường cong mượt mà cho animation
    val enlargeCenterPage: Boolean = false,                             // Bật hiệu ứng phóng to trang ở giữa
    val enlargeFactor: Float = 0.3f,                                    // Tỉ lệ thu nhỏ của 2 trang bên cạnh (30%)
    val enlargeStrategy: CenterPageEnlargeStrategy = CenterPageEnlargeStrategy.SCALE, // Chiến lược phóng to
    val onPageChanged: ((index: Int, reason: CarouselPageChangedReason) -> Unit)? = null, // Callback nhận sự kiện đổi trang
    val scrollDirection: CarouselScrollDirection = CarouselScrollDirection.HORIZONTAL, // Hướng cuộn ngang/dọc
    val pauseAutoPlayOnTouch: Boolean = true,                          // Dừng tự động cuộn khi người dùng chạm tay vào
    val padEnds: Boolean = true,                                        // Padding ở 2 đầu khi không cuộn vô hạn
)

// Class điều khiển CarouselController tương đương bên Flutter
class CarouselController internal constructor(
    private val coroutineScope: CoroutineScope,                         // CoroutineScope để chạy animation bất đồng bộ
) {
    internal var pagerState: PagerState? = null                         // Giữ tham chiếu PagerState nội bộ
    internal var itemCountProvider: (() -> Int)? = null                // Hàm cung cấp tổng số phần tử
    internal var infiniteScrollProvider: (() -> Boolean)? = null       // Hàm kiểm tra có bật cuộn vô hạn không
    internal var lastReason: CarouselPageChangedReason = CarouselPageChangedReason.MANUAL // Nguyên nhân đổi trang gần nhất

    var isAutoPlayPlaying: Boolean by mutableStateOf(true)              // Trạng thái bật/tắt AutoPlay bằng code
        internal set

    // Tính toán chỉ số trang thực tế hiện tại (0 .. itemCount - 1)
    val realCurrentPage: Int
        get() {
            val state = pagerState ?: return 0
            val count = itemCountProvider?.invoke() ?: return 0
            if (count <= 0) return 0
            return (state.currentPage % count + count) % count           // Quy đổi chỉ số ảo thành chỉ số thực
        }

    // Chuyển tới trang chỉ định có animation
    fun animateToPage(page: Int, durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val targetReal = (page % count + count) % count                 // Tính trang thực tế mục tiêu
        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER          // Đánh dấu nguyên nhân do Controller
            if (isInfinite && count > 1) {
                val currentVirtual = state.currentPage
                val currentReal = (currentVirtual % count + count) % count
                val diff = targetReal - currentReal
                state.animateScrollToPage(                              // Cuộn trang ảo tới khoảng lệch diff
                    page = currentVirtual + diff,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            } else {
                state.animateScrollToPage(                              // Cuộn trực tiếp khi không vô hạn
                    page = targetReal,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            }
        }
    }

    // Chuyển sang trang tiếp theo
    fun nextPage(durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            if (isInfinite && count > 1) {
                state.animateScrollToPage(                              // Tiến thêm 1 trang ảo
                    page = state.currentPage + 1,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            } else {
                val next = (state.currentPage + 1) % count              // Tính trang kế tiếp
                state.animateScrollToPage(
                    page = next,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            }
        }
    }

    // Quay về trang phía trước
    fun previousPage(durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            if (isInfinite && count > 1) {
                state.animateScrollToPage(                              // Lùi 1 trang ảo
                    page = state.currentPage - 1,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            } else {
                val prev = if (state.currentPage - 1 < 0) count - 1 else state.currentPage - 1
                state.animateScrollToPage(
                    page = prev,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            }
        }
    }

    // Nhảy tới trang lập tức không có animation
    fun jumpToPage(page: Int) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val targetReal = (page % count + count) % count
        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            if (isInfinite && count > 1) {
                val currentVirtual = state.currentPage
                val currentReal = (currentVirtual % count + count) % count
                val diff = targetReal - currentReal
                state.scrollToPage(currentVirtual + diff)
            } else {
                state.scrollToPage(targetReal)
            }
        }
    }

    fun startAutoPlay() { isAutoPlayPlaying = true }                    // Bật chạy tự động
    fun stopAutoPlay() { isAutoPlayPlaying = false }                    // Tắt chạy tự động
}

// Khởi tạo CarouselController trong Composable
@Composable
fun rememberCarouselController(): CarouselController {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) { CarouselController(coroutineScope) }
}

// Component CarouselSlider cho danh sách dữ liệu items
@Composable
fun <T> CarouselSlider(
    items: List<T>,
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(),
    controller: CarouselController = rememberCarouselController(),
    itemContent: @Composable (item: T, index: Int, realIndex: Int) -> Unit,
) {
    CarouselSliderBuilder(
        itemCount = items.size,
        modifier = modifier,
        options = options,
        controller = controller,
        itemContent = { index, realIndex ->
            if (realIndex in items.indices) {
                itemContent(items[realIndex], index, realIndex)
            }
        },
    )
}

// Component CarouselSliderBuilder dạng dựng theo số lượng phần tử itemCount
@Composable
fun CarouselSliderBuilder(
    itemCount: Int,
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(),
    controller: CarouselController = rememberCarouselController(),
    itemContent: @Composable (index: Int, realIndex: Int) -> Unit,
) {
    if (itemCount <= 0) return                                          // Thoát sớm nếu mảng rỗng

    val isInfinite = options.enableInfiniteScroll && itemCount > 1      // Kiểm tra xem có cuộn vô hạn không
    val totalPages = if (isInfinite) Int.MAX_VALUE else itemCount      // Đặt số lượng trang ảo nếu vô hạn

    // Tính toán trang ảo khởi đầu nằm chính giữa dải Int.MAX_VALUE
    val initialVirtualPage = if (isInfinite) {
        val half = Int.MAX_VALUE / 2
        half - (half % itemCount) + options.initialPage.coerceIn(0, itemCount - 1)
    } else {
        options.initialPage.coerceIn(0, itemCount - 1)
    }

    // Khởi tạo PagerState của Jetpack Compose
    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage,
        pageCount = { totalPages },
    )

    // Liên kết thông tin vào controller
    controller.pagerState = pagerState
    controller.itemCountProvider = { itemCount }
    controller.infiniteScrollProvider = { isInfinite }

    // Theo dõi thao tác vuốt chạm bằng tay của người dùng
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Xử lý tự động chuyển trang AutoPlay
    LaunchedEffect(options.autoPlay, controller.isAutoPlayPlaying, isDragged, pagerState.currentPage, options.autoPlayInterval) {
        if (options.autoPlay && controller.isAutoPlayPlaying) {
            val shouldPause = options.pauseAutoPlayOnTouch && isDragged
            if (!shouldPause) {
                delay(options.autoPlayInterval)                        // Chờ hết khoảng thời gian dừng
                controller.lastReason = CarouselPageChangedReason.TIMED
                if (isInfinite) {
                    pagerState.animateScrollToPage(                    // Cuộn tới trang ảo tiếp theo
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(
                            durationMillis = options.autoPlayAnimationDuration.toInt(),
                            easing = options.autoPlayCurve,
                        ),
                    )
                } else {
                    val nextPage = (pagerState.currentPage + 1) % itemCount
                    pagerState.animateScrollToPage(                    // Cuộn tới trang thực tiếp theo
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = options.autoPlayAnimationDuration.toInt(),
                            easing = options.autoPlayCurve,
                        ),
                    )
                }
            }
        }
    }

    // Lắng nghe sự kiện chuyển trang và gọi callback onPageChanged
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            val realIndex = (page % itemCount + itemCount) % itemCount
            val reason = if (isDragged) CarouselPageChangedReason.MANUAL else controller.lastReason
            options.onPageChanged?.invoke(realIndex, reason)           // Phát sự kiện ra ngoài
            controller.lastReason = CarouselPageChangedReason.MANUAL
        }
    }

    // Tính toán chiều cao / AspectRatio cho container
    val containerModifier = modifier.then(
        if (options.height != null) Modifier.height(options.height) else Modifier.aspectRatio(options.aspectRatio)
    )

    // Đo kích thước màn hình để tính khoảng không nhô 2 bên theo viewportFraction
    BoxWithConstraints(modifier = containerModifier) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight

        // Tính khoảng đệm 2 bên trái/phải khi cuộn ngang
        val horizontalPadding = if (options.scrollDirection == CarouselScrollDirection.HORIZONTAL && options.viewportFraction < 1f) {
            parentWidth * ((1f - options.viewportFraction) / 2f)
        } else 0.dp

        // Tính khoảng đệm 2 bên trên/dưới khi cuộn dọc
        val verticalPadding = if (options.scrollDirection == CarouselScrollDirection.VERTICAL && options.viewportFraction < 1f) {
            parentHeight * ((1f - options.viewportFraction) / 2f)
        } else 0.dp

        val contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding)

        // Phân nhánh HorizontalPager (Cuộn ngang) hoặc VerticalPager (Cuộn dọc)
        if (options.scrollDirection == CarouselScrollDirection.HORIZONTAL) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                reverseLayout = options.reverse,
            ) { page ->
                val realIndex = (page % itemCount + itemCount) % itemCount
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            // Phóng to trang ở trung tâm nếu bật enlargeCenterPage
                            if (options.enlargeCenterPage) {
                                Modifier.graphicsLayer {
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).let { abs(it) }
                                    val scaleFraction = 1f - (pageOffset * options.enlargeFactor).coerceIn(0f, 1f)

                                    when (options.enlargeStrategy) {
                                        CenterPageEnlargeStrategy.SCALE -> {
                                            scaleX = scaleFraction
                                            scaleY = scaleFraction
                                        }
                                        CenterPageEnlargeStrategy.HEIGHT -> {
                                            scaleY = scaleFraction
                                        }
                                        CenterPageEnlargeStrategy.ZOOM -> {
                                            scaleX = scaleFraction
                                            scaleY = scaleFraction
                                            alpha = lerp(0.5f, 1.0f, 1f - pageOffset.coerceIn(0f, 1f))
                                        }
                                    }
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    itemContent(page, realIndex)                       // Vẽ nội dung item
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                reverseLayout = options.reverse,
            ) { page ->
                val realIndex = (page % itemCount + itemCount) % itemCount
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (options.enlargeCenterPage) {
                                Modifier.graphicsLayer {
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).let { abs(it) }
                                    val scaleFraction = 1f - (pageOffset * options.enlargeFactor).coerceIn(0f, 1f)

                                    when (options.enlargeStrategy) {
                                        CenterPageEnlargeStrategy.SCALE -> {
                                            scaleX = scaleFraction
                                            scaleY = scaleFraction
                                        }
                                        CenterPageEnlargeStrategy.HEIGHT -> {
                                            scaleY = scaleFraction
                                        }
                                        CenterPageEnlargeStrategy.ZOOM -> {
                                            scaleX = scaleFraction
                                            scaleY = scaleFraction
                                            alpha = lerp(0.5f, 1.0f, 1f - pageOffset.coerceIn(0f, 1f))
                                        }
                                    }
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    itemContent(page, realIndex)
                }
            }
        }
    }
}
```

---

## 🛠️ 3. Giải Thích Chi Tiết Từng Dòng Code `SmoothPageIndicator.kt`

Tệp tin nằm tại: `core/designsystem/component/SmoothPageIndicator.kt`

```kotlin
package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// Enum kiểu hiệu ứng chuyển động chấm chỉ báo
enum class IndicatorEffect {
    EXPANDING, // Mở rộng dạng viên thuốc
    SCALE,     // Phóng to đường kính
    WORM,      // Kéo dài liên tục theo tay vuốt
    SIMPLE     // Đổi màu đơn giản
}

@Composable
fun SmoothPageIndicator(
    pagerState: PagerState,                                             // Trạng thái PagerState
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,                              // Tổng số trang
    effect: IndicatorEffect = IndicatorEffect.EXPANDING,               // Kiểu hiệu ứng
    activeColor: Color = EdupiaColors.Primary,                          // Màu khi trang Active
    inactiveColor: Color = EdupiaColors.Neutral300,                     // Màu khi trang Inactive
    dotWidth: Dp = 8.dp,                                                // Chiều rộng chấm tiêu chuẩn
    dotHeight: Dp = 8.dp,                                               // Chiều cao chấm tiêu chuẩn
    expandedDotWidth: Dp = 24.dp,                                       // Chiều rộng khi mở rộng
    spacing: Dp = 8.dp,                                                 // Khoảng cách giữa các chấm
    onDotClick: ((Int) -> Unit)? = null,                                // Callback khi click vào chấm
) {
    // Xử lý số trang an toàn (Nếu cuộn vô hạn Int.MAX_VALUE thì dùng tham số pageCount truyền vào)
    val actualPageCount = if (pageCount == Int.MAX_VALUE || pageCount <= 0) {
        pagerState.pageCount
    } else {
        pageCount
    }

    if (actualPageCount <= 0 || actualPageCount == Int.MAX_VALUE) return // Thoát nếu số trang không hợp lệ

    // Quy đổi trang hiện tại thành chỉ số trang thực tế (0 .. actualPageCount - 1)
    val currentRealIndex = (pagerState.currentPage % actualPageCount + actualPageCount) % actualPageCount

    // Dựng hàng chứa danh sách các chấm
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),         // Đặt khoảng cách đều giữa các chấm
        verticalAlignment = Alignment.CenterVertically,                // Căn giữa theo chiều dọc
    ) {
        // Lặp vẽ từng chấm tương ứng với mỗi trang thực tế
        repeat(actualPageCount) { index ->
            // Tính độ lệch khoảng cách vuốt giữa ngón tay và trang hiện tại
            val pageOffset = (
                (currentRealIndex - index) + pagerState.currentPageOffsetFraction
            ).let { abs(it) }

            // Kiểm tra chấm hiện tại có đang Active không
            val isSelected = index == currentRealIndex

            // Animation chuyển đổi chiều rộng mượt mà bằng animateDpAsState
            val width by animateDpAsState(
                targetValue = when (effect) {
                    IndicatorEffect.EXPANDING -> if (isSelected) expandedDotWidth else dotWidth // Mở rộng nếu chọn
                    IndicatorEffect.WORM -> {                                                   // Co giãn liên tục theo tay vuốt
                        val fraction = (1f - pageOffset.coerceIn(0f, 1f))
                        dotWidth + (expandedDotWidth - dotWidth) * fraction
                    }
                    else -> dotWidth
                },
                animationSpec = tween(durationMillis = 300),           // Thời gian hiệu ứng 300ms
                label = "indicator_width",
            )

            // Animation chuyển đổi chiều cao mượt mà
            val height by animateDpAsState(
                targetValue = when (effect) {
                    IndicatorEffect.SCALE -> if (isSelected) dotHeight * 1.5f else dotHeight   // Phóng to 1.5 lần nếu chọn
                    else -> dotHeight
                },
                animationSpec = tween(durationMillis = 300),
                label = "indicator_height",
            )

            // Animation đổi màu sắc mượt mà
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor, // Đổi sang activeColor nếu chọn
                animationSpec = tween(durationMillis = 300),
                label = "indicator_color",
            )

            // Dựng chấm chỉ báo bằng Box
            Box(
                modifier = Modifier
                    .height(height)                                     // Chiều cao animated
                    .width(width)                                       // Chiều rộng animated
                    .clip(CircleShape)                                  // Bo tròn hình viên thuốc/tròn
                    .background(color)                                  // Đổ màu animated
                    .then(
                        if (onDotClick != null) {
                            Modifier.clickable { onDotClick(index) }     // Click chuyển trang
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}
```

---

## 🔍 4. Giải Thích Cơ Chế Kỹ Thuật

1. **Cuộn vô hạn (`enableInfiniteScroll = true`)**:
   - Khởi tạo `PagerState` với `pageCount = Int.MAX_VALUE`.
   - Trang bắt đầu ở vị trí giữa `(Int.MAX_VALUE / 2)`.
   - Tính toán chỉ số trang thực tế: `realIndex = (page % itemCount + itemCount) % itemCount`.

2. **Cấu hình tỉ lệ hiển thị `viewportFraction` (vd: `0.8f`)**:
   - Sử dụng `BoxWithConstraints` để tính toán khoảng padding 2 bên:
     `horizontalPadding = maxWidth * ((1f - viewportFraction) / 2f)`.
   - Giúp các trang 2 bên nhô ra một khoảng tự nhiên giống hệt Flutter.

3. **Hiệu ứng Phóng to Trang Trung Tâm (`enlargeCenterPage`)**:
   - Sử dụng `Modifier.graphicsLayer` biến đổi `scaleX`, `scaleY` và `alpha` dựa trên `currentPageOffsetFraction`.
   - Hỗ trợ 3 chế độ `SCALE`, `HEIGHT`, `ZOOM`.

4. **Tự động tạm dừng khi vuốt chạm (`pauseAutoPlayOnTouch`)**:
   - Đọc trạng thái vuốt ngón tay từ `pagerState.interactionSource.collectIsDraggedAsState()`.
   - Khi `isDragged == true`, Coroutine đếm ngược thời gian sẽ tạm dừng để tránh xung đột thao tác của người dùng.
