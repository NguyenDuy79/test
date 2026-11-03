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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

/**
 * Enum định nghĩa các nguyên nhân làm thay đổi trang trong CarouselSlider.
 * Tương đương với `CarouselPageChangedReason` trong Flutter `carousel_slider: 5.1.2`.
 */
enum class CarouselPageChangedReason {
    TIMED,      // Trang thay đổi tự động do hết thời gian đếm ngược (Auto-play)
    MANUAL,     // Trang thay đổi do người dùng trực tiếp vuốt/chạm ngón tay trên màn hình
    CONTROLLER  // Trang thay đổi do gọi lệnh qua CarouselController (nextPage, animateToPage...)
}

/**
 * Enum định nghĩa chiến lược co giãn/phóng to trang ở giữa (Center Page).
 * Tương đương với `CenterPageEnlargeStrategy` trong Flutter `carousel_slider: 5.1.2`.
 */
enum class CenterPageEnlargeStrategy {
    SCALE,  // Co giãn đồng đều cả chiều ngang (scaleX) và chiều cao (scaleY)
    HEIGHT, // Chỉ co giãn chiều cao (scaleY), giữ nguyên chiều ngang
    ZOOM    // Phóng to co giãn kết hợp hiệu ứng làm mờ dần (Scale + Alpha Fade)
}

/**
 * Enum định nghĩa hướng cuộn của CarouselSlider.
 * Tương đương `scrollDirection: Axis.horizontal` hoặc `Axis.vertical` trong Flutter.
 */
enum class CarouselScrollDirection {
    HORIZONTAL, // Cuộn theo chiều ngang (Trái <-> Phải)
    VERTICAL    // Cuộn theo chiều dọc (Trên <-> Dưới)
}

/**
 * Class dữ liệu chứa toàn bộ các thuộc tính cấu hình cho `CarouselSlider`.
 * API chuẩn tương đương với `CarouselOptions` trong Flutter `carousel_slider: 5.1.2`.
 */
data class CarouselOptions(
    // Chiều cao cố định của Carousel (Nếu null sẽ tự động tính theo tỉ lệ aspectRatio)
    val height: Dp? = null,
    
    // Tỉ lệ khung hình (Chiều rộng / Chiều cao), mặc định 16:9 khi height = null
    // null (cùng với height = null) → Carousel không tự đặt kích thước, lấp đầy theo modifier truyền vào
    val aspectRatio: Float? = 16f / 9f,
    
    // Tỉ lệ kích thước item chính so với khung, trong khoảng (0, 1]:
    //  - 1.0f  = tràn viền: item chiếm trọn khung, không lộ trang bên cạnh
    //  - 0.8f  = item chiếm 80%, hé lộ 10% trang bên cạnh ở mỗi phía
    // Giá trị ngoài khoảng sẽ bị kẹp về [MIN_VIEWPORT_FRACTION, 1f]
    val viewportFraction: Float = 0.8f,
    
    // Trang hiển thị ban đầu khi khởi tạo (mặc định trang đầu tiên: 0)
    val initialPage: Int = 0,
    
    // Bật/Tắt chế độ cuộn vô hạn vòng tròn (0 -> 1 -> 2 -> 0 -> 1...)
    val enableInfiniteScroll: Boolean = true,
    
    // Đảo ngược hướng cuộn (true: vuốt ngược chiều tiêu chuẩn)
    val reverse: Boolean = false,
    
    // Bật/Tắt chế độ tự động cuộn trang (AutoPlay)
    val autoPlay: Boolean = false,
    
    // Khoảng thời gian dừng lại tại mỗi trang (mặc định 4000ms = 4 giây)
    val autoPlayInterval: Long = 4000L,
    
    // Thời gian diễn ra hiệu ứng chuyển trang (mặc định 800ms)
    val autoPlayAnimationDuration: Long = 800L,
    
    // Đường cong gia tốc (Easing curve) cho hiệu ứng chuyển trang
    val autoPlayCurve: Easing = FastOutSlowInEasing,
    
    // Bật/Tắt hiệu ứng phóng to trang nằm ở chính giữa
    val enlargeCenterPage: Boolean = false,
    
    // Tỉ lệ thu nhỏ của các trang nằm ở 2 bên (mặc định 0.3f = thu nhỏ 30%)
    val enlargeFactor: Float = 0.3f,
    
    // Chiến lược thực hiện phóng to (SCALE, HEIGHT, ZOOM)
    val enlargeStrategy: CenterPageEnlargeStrategy = CenterPageEnlargeStrategy.SCALE,
    
    // Callback lắng nghe sự kiện chuyển trang (trả về chỉ số trang thực và nguyên nhân đổi trang)
    val onPageChanged: ((index: Int, reason: CarouselPageChangedReason) -> Unit)? = null,
    
    // Hướng cuộn trang (HORIZONTAL: cuộn ngang, VERTICAL: cuộn dọc)
    val scrollDirection: CarouselScrollDirection = CarouselScrollDirection.HORIZONTAL,
    
    // Tạm dừng tự động cuộn khi người dùng chạm ngón tay vào màn hình
    val pauseAutoPlayOnTouch: Boolean = true,
    
    // Thêm khoảng đệm 2 đầu nếu không dùng cuộn vô hạn
    val padEnds: Boolean = true,
)

/** Giá trị nhỏ nhất cho viewportFraction — tránh trang có kích thước 0 làm Pager hỏng. */
private const val MIN_VIEWPORT_FRACTION = 0.1f

/** Số trang bản sao tối thiểu đệm ở MỖI đầu khi cuộn vô hạn — vùng an toàn trước khi chạm tường. */
private const val MIN_SIDE_BUFFER_PAGES = 10

/**
 * Bộ điều khiển `CarouselController` cho phép lập trình viên gọi lệnh chuyển trang bằng code.
 * Tương đương `CarouselController` / `CarouselSliderController` trong Flutter `carousel_slider: 5.1.2`.
 */
class CarouselController internal constructor(
    // CoroutineScope để thực thi các animation chuyển trang không bất đồng bộ
    private val coroutineScope: CoroutineScope,
) {
    // Lưu tham chiếu đến PagerState nội bộ của Compose
    internal var pagerState: PagerState? = null
    
    // Hàm lấy tổng số lượng phần tử thực tế
    internal var itemCountProvider: (() -> Int)? = null
    
    // Hàm kiểm tra có đang bật cuộn vô hạn không
    internal var infiniteScrollProvider: (() -> Boolean)? = null

    // Trang ảo đầu tiên của bản GỐC (0 khi không cuộn vô hạn)
    internal var firstOriginalPageProvider: (() -> Int)? = null
    
    // Lưu trữ nguyên nhân dẫn đến lần chuyển trang gần nhất
    internal var lastReason: CarouselPageChangedReason = CarouselPageChangedReason.MANUAL

    // Trạng thái bật/tắt AutoPlay có thể điều khiển bằng code
    var isAutoPlayPlaying: Boolean by mutableStateOf(true)
        internal set

    /**
     * Lấy chỉ số trang thực tế hiện tại (Luôn nằm trong khoảng 0 .. itemCount - 1).
     */
    val realCurrentPage: Int
        get() {
            val state = pagerState ?: return 0
            val count = itemCountProvider?.invoke() ?: return 0
            if (count <= 0) return 0
            // Công thức quy đổi trang ảo thành trang thực tế chuẩn xác
            return (state.currentPage % count + count) % count
        }

    /**
     * Đổi chỉ số trang THẬT (0 .. itemCount-1) sang chỉ số trang ẢO của Pager.
     * Khi cuộn vô hạn, danh sách ảo là [bản sao | bản gốc | bản sao] nên trang thật
     * luôn nằm ở bản GỐC giữa: virtual = firstOriginalPage + page.
     */
    private fun toVirtualPage(page: Int): Int {
        val count = itemCountProvider?.invoke() ?: return page
        if (count <= 0) return page
        val target = page.coerceIn(0, count - 1)
        return (firstOriginalPageProvider?.invoke() ?: 0) + target
    }

    /**
     * Chuyển đến trang chỉ định với hiệu ứng Animation mượt mà.
     * @param page Trang THẬT muốn chuyển tới (0 .. itemCount-1)
     * @param durationMs Thời gian chạy animation (mặc định 800ms)
     * @param easing Đường cong animation
     */
    fun animateToPage(
        page: Int,
        durationMs: Long = 800L,
        easing: Easing = FastOutSlowInEasing,
    ) {
        val state = pagerState ?: return

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            state.animateScrollToPage(
                page = toVirtualPage(page),
                animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
            )
        }
    }

    /**
     * Chuyển sang trang tiếp theo (Next Page).
     */
    fun nextPage(
        durationMs: Long = 800L,
        easing: Easing = FastOutSlowInEasing,
    ) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            if (isInfinite && count > 1) {
                // Tiến thêm 1 trang ảo (sang bản sao cuối nếu cần, sau đó tự nhảy ngầm về bản gốc)
                state.animateScrollToPage(
                    page = (state.currentPage + 1).coerceAtMost(state.pageCount - 1),
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            } else {
                // Cuộn vòng lặp thủ công khi không bật cuộn vô hạn
                val next = (state.currentPage + 1) % count
                state.animateScrollToPage(
                    page = next,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            }
        }
    }

    /**
     * Quay lại trang trước đó (Previous Page).
     */
    fun previousPage(
        durationMs: Long = 800L,
        easing: Easing = FastOutSlowInEasing,
    ) {
        val state = pagerState ?: return
        val count = itemCountProvider?.invoke() ?: return
        if (count <= 0) return

        val isInfinite = infiniteScrollProvider?.invoke() ?: false

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            if (isInfinite && count > 1) {
                // Lùi 1 trang ảo (sang bản sao đầu nếu cần, sau đó tự nhảy ngầm về bản gốc)
                state.animateScrollToPage(
                    page = (state.currentPage - 1).coerceAtLeast(0),
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            } else {
                // Lùi trang thủ công
                val prev = if (state.currentPage - 1 < 0) count - 1 else state.currentPage - 1
                state.animateScrollToPage(
                    page = prev,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
                )
            }
        }
    }

    /**
     * Nhảy trực tiếp tới trang THẬT chỉ định lập tức (Không có animation).
     */
    fun jumpToPage(page: Int) {
        val state = pagerState ?: return

        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            state.scrollToPage(toVirtualPage(page))
        }
    }

    /**
     * Bắt đầu/Tiếp tục tự động cuộn trang.
     */
    fun startAutoPlay() {
        isAutoPlayPlaying = true
    }

    /**
     * Tạm dừng tự động cuộn trang.
     */
    fun stopAutoPlay() {
        isAutoPlayPlaying = false
    }
}

/**
 * Composable khởi tạo và ghi nhớ `CarouselController`.
 */
@Composable
fun rememberCarouselController(): CarouselController {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) {
        CarouselController(coroutineScope)
    }
}

/**
 * CarouselSlider cho danh sách dữ liệu mẫu [items].
 * Tương đương với `CarouselSlider` / `CarouselSlider.builder` trong Flutter `carousel_slider: 5.1.2`.
 */
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
            // Kiểm tra chỉ số trang hợp lệ trước khi vẽ
            if (realIndex in items.indices) {
                itemContent(items[realIndex], index, realIndex)
            }
        },
    )
}

/**
 * Component CarouselSlider dạng Builder hỗ trợ dựng theo tổng số phần tử [itemCount].
 * Tương đương `CarouselSlider.builder` trong Flutter `carousel_slider: 5.1.2`.
 */
@Composable
fun CarouselSliderBuilder(
    itemCount: Int,
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(),
    controller: CarouselController = rememberCarouselController(),
    itemContent: @Composable (index: Int, realIndex: Int) -> Unit,
) {
    // Nếu danh sách rỗng thì không hiển thị gì
    if (itemCount <= 0) return

    // Kiểm tra có đủ điều kiện bật cuộn vô hạn không (Phải có > 1 phần tử)
    val isInfinite = options.enableInfiniteScroll && itemCount > 1
    
    // Cuộn vô hạn kiểu "đệm 2 đầu & dịch chuyển tức thời" (Fake Clones + scrollToPage):
    // danh sách ảo = [N bản sao | bản GỐC | N bản sao].
    //   VD [A, B, C], N = 4 → 4×3 trang sao + 3 trang gốc + 4×3 trang sao = 27 trang,
    //   bản gốc là index 12..14.
    // - Mỗi đầu đệm ít nhất MIN_SIDE_BUFFER_PAGES trang → vuốt liên tục nhiều lần (Pager chưa
    //   kịp dừng nên chưa nhảy ngầm) vẫn khó chạm tường; khi viewportFraction < 1 luôn có trang
    //   để hé lộ.
    // - Đệm đúng bội số itemCount → công thức `page % itemCount` vẫn ra trang thật ở mọi vị trí
    //   (SmoothPageIndicator, realIndex, enlargeCenterPage… không phải đổi).
    val copiesPerSide = if (isInfinite) {
        (MIN_SIDE_BUFFER_PAGES + itemCount - 1) / itemCount // làm tròn lên
    } else {
        0
    }
    val firstOriginalPage = copiesPerSide * itemCount
    val totalPages = itemCount * (copiesPerSide * 2 + 1)

    // Trang ảo ban đầu nằm trong bản GỐC (đoạn giữa)
    val initialVirtualPage = firstOriginalPage + options.initialPage.coerceIn(0, itemCount - 1)

    // Khởi tạo trạng thái PagerState của Jetpack Compose
    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage,
        pageCount = { totalPages },
    )

    // Liên kết thông tin PagerState vào controller để gọi lệnh ngoại vi
    controller.pagerState = pagerState
    controller.itemCountProvider = { itemCount }
    controller.infiniteScrollProvider = { isInfinite }
    controller.firstOriginalPageProvider = { firstOriginalPage }

    // Theo dõi xem người dùng có đang vuốt (kéo) Slider không
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Theo dõi ngón tay đang đặt trên Slider (kể cả chỉ chạm giữ, chưa kéo) — giống Flutter
    var isTouched by remember { mutableStateOf(false) }

    // Luôn đọc giá trị mới nhất bên trong các coroutine sống lâu
    val currentItemCount by rememberUpdatedState(itemCount)
    val currentOnPageChanged by rememberUpdatedState(options.onPageChanged)

    // Người dùng bắt đầu vuốt → mọi lần đổi trang tiếp theo (kể cả lúc fling trôi) là MANUAL
    LaunchedEffect(pagerState) {
        snapshotFlow { isDragged }.collect { dragging ->
            if (dragging) controller.lastReason = CarouselPageChangedReason.MANUAL
        }
    }

    // Trang có thuộc bản GỐC không (luôn đúng khi không cuộn vô hạn)
    fun isInOriginalSection(page: Int): Boolean =
        !isInfinite || page in firstOriginalPage until firstOriginalPage + itemCount

    // "Bẫy dịch chuyển tức thời": mỗi khi Pager ngừng cuộn mà đang ở vùng bản sao,
    // nhảy ngầm (không animation) về trang cùng nội dung trong bản GỐC.
    // Pager "ngừng cuộn" cả khi fling trôi xong lẫn khi người dùng chạm tay chặn fling
    // giữa chừng → vuốt nhanh liên tục vẫn được kéo về bản gốc trước mỗi lần vuốt mới.
    LaunchedEffect(pagerState, isInfinite, itemCount, firstOriginalPage) {
        if (!isInfinite) return@LaunchedEffect
        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
            .collect { (scrolling, page) ->
                if (scrolling || isInOriginalSection(page)) return@collect
                // Cùng nội dung (page % itemCount) nhưng nằm trong bản gốc
                val target = firstOriginalPage + page % itemCount
                // Giữ nguyên độ lệch trang: nếu Pager bị chặn giữa 2 trang (người dùng chạm
                // tay khi đang fling) thì vị trí trên màn hình không bị giật về mép trang.
                // Nếu người dùng bắt đầu kéo đúng lúc này, lệnh bị huỷ và sẽ chạy lại ở lần
                // ngừng cuộn tiếp theo.
                pagerState.scrollToPage(
                    page = target,
                    pageOffsetFraction = pagerState.currentPageOffsetFraction,
                )
            }
    }

    // Coroutine xử lý AutoPlay.
    // KHÔNG dùng currentPage làm key: currentPage đổi ngay giữa animation (khi trượt quá
    // nửa trang) → effect restart → animation bị huỷ giữa chừng, trang kẹt lệch / nhảy cóc.
    LaunchedEffect(
        pagerState,
        options.autoPlay,
        controller.isAutoPlayPlaying,
        options.autoPlayInterval,
        options.autoPlayAnimationDuration,
        options.autoPlayCurve,
        options.pauseAutoPlayOnTouch,
        isInfinite,
    ) {
        if (!options.autoPlay || !controller.isAutoPlayPlaying) return@LaunchedEffect

        // Được phép đếm giờ khi: pager đứng yên và (nếu cấu hình) người dùng không chạm
        fun canTick(): Boolean =
            !pagerState.isScrollInProgress &&
                !(options.pauseAutoPlayOnTouch && (isDragged || isTouched))

        while (true) {
            // 1. Chờ tới khi pager đứng yên, không bị chạm và đã nhảy ngầm về bản gốc
            snapshotFlow {
                canTick() && currentItemCount > 1 && isInOriginalSection(pagerState.settledPage)
            }.first { it }

            // 2. Đếm ngược; nếu bị vuốt / chạm / đổi trang trong lúc chờ thì đếm lại từ đầu
            val startPage = pagerState.settledPage
            val interrupted = withTimeoutOrNull(options.autoPlayInterval) {
                snapshotFlow { canTick() && pagerState.settledPage == startPage }.first { !it }
            }
            if (interrupted != null) continue

            // 3. Hết giờ → chuyển trang (giống carousel_slider: hết trang cuối thì về 0)
            val count = currentItemCount
            val target = if (isInfinite) {
                pagerState.currentPage + 1
            } else {
                (pagerState.currentPage + 1) % count
            }
            controller.lastReason = CarouselPageChangedReason.TIMED
            try {
                pagerState.animateScrollToPage(
                    page = target,
                    animationSpec = tween(
                        durationMillis = options.autoPlayAnimationDuration.toInt(),
                        easing = options.autoPlayCurve,
                    ),
                )
            } catch (e: CancellationException) {
                // Animation bị người dùng vuốt chen ngang → vẫn tiếp tục vòng lặp,
                // chỉ thoát nếu chính effect bị huỷ.
                currentCoroutineContext().ensureActive()
            }
        }
    }

    // Lắng nghe sự kiện thay đổi trang để kích hoạt callback onPageChanged.
    // Theo dõi trang THẬT (không phải trang ảo): lần nhảy ngầm bản sao → bản gốc giữ nguyên
    // trang thật nên snapshotFlow không phát lại → callback không bị gọi 2 lần.
    // drop(1): bỏ giá trị ban đầu — Flutter không gọi onPageChanged khi vừa hiển thị.
    LaunchedEffect(pagerState) {
        snapshotFlow {
            val count = currentItemCount
            (pagerState.currentPage % count + count) % count
        }.drop(1).collectLatest { realIndex ->

            // Xác định nguyên nhân chuyển trang (Nếu tay đang vuốt -> MANUAL, ngược lại lấy từ lastReason)
            val reason = if (isDragged) {
                CarouselPageChangedReason.MANUAL
            } else {
                controller.lastReason
            }

            // Phát callback ra ngoài cho lập trình viên.
            // Không reset lastReason ở đây: một lần cuộn có thể đi qua nhiều trang
            // (VD: trang cuối → trang 0), tất cả phải cùng một reason. lastReason chỉ đổi
            // khi có nguồn cuộn mới (AutoPlay, Controller, hoặc người dùng bắt đầu vuốt).
            currentOnPageChanged?.invoke(realIndex, reason)
        }
    }

    // Thiết lập Modifier kích thước (Chiều cao cố định hoặc Tỉ lệ khung hình AspectRatio)
    // + theo dõi ngón tay chạm vào Slider (pass Initial: chỉ "nghe", không chặn cử chỉ của Pager)
    val containerModifier = modifier
        .then(
            when {
                options.height != null -> Modifier.height(options.height)
                options.aspectRatio != null -> Modifier.aspectRatio(options.aspectRatio)
                // Không đặt kích thước: dùng đúng kích thước modifier của nơi gọi (VD: weight(1f))
                else -> Modifier
            },
        )
        // Carousel tràn sát mép màn hình → vuốt từ mép bị cử chỉ Back của hệ thống (gesture
        // navigation) cướp mất và đóng Activity. Loại trừ vùng carousel khỏi cử chỉ Back.
        // (Android 10+; hệ thống chỉ chấp nhận tối đa 200dp chiều cao mỗi mép.)
        .systemGestureExclusion()
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                isTouched = true
                try {
                    // Chờ đến khi nhấc hết ngón tay
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                    } while (event.changes.any { it.pressed })
                } finally {
                    isTouched = false
                }
            }
        }

    // Sử dụng BoxWithConstraints để đo chính xác chiều rộng/chiều cao cha nhằm tính toán viewportFraction
    BoxWithConstraints(
        modifier = containerModifier,
    ) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight

        // Kẹp viewportFraction về (0, 1]: 1f = tràn viền, nhỏ hơn = hé lộ trang bên cạnh
        val fraction = options.viewportFraction.coerceIn(MIN_VIEWPORT_FRACTION, 1f)

        // Phần khung còn thừa chia đều cho 2 phía (bằng 0 khi tràn viền)
        val sideRatio = (1f - fraction) / 2f
        val isHorizontal = options.scrollDirection == CarouselScrollDirection.HORIZONTAL

        // Padding trái/phải khi cuộn ngang
        val horizontalPadding = if (isHorizontal) parentWidth * sideRatio else 0.dp

        // Padding trên/dưới khi cuộn dọc
        val verticalPadding = if (!isHorizontal) parentHeight * sideRatio else 0.dp

        // Tổng hợp PaddingValues cho Pager
        val contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding,
        )

        val pageModifier = Modifier.fillMaxSize()

        // Phân nhánh theo hướng cuộn: NGAN (HorizontalPager) hoặc DỌC (VerticalPager)
        if (options.scrollDirection == CarouselScrollDirection.HORIZONTAL) {
            HorizontalPager(
                state = pagerState,
                modifier = pageModifier,
                contentPadding = contentPadding,
                reverseLayout = options.reverse,
            ) { page ->
                // Tính toán chỉ số trang thực tế của từng phần tử
                val realIndex = (page % itemCount + itemCount) % itemCount
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // TalkBack đọc theo trang THẬT thay vì chỉ số trang ảo (có cả bản sao)
                        .semantics { contentDescription = "Trang ${realIndex + 1} trên $itemCount" }
                        .then(
                            // Nếu bật hiệu ứng phóng to trang trung tâm (enlargeCenterPage)
                            if (options.enlargeCenterPage) {
                                Modifier.graphicsLayer {
                                    // Tính khoảng cách lệch offset của trang so với trang đang ở trung tâm
                                    val pageOffset = (
                                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                            ).let { abs(it) }

                                    // Tính tỉ lệ co giãn (Thu nhỏ dần các trang xa trung tâm)
                                    val scaleFraction =
                                        1f - (pageOffset * options.enlargeFactor).coerceIn(0f, 1f)

                                    // Áp dụng biến đổi theo chiến lược enlargeStrategy
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
                                            // Kết hợp mờ dần khi xa trung tâm
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1.0f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                                            )
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Hiển thị giao diện của item trang
                    itemContent(page, realIndex)
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = pageModifier,
                contentPadding = contentPadding,
                reverseLayout = options.reverse,
            ) { page ->
                val realIndex = (page % itemCount + itemCount) % itemCount
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // TalkBack đọc theo trang THẬT thay vì chỉ số trang ảo (có cả bản sao)
                        .semantics { contentDescription = "Trang ${realIndex + 1} trên $itemCount" }
                        .then(
                            if (options.enlargeCenterPage) {
                                Modifier.graphicsLayer {
                                    val pageOffset = (
                                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                            ).let { abs(it) }

                                    val scaleFraction =
                                        1f - (pageOffset * options.enlargeFactor).coerceIn(0f, 1f)

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
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1.0f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                                            )
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    itemContent(page, realIndex)
                }
            }
        }
    }
}

/**
 * Component Carousel cấp cao tích hợp sẵn CarouselSlider + SmoothPageIndicator + Text Animation trượt phía dưới.
 *
 * @param imageFillsRemainingSpace
 *  - false (mặc định): ảnh có kích thước theo `options.height` / `options.aspectRatio`.
 *  - true: indicator và phần chữ giữ kích thước bạn đặt, ảnh (carousel) chiếm TOÀN BỘ phần còn lại.
 *    Khi đó `options.height` / `options.aspectRatio` bị bỏ qua, và [modifier] phải cho
 *    CarouselSliderWithText một chiều cao giới hạn (VD: `fillMaxSize()` hoặc `weight(1f)`).
 * @param bottomContentHeight Chiều cao cố định của phần chữ phía dưới. null = cao theo nội dung.
 *  Nên đặt khi [imageFillsRemainingSpace] = true, nếu không ảnh sẽ co giãn theo số dòng chữ
 *  mỗi khi đổi trang.
 */
@Composable
fun <T> CarouselSliderWithText(
    items: List<T>,
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(
        autoPlay = true,
        autoPlayInterval = 3500L,
        viewportFraction = 0.85f,
        enlargeCenterPage = true,
    ),
    controller: CarouselController = rememberCarouselController(),
    indicatorEffect: IndicatorEffect = IndicatorEffect.WORM,
    dotWidth: Dp = 8.dp,
    dotHeight: Dp = 8.dp,
    expandedDotWidth: Dp = 24.dp,
    spacing: Dp = 8.dp,
    activeColor: Color = EdupiaColors.Primary,
    inactiveColor: Color = EdupiaColors.Neutral300,
    indicatorTopSpacing: Dp = 24.dp,
    textTopSpacing: Dp = 24.dp,
    imageFillsRemainingSpace: Boolean = false,
    bottomContentHeight: Dp? = null,
    itemContent: @Composable (item: T, index: Int, realIndex: Int) -> Unit,
    bottomTextContent: @Composable (item: T, realIndex: Int) -> Unit,
) {
    // Không hiển thị nếu danh sách rỗng
    if (items.isEmpty()) return

    // Lưu trữ chỉ số trang thực tế hiện tại để đồng bộ phần chữ bên dưới
    var currentRealIndex by remember { mutableIntStateOf(options.initialPage.coerceIn(0, items.size - 1)) }

    // Kết hợp callback onPageChanged để cập nhật currentRealIndex
    val combinedOptions = options.copy(
        onPageChanged = { index, reason ->
            currentRealIndex = index
            options.onPageChanged?.invoke(index, reason)
        },
    ).let {
        // Ảnh chiếm phần còn lại → bỏ kích thước cố định, để weight(1f) quyết định
        if (imageFillsRemainingSpace) it.copy(height = null, aspectRatio = null) else it
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Khối Banner Carousel chính phía trên
        CarouselSlider(
            items = items,
            modifier = if (imageFillsRemainingSpace) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            } else {
                Modifier
            },
            options = combinedOptions,
            controller = controller,
            itemContent = itemContent,
        )

        Spacer(modifier = Modifier.height(indicatorTopSpacing))

        // 2. Chấm chỉ báo trang SmoothPageIndicator
        controller.pagerState?.let { state ->
            SmoothPageIndicator(
                pagerState = state,
                pageCount = items.size,
                effect = indicatorEffect,
                dotWidth = dotWidth,
                dotHeight = dotHeight,
                expandedDotWidth = expandedDotWidth,
                spacing = spacing,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
            )
        }

        Spacer(modifier = Modifier.height(textTopSpacing))

        // 3. Khối chữ phía dưới tự động đổi mượt mà theo trang bằng AnimatedContent
        //    (bọc trong Box để cố định chiều cao khi có bottomContentHeight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (bottomContentHeight != null) Modifier.height(bottomContentHeight) else Modifier),
            contentAlignment = Alignment.TopCenter,
        ) {
            AnimatedContent(
                targetState = currentRealIndex,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = 250
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 250)
                    )
                },
                label = "carousel_text_animation",
            ) { targetIndex ->

                val safeIndex = targetIndex.coerceIn(0, items.size - 1)
                bottomTextContent(items[safeIndex], safeIndex)
            }
        }
    }
}

@Preview(showBackground = true, name = "CarouselSlider Preview")
@Composable
fun CarouselSliderPreview() {
    val sampleList = listOf(
        SampleOnboardingItem("Học Tiếng Anh Với AI", "Luyện phản xạ 1:1 cùng AI Edupia.", R.drawable.image_login),
        SampleOnboardingItem("Lộ Trình Chuẩn Quốc Tế", "Chương trình học chuẩn khung CEFR.", R.drawable.logo_splash),
        SampleOnboardingItem("Học Mà Chơi", "Hệ thống bài học tương tác sinh động.", R.drawable.bg_onboarding),
    )

    EdupiaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EdupiaColors.BackgroundWhite)

        ) {
            CarouselSliderWithText(
                items = sampleList,
                indicatorEffect = IndicatorEffect.EXPANDING,
                dotWidth = 16.dp,
                dotHeight = 8.dp,
                options = CarouselOptions(
                    aspectRatio = 16f/16f,
                    viewportFraction = 1f,
                    enlargeCenterPage = true,
                    autoPlay = true,
                    enableInfiniteScroll = true,
                ),
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
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = item.title,
                            style = EdupiaTypography.headlineMedium,
                            color = EdupiaColors.Neutral900,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
