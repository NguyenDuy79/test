package com.example.fitness.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Biểu đồ chứng khoán (Candlestick + Volume + MA7/MA20 + Crosshair)
 * được thiết kế để minh họa toàn diện Vòng đời của một Android Custom View:
 *
 * 1. Constructor / init        -> Khởi tạo Paint, Path, GestureDetector, Scroller, dữ liệu mẫu.
 * 2. onAttachedToWindow()      -> Bắt đầu ValueAnimator (hiệu ứng mở màn & nhịp giá live).
 * 3. onMeasure()               -> Tính toán kích thước View theo MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED).
 * 4. onSizeChanged()           -> Chia vùng khung vẽ (Header, Giá, Volume, Trục thời gian) & tạo Shader/Gradient.
 * 5. onLayout()                -> Chốt giới hạn cuộn (scroll bounds) sau khi có kích thước thực tế.
 * 6. onDraw()                  -> Vẽ biểu đồ lên Canvas (TUYỆT ĐỐI không cấp phát object mới tại đây).
 * 7. onTouchEvent()            -> Xử lý Pan (kéo), Pinch-to-Zoom (phóng to/thu nhỏ), Crosshair (tra cứu giá).
 * 8. computeScroll()           -> Phối hợp với OverScroller để cuộn theo quán tính (Fling) mượt 60/120fps.
 * 9. onVisibilityChanged()     -> Tạm dừng/tiếp tục Animator khi View ẩn/hiện để tiết kiệm pin & CPU.
 * 10. onWindowFocusChanged()   -> Tạm dừng hiệu ứng khi cửa sổ mất focus (ví dụ mở dialog/đa nhiệm).
 * 11. onSaveInstanceState()    -> Lưu trạng thái zoom, vị trí cuộn, nến đang chọn khi xoay màn hình.
 * 12. onRestoreInstanceState() -> Khôi phục trạng thái đã lưu.
 * 13. onDetachedFromWindow()   -> Hủy toàn bộ Animator, dừng Scroller để tránh Memory Leak.
 */
// =========================================================================
// [CÂU #004]: Viết constructor cho Custom View & Bẫy của @JvmOverloads
// [CÂU #005]: Đọc AttributeSet và Thứ tự ưu tiên Attribute
// [CÂU #008]: Hỗ trợ Accessibility (TalkBack, contentDescription, 48dp)
// =========================================================================
/**
 * Biểu đồ chứng khoán (Candlestick + Volume + MA7/MA20 + Crosshair)
 * được thiết kế để minh họa toàn diện và giải thích trực tiếp 10 CÂU HỎI ĐẦU TIÊN (#001 - #010):
 *
 * • [CÂU #001]: Các callback vòng đời chính (attached, measure, sizeChanged, layout, draw, detached).
 * • [CÂU #002]: MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED) & resolveSize trong onMeasure().
 * • [CÂU #003]: Khác biệt invalidate() (chỉ vẽ lại onDraw) và requestLayout() (đo đạc lại layout).
 * • [CÂU #004]: 4 Constructors & bẫy @JvmOverloads (tự điền defStyleAttr = 0).
 * • [CÂU #005]: Thứ tự ưu tiên attribute: XML direct > style="" > defStyleAttr > defStyleRes > theme.
 * • [CÂU #006]: Tối ưu onDraw: Tuyệt đối không tạo Paint/Path mới, Viewport Culling, clipRect.
 * • [CÂU #007]: Luồng Touch Event, ACTION_CANCEL & requestDisallowInterceptTouchEvent().
 * • [CÂU #008]: Accessibility & TalkBack: contentDescription, sendAccessibilityEvent.
 * • [CÂU #009]: Tự lưu State khi xoay màn hình (onSaveInstanceState, onRestoreInstanceState, BaseSavedState).
 * • [CÂU #010]: Chuyển đổi dp, sp trên Android 14+ với TypedValue.applyDimension.
 */
class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    // [CÂU #004 LƯU Ý]: @JvmOverloads sinh ra 3 constructor (1, 2, 3 tham số).
    // Bẫy: Nếu kế thừa widget hệ thống như Button/EditText, @JvmOverloads truyền defStyleAttr = 0
    // sẽ làm MẤT style mặc định của hệ thống! Với View thường thì dùng an toàn.
) : View(context, attrs, defStyleAttr) {

    data class Candle(
        val index: Int,
        val timeLabel: String,
        val open: Float,
        val high: Float,
        val low: Float,
        val close: Float,
        val volume: Float
    ) {
        val isBullish: Boolean get() = close >= open
    }

    enum class ChartMode {
        CANDLESTICK,
        LINE_AREA
    }

    // Callback giúp màn hình bên ngoài theo dõi thứ tự gọi các hàm vòng đời
    var onLifecycleEvent: ((event: String) -> Unit)? = null

    // Đếm số lần onDraw để quan sát sự khác biệt giữa invalidate() và requestLayout()
    var drawCount: Int = 0
        private set

    // --- Dữ liệu biểu đồ ---
    private val candles = mutableListOf<Candle>()
    private val ma7Values = mutableListOf<Float?>()
    private val ma20Values = mutableListOf<Float?>()

    var chartMode: ChartMode = ChartMode.CANDLESTICK
        set(value) {
            if (field != value) {
                field = value
                logLifecycle("invalidate() -> Đổi chế độ sang $value (chỉ gọi onDraw)")
                invalidate()
            }
        }

    // --- Vùng bố cục (được tính trong onSizeChanged, dùng lại trong onDraw) ---
    private val headerRect = RectF()
    private val priceChartRect = RectF()
    private val volumeChartRect = RectF()
    private val timeAxisRect = RectF()
    private val tempRect = RectF()

    // --- Trạng thái tương tác (Zoom, Scroll, Crosshair) ---
    private var candleWidthPx = dpToPx(14f)
    private val minCandleWidthPx = dpToPx(6f)
    private val maxCandleWidthPx = dpToPx(38f)
    private var candleSpacingPx = dpToPx(4f)

    private var scrollOffsetPx = 0f
    private var maxScrollOffsetPx = 0f
    private var selectedCandleIndex: Int = -1
    private var isCrosshairActive: Boolean = false

    // --- Tiến trình Animation ---
    private var introProgress = 1f
    private var livePulseFraction = 0f

    // --- Các đối tượng Paint & Path được cấp phát SẴN trong init (Tránh GC giật lag khi onDraw) ---
    private val bgPaint = Paint().apply {
        color = Color.parseColor("#0F172A") // Nền tối kiểu sàn giao dịch
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        strokeWidth = dpToPx(1f)
        style = Paint.Style.STROKE
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        textSize = spToPx(10f)
    }

    private val titleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F8FAFC")
        textSize = spToPx(16f)
        isFakeBoldText = true
    }

    private val subtitleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBD5E1")
        textSize = spToPx(11.5f)
    }

    private val bullPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981") // Xanh tăng giá
        style = Paint.Style.FILL
        strokeWidth = dpToPx(1.5f)
    }

    private val bearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444") // Đỏ giảm giá
        style = Paint.Style.FILL
        strokeWidth = dpToPx(1.5f)
    }

    private val ma7Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B") // Vàng cho MA7
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.6f)
    }

    private val ma20Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8") // Xanh dương cho MA20
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.6f)
    }

    private val linePricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22D3EE")
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2.2f)
    }

    private val lineAreaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val livePriceLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1f)
        pathEffect = DashPathEffect(floatArrayOf(dpToPx(6f), dpToPx(4f)), 0f)
    }

    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E2E8F0")
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1f)
        pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(4f)), 0f)
    }

    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        style = Paint.Style.FILL
    }

    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = spToPx(10.5f)
        isFakeBoldText = true
    }

    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.FILL
    }

    // Các Path dùng lại trong mỗi frame
    private val ma7Path = Path()
    private val ma20Path = Path()
    private val linePath = Path()
    private val areaPath = Path()

    // --- Bộ xử lý cử chỉ & vật lý cuộn ---
    private val scroller = OverScroller(context)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // --- Các bộ Animation quản lý theo vòng đời ---
    private var introAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null

    // =========================================================================
    // 1. GIAI ĐOẠN KHỞI TẠO (CONSTRUCTOR / INIT)
    // • [CÂU #008]: Accessibility - Cung cấp contentDescription & focus cho TalkBack
    // • [CÂU #009]: Điều kiện để onSaveInstanceState hoạt động (id != NO_ID & isSaveEnabled = true)
    // =========================================================================
    init {
        // [CÂU #009]: 2 điều kiện bắt buộc để Android tự lưu state khi xoay màn hình:
        // 1. View phải có ID cụ thể (không phải NO_ID).
        // 2. isSaveEnabled phải là true.
        if (id == NO_ID) {
            id = generateViewId()
        }
        isSaveEnabled = true

        // [CÂU #008]: Tránh cảnh báo ClickableViewAccessibility của Lint & hỗ trợ người khiếm thị
        isFocusable = true
        contentDescription = "Biểu đồ chứng khoán FPT. Vuốt 1 ngón để cuộn, 2 ngón để phóng to/thu nhỏ, chạm để tra cứu giá."

        generateInitialStockData(count = 80)

        scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val oldStep = candleStepPx()
                    val focusX = detector.focusX - priceChartRect.left
                    val centerCandleFloat = (scrollOffsetPx + focusX) / oldStep

                    candleWidthPx = (candleWidthPx * detector.scaleFactor)
                        .coerceIn(minCandleWidthPx, maxCandleWidthPx)
                    candleSpacingPx = (candleWidthPx * 0.28f).coerceAtLeast(dpToPx(2f))

                    updateScrollBounds()
                    // Giữ nguyên cây nến tại tâm điểm zoom của 2 ngón tay
                    val newStep = candleStepPx()
                    scrollOffsetPx = (centerCandleFloat * newStep - focusX)
                        .coerceIn(0f, maxScrollOffsetPx)

                    invalidate()
                    return true
                }
            }
        )

        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    if (!scroller.isFinished) {
                        scroller.abortAnimation()
                    }
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (priceChartRect.contains(e.x, e.y) || volumeChartRect.contains(e.x, e.y)) {
                        val tappedIndex = xToCandleIndex(e.x)
                        if (isCrosshairActive && selectedCandleIndex == tappedIndex) {
                            isCrosshairActive = false
                            selectedCandleIndex = -1
                        } else {
                            isCrosshairActive = true
                            selectedCandleIndex = tappedIndex
                        }
                        invalidate()
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    if (priceChartRect.contains(e.x, e.y) || volumeChartRect.contains(e.x, e.y)) {
                        isCrosshairActive = true
                        selectedCandleIndex = xToCandleIndex(e.x)
                        parent?.requestDisallowInterceptTouchEvent(true)
                        invalidate()
                    }
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (scaleDetector.isInProgress) return false

                    scrollOffsetPx = (scrollOffsetPx + distanceX)
                        .coerceIn(0f, maxScrollOffsetPx)
                    if (isCrosshairActive) {
                        selectedCandleIndex = xToCandleIndex(e2.x)
                    }
                    invalidate()
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (scaleDetector.isInProgress) return false
                    scroller.fling(
                        scrollOffsetPx.toInt(),
                        0,
                        (-velocityX).toInt(),
                        0,
                        0,
                        maxScrollOffsetPx.toInt(),
                        0,
                        0
                    )
                    // Kích hoạt computeScroll() ở frame tiếp theo
                    postInvalidateOnAnimation()
                    return true
                }
            }
        )

        logLifecycle("1. init: Khởi tạo Paint, Path, GestureDetector & 80 cây nến mẫu")
    }

    // =========================================================================
    // 2. GẮN VÀO WINDOW (onAttachedToWindow)
    // • [CÂU #001]: Callback vòng đời - Nơi chuẩn nhất để bắt đầu Animator / Listeners
    // =========================================================================
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logLifecycle("2. onAttachedToWindow(): Gắn vào Window -> Bắt đầu Animator")
        if (!isInEditMode) {
            startIntroAnimation()
            startPulseAnimation()
        }
    }

    // =========================================================================
    // 3. ĐO ĐẠC KÍCH THƯỚC (onMeasure)
    // • [CÂU #002]: MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED) & resolveSize
    // • BẪY: Luôn phải cộng padding (paddingLeft, paddingRight, paddingTop, paddingBottom)
    // =========================================================================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // [CÂU #002]: Bẫy hay gặp: Quên cộng padding vào kích thước mong muốn (desired)
        val desiredWidth = dpToPx(360f).toInt() + paddingLeft + paddingRight
        val desiredHeight = dpToPx(340f).toInt() + paddingTop + paddingBottom

        // [CÂU #002]: resolveSize tự động phân giải:
        // - EXACTLY: lấy đúng size cha ấn định
        // - AT_MOST: minOf(desired, size)
        // - UNSPECIFIED: desired (như khi nằm trong ScrollView theo chiều cuộn)
        val measuredW = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredH = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(measuredW, measuredH)

        val wMode = measureModeName(MeasureSpec.getMode(widthMeasureSpec))
        val hMode = measureModeName(MeasureSpec.getMode(heightMeasureSpec))
        logLifecycle("3. onMeasure(): w=${measuredW}px ($wMode), h=${measuredH}px ($hMode)")
    }

    // =========================================================================
    // 4. THAY ĐỔI KÍCH THƯỚC (onSizeChanged)
    // • [CÂU #001 & #006]: Tính toán vùng vẽ & tạo Shader tại đây, KHÔNG tạo trong onDraw
    // =========================================================================
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val contentLeft = paddingLeft + dpToPx(12f)
        // Chừa lề phải cho cột giá Y-Axis
        val yAxisWidth = dpToPx(56f)
        val contentRight = (w - paddingRight - yAxisWidth).coerceAtLeast(contentLeft + dpToPx(60f))
        val contentTop = paddingTop + dpToPx(8f)
        val contentBottom = (h - paddingBottom - dpToPx(8f)).coerceAtLeast(contentTop + dpToPx(80f))

        val totalHeight = contentBottom - contentTop
        val headerHeight = dpToPx(58f).coerceAtMost(totalHeight * 0.24f)
        val timeAxisHeight = dpToPx(24f).coerceAtMost(totalHeight * 0.12f)
        val chartsAvailableHeight = (totalHeight - headerHeight - timeAxisHeight).coerceAtLeast(dpToPx(40f))

        // 74% diện tích cho biểu đồ nến giá, 26% cho biểu đồ cột khối lượng (Volume)
        val priceHeight = chartsAvailableHeight * 0.74f
        val gapBetweenCharts = dpToPx(8f)

        headerRect.set(contentLeft, contentTop, w - paddingRight - dpToPx(12f), contentTop + headerHeight)
        priceChartRect.set(
            contentLeft,
            headerRect.bottom + dpToPx(4f),
            contentRight,
            headerRect.bottom + priceHeight
        )
        volumeChartRect.set(
            contentLeft,
            priceChartRect.bottom + gapBetweenCharts,
            contentRight,
            contentBottom - timeAxisHeight
        )
        timeAxisRect.set(
            contentLeft,
            volumeChartRect.bottom,
            contentRight,
            contentBottom
        )

        // Khởi tạo LinearGradient theo chiều cao thực tế của vùng giá
        lineAreaPaint.shader = LinearGradient(
            0f,
            priceChartRect.top,
            0f,
            priceChartRect.bottom,
            Color.argb(110, 34, 211, 238),
            Color.argb(0, 34, 211, 238),
            Shader.TileMode.CLAMP
        )

        val wasAtEnd = maxScrollOffsetPx == 0f || scrollOffsetPx >= maxScrollOffsetPx - dpToPx(10f)
        updateScrollBounds()
        if (oldw == 0 || wasAtEnd) {
            // Mặc định cuộn tới cây nến mới nhất ở bên phải
            scrollOffsetPx = maxScrollOffsetPx
        }

        logLifecycle("4. onSizeChanged(): ${oldw}x${oldh} -> ${w}x${h} (Tạo lại RectF & Shader)")
    }

    // =========================================================================
    // 5. BỐ CỤC VỊ TRÍ (onLayout)
    // =========================================================================
    /**
     * Với View đơn (không phải ViewGroup), onLayout ít việc hơn nhưng vẫn nằm trong
     * chuỗi bắt buộc: requestLayout() -> onMeasure() -> onSizeChanged() -> onLayout().
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateScrollBounds()
        if (changed) {
            logLifecycle("5. onLayout(): changed=true, bounds=[$left,$top - $right,$bottom]")
        }
    }

    // =========================================================================
    // 6. VẼ LÊN MÀN HÌNH (onDraw)
    // • [CÂU #006]: TỐI ƯU ONDRAW:
    //   1. TUYỆT ĐỐI KHÔNG TẠO NEW OBJECT (Paint, Path, RectF) TRONG ONDRAW để tránh GC giật lag.
    //   2. Áp dụng Viewport Culling: Chỉ tính toán & vẽ những cây nến nằm trong màn hình.
    //   3. Dùng canvas.clipRect() để giới hạn vùng vẽ, tránh overdraw tràn sang trục Y.
    // =========================================================================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCount++

        // 1. Vẽ nền tối của sàn chứng khoán
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        if (candles.isEmpty() || priceChartRect.width() <= 0f) return

        // [CÂU #006]: VIEWPORT CULLING (Chỉ vẽ nến trong tầm nhìn)
        // Dù danh sách có 100.000 cây nến thì mỗi frame chỉ vẽ ~20-30 cây trên màn hình -> 120fps mượt mà!
        val step = candleStepPx()
        val firstVisibleIndex = max(0, (scrollOffsetPx / step).toInt() - 1)
        val visibleCount = (priceChartRect.width() / step).toInt() + 3
        val lastVisibleIndex = min(candles.lastIndex, firstVisibleIndex + visibleCount)

        // Tìm đỉnh cao nhất (Max High) & đáy thấp nhất (Min Low) trong vùng nhìn thấy để tự động co giãn trục Y
        var minPrice = Float.MAX_VALUE
        var maxPrice = -Float.MAX_VALUE
        var maxVolume = 1f

        for (i in firstVisibleIndex..lastVisibleIndex) {
            val c = candles[i]
            if (c.low < minPrice) minPrice = c.low
            if (c.high > maxPrice) maxPrice = c.high
            if (c.volume > maxVolume) maxVolume = c.volume
            ma7Values.getOrNull(i)?.let {
                if (it < minPrice) minPrice = it
                if (it > maxPrice) maxPrice = it
            }
            ma20Values.getOrNull(i)?.let {
                if (it < minPrice) minPrice = it
                if (it > maxPrice) maxPrice = it
            }
        }

        if (minPrice >= maxPrice) {
            minPrice -= 1f
            maxPrice += 1f
        }
        val pricePadding = (maxPrice - minPrice) * 0.08f
        minPrice -= pricePadding
        maxPrice += pricePadding

        // 2. Vẽ lưới (Grid) & Nhãn giá bên trục Y
        drawGridAndAxes(canvas, minPrice, maxPrice, firstVisibleIndex, lastVisibleIndex)

        // 3. Cắt vùng vẽ (clipRect) cho biểu đồ chính để nến không tràn ra ngoài trục Y khi cuộn
        canvas.save()
        canvas.clipRect(
            priceChartRect.left,
            priceChartRect.top,
            priceChartRect.right,
            volumeChartRect.bottom
        )

        if (chartMode == ChartMode.CANDLESTICK) {
            drawCandlesticksAndVolume(
                canvas,
                firstVisibleIndex,
                lastVisibleIndex,
                minPrice,
                maxPrice,
                maxVolume
            )
            drawMovingAverages(canvas, firstVisibleIndex, lastVisibleIndex, minPrice, maxPrice)
        } else {
            drawLineAreaAndVolume(
                canvas,
                firstVisibleIndex,
                lastVisibleIndex,
                minPrice,
                maxPrice,
                maxVolume
            )
        }

        // Vẽ đường giá hiện tại (Live Price) + Hiệu ứng nhịp đập (Pulse) ở cây nến cuối
        drawLivePriceIndicator(canvas, minPrice, maxPrice)

        canvas.restore()

        // 4. Vẽ tia ngắm tra cứu giá (Crosshair) nếu người dùng đang chạm/nhấn giữ
        if (isCrosshairActive && selectedCandleIndex in candles.indices) {
            drawCrosshair(canvas, selectedCandleIndex, minPrice, maxPrice)
        }

        // 5. Vẽ Header thông tin mã cổ phiếu và chi tiết OHLCV
        drawHeaderInfo(canvas)
    }

    private fun drawGridAndAxes(
        canvas: Canvas,
        minPrice: Float,
        maxPrice: Float,
        firstIndex: Int,
        lastIndex: Int
    ) {
        val horizontalLines = 4
        for (i in 0..horizontalLines) {
            val fraction = i.toFloat() / horizontalLines
            val y = priceChartRect.top + fraction * priceChartRect.height()
            canvas.drawLine(priceChartRect.left, y, priceChartRect.right, y, gridPaint)

            val priceAtLine = maxPrice - fraction * (maxPrice - minPrice)
            val label = formatPrice(priceAtLine)
            canvas.drawText(
                label,
                priceChartRect.right + dpToPx(6f),
                y + dpToPx(3.5f),
                axisTextPaint
            )
        }

        // Khung ngăn cách vùng Volume
        canvas.drawLine(
            volumeChartRect.left,
            volumeChartRect.top,
            volumeChartRect.right,
            volumeChartRect.top,
            gridPaint
        )
        canvas.drawLine(
            volumeChartRect.left,
            volumeChartRect.bottom,
            volumeChartRect.right,
            volumeChartRect.bottom,
            gridPaint
        )
        canvas.drawText(
            "VOL",
            volumeChartRect.right + dpToPx(6f),
            volumeChartRect.top + dpToPx(12f),
            axisTextPaint
        )

        // Lưới dọc & Nhãn thời gian trục X
        val labelInterval = max(1, (dpToPx(65f) / candleStepPx()).toInt())
        for (i in firstIndex..lastIndex) {
            if (i % labelInterval == 0) {
                val cx = candleIndexToCenterX(i)
                if (cx in priceChartRect.left..priceChartRect.right) {
                    canvas.drawLine(cx, priceChartRect.top, cx, volumeChartRect.bottom, gridPaint)
                    val timeText = candles[i].timeLabel
                    val tw = axisTextPaint.measureText(timeText)
                    canvas.drawText(
                        timeText,
                        cx - tw / 2f,
                        timeAxisRect.top + dpToPx(16f),
                        axisTextPaint
                    )
                }
            }
        }
    }

    private fun drawCandlesticksAndVolume(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float,
        maxVolume: Float
    ) {
        val halfBody = candleWidthPx / 2f
        val baselinePrice = (minPrice + maxPrice) / 2f

        for (i in firstIndex..lastIndex) {
            val c = candles[i]
            val cx = candleIndexToCenterX(i)
            val paint = if (c.isBullish) bullPaint else bearPaint

            // Áp dụng introProgress để các cây nến mọc mượt mà khi mở màn hình
            val animatedHigh = baselinePrice + (c.high - baselinePrice) * introProgress
            val animatedLow = baselinePrice + (c.low - baselinePrice) * introProgress
            val animatedOpen = baselinePrice + (c.open - baselinePrice) * introProgress
            val animatedClose = baselinePrice + (c.close - baselinePrice) * introProgress

            val highY = priceToY(animatedHigh, minPrice, maxPrice)
            val lowY = priceToY(animatedLow, minPrice, maxPrice)
            val openY = priceToY(animatedOpen, minPrice, maxPrice)
            val closeY = priceToY(animatedClose, minPrice, maxPrice)

            // 1. Vẽ râu nến (High - Low Wick)
            canvas.drawLine(cx, highY, cx, lowY, paint)

            // 2. Vẽ thân nến (Open - Close Body)
            val topBody = min(openY, closeY)
            val bottomBody = max(openY, closeY).coerceAtLeast(topBody + dpToPx(1.5f))
            canvas.drawRect(cx - halfBody, topBody, cx + halfBody, bottomBody, paint)

            // 3. Vẽ cột Volume bên dưới
            val volHeight = (c.volume / maxVolume) * volumeChartRect.height() * introProgress
            val volTop = volumeChartRect.bottom - volHeight
            paint.alpha = 140
            canvas.drawRect(cx - halfBody, volTop, cx + halfBody, volumeChartRect.bottom, paint)
            paint.alpha = 255
        }
    }

    private fun drawMovingAverages(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float
    ) {
        ma7Path.reset()
        ma20Path.reset()
        var ma7Started = false
        var ma20Started = false

        for (i in firstIndex..lastIndex) {
            val cx = candleIndexToCenterX(i)
            ma7Values.getOrNull(i)?.let { ma ->
                val y = priceToY(ma, minPrice, maxPrice)
                if (!ma7Started) {
                    ma7Path.moveTo(cx, y)
                    ma7Started = true
                } else {
                    ma7Path.lineTo(cx, y)
                }
            }
            ma20Values.getOrNull(i)?.let { ma ->
                val y = priceToY(ma, minPrice, maxPrice)
                if (!ma20Started) {
                    ma20Path.moveTo(cx, y)
                    ma20Started = true
                } else {
                    ma20Path.lineTo(cx, y)
                }
            }
        }

        if (ma7Started) canvas.drawPath(ma7Path, ma7Paint)
        if (ma20Started) canvas.drawPath(ma20Path, ma20Paint)
    }

    private fun drawLineAreaAndVolume(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float,
        maxVolume: Float
    ) {
        linePath.reset()
        areaPath.reset()
        val halfBody = candleWidthPx / 2f
        var firstX = 0f
        var lastX = 0f

        for (i in firstIndex..lastIndex) {
            val c = candles[i]
            val cx = candleIndexToCenterX(i)
            val cy = priceToY(c.close, minPrice, maxPrice)

            if (i == firstIndex) {
                firstX = cx
                linePath.moveTo(cx, cy)
                areaPath.moveTo(cx, cy)
            } else {
                linePath.lineTo(cx, cy)
                areaPath.lineTo(cx, cy)
            }
            lastX = cx

            // Vẽ cột Volume
            val paint = if (c.isBullish) bullPaint else bearPaint
            val volHeight = (c.volume / maxVolume) * volumeChartRect.height() * introProgress
            paint.alpha = 120
            canvas.drawRect(
                cx - halfBody,
                volumeChartRect.bottom - volHeight,
                cx + halfBody,
                volumeChartRect.bottom,
                paint
            )
            paint.alpha = 255
        }

        areaPath.lineTo(lastX, priceChartRect.bottom)
        areaPath.lineTo(firstX, priceChartRect.bottom)
        areaPath.close()

        canvas.drawPath(areaPath, lineAreaPaint)
        canvas.drawPath(linePath, linePricePaint)
    }

    private fun drawLivePriceIndicator(canvas: Canvas, minPrice: Float, maxPrice: Float) {
        val lastCandle = candles.lastOrNull() ?: return
        val y = priceToY(lastCandle.close, minPrice, maxPrice)
        if (y !in priceChartRect.top..priceChartRect.bottom) return

        livePriceLinePaint.color =
            if (lastCandle.isBullish) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        canvas.drawLine(priceChartRect.left, y, priceChartRect.right, y, livePriceLinePaint)

        // Chấm sáng nhấp nháy ở cây nến cuối cùng nếu đang trong vùng nhìn thấy
        val lastX = candleIndexToCenterX(candles.lastIndex)
        if (lastX in priceChartRect.left..priceChartRect.right) {
            pulsePaint.color = livePriceLinePaint.color
            val maxPulseRadius = dpToPx(11f)
            val currentRadius = dpToPx(3.5f) + maxPulseRadius * livePulseFraction
            pulsePaint.alpha = ((1f - livePulseFraction) * 140).toInt().coerceIn(0, 255)
            canvas.drawCircle(lastX, y, currentRadius, pulsePaint)

            pulsePaint.alpha = 255
            canvas.drawCircle(lastX, y, dpToPx(3.5f), pulsePaint)
        }
    }

    private fun drawCrosshair(canvas: Canvas, index: Int, minPrice: Float, maxPrice: Float) {
        val candle = candles[index]
        val cx = candleIndexToCenterX(index)
        if (cx !in priceChartRect.left..priceChartRect.right) return

        val cy = priceToY(candle.close, minPrice, maxPrice)

        // Đường gióng dọc & ngang
        canvas.drawLine(cx, priceChartRect.top, cx, volumeChartRect.bottom, crosshairPaint)
        canvas.drawLine(priceChartRect.left, cy, priceChartRect.right, cy, crosshairPaint)

        // Nhãn giá bên trục Y
        val priceLabel = formatPrice(candle.close)
        val badgeH = dpToPx(18f)
        tempRect.set(
            priceChartRect.right + dpToPx(2f),
            cy - badgeH / 2f,
            width - paddingRight - dpToPx(2f),
            cy + badgeH / 2f
        )
        canvas.drawRoundRect(tempRect, dpToPx(4f), dpToPx(4f), badgeBgPaint)
        canvas.drawText(
            priceLabel,
            tempRect.left + dpToPx(4f),
            cy + dpToPx(3.5f),
            badgeTextPaint
        )

        // Nhãn thời gian bên dưới trục X
        val timeText = candle.timeLabel
        val timeW = badgeTextPaint.measureText(timeText) + dpToPx(12f)
        val clampedLeft = (cx - timeW / 2f).coerceIn(priceChartRect.left, priceChartRect.right - timeW)
        tempRect.set(
            clampedLeft,
            timeAxisRect.top + dpToPx(2f),
            clampedLeft + timeW,
            timeAxisRect.bottom
        )
        canvas.drawRoundRect(tempRect, dpToPx(4f), dpToPx(4f), badgeBgPaint)
        canvas.drawText(
            timeText,
            tempRect.left + dpToPx(6f),
            tempRect.centerY() + dpToPx(3.5f),
            badgeTextPaint
        )
    }

    private fun drawHeaderInfo(canvas: Canvas) {
        val displayCandle = if (isCrosshairActive && selectedCandleIndex in candles.indices) {
            candles[selectedCandleIndex]
        } else {
            candles.last()
        }
        val prevClose = if (displayCandle.index > 0) {
            candles[displayCandle.index - 1].close
        } else {
            displayCandle.open
        }
        val diff = displayCandle.close - prevClose
        val pct = if (prevClose != 0f) (diff / prevClose) * 100f else 0f
        val sign = if (diff >= 0) "+" else ""

        // Dòng 1: Mã cổ phiếu + Giá khớp + % thay đổi
        val symbolText = "FPT • VNINDEX"
        canvas.drawText(
            symbolText,
            headerRect.left,
            headerRect.top + dpToPx(18f),
            titleTextPaint
        )

        val symbolWidth = titleTextPaint.measureText(symbolText)
        val priceStatusPaint = if (diff >= 0) bullPaint else bearPaint
        priceStatusPaint.textSize = spToPx(15f)
        priceStatusPaint.isFakeBoldText = true
        val priceText = "${formatPrice(displayCandle.close)} ($sign${String.format("%.2f", pct)}%)"
        canvas.drawText(
            priceText,
            headerRect.left + symbolWidth + dpToPx(10f),
            headerRect.top + dpToPx(18f),
            priceStatusPaint
        )

        // Dòng 2: Thông số O - H - L - C - Vol
        val ohlcText = "T:${displayCandle.timeLabel}   " +
            "O:${formatPrice(displayCandle.open)}   " +
            "H:${formatPrice(displayCandle.high)}   " +
            "L:${formatPrice(displayCandle.low)}   " +
            "C:${formatPrice(displayCandle.close)}   " +
            "V:${displayCandle.volume.toInt()}K"
        canvas.drawText(
            ohlcText,
            headerRect.left,
            headerRect.top + dpToPx(38f),
            subtitleTextPaint
        )

        // Dòng 3: Chú thích MA7 & MA20
        val maIndex = displayCandle.index
        val ma7Str = ma7Values.getOrNull(maIndex)?.let { formatPrice(it) } ?: "--"
        val ma20Str = ma20Values.getOrNull(maIndex)?.let { formatPrice(it) } ?: "--"

        ma7Paint.style = Paint.Style.FILL
        ma7Paint.textSize = spToPx(10.5f)
        val ma7Label = "MA7: $ma7Str"
        canvas.drawText(ma7Label, headerRect.left, headerRect.top + dpToPx(53f), ma7Paint)
        ma7Paint.style = Paint.Style.STROKE

        ma20Paint.style = Paint.Style.FILL
        ma20Paint.textSize = spToPx(10.5f)
        canvas.drawText(
            "MA20: $ma20Str",
            headerRect.left + dpToPx(90f),
            headerRect.top + dpToPx(53f),
            ma20Paint
        )
        ma20Paint.style = Paint.Style.STROKE
    }

    // =========================================================================
    // 7. VÒNG ĐỜI TƯƠNG TÁC CẢM ỨNG & CUỘN VẬT LÝ (onTouchEvent & computeScroll)
    // • [CÂU #007]: Luồng Touch Event Dispatch, ACTION_CANCEL & requestDisallowInterceptTouchEvent
    // =========================================================================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // [CÂU #007]: Khi bắt đầu chạm (ACTION_DOWN), yêu cầu ViewGroup cha (như ScrollView)
        // KHÔNG được cướp sự kiện (requestDisallowInterceptTouchEvent = true).
        // Khi thả tay (ACTION_UP) hoặc bị hủy (ACTION_CANCEL), nhả cờ lại cho cha.
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        val scaleHandled = scaleDetector.onTouchEvent(event)
        val gestureHandled = gestureDetector.onTouchEvent(event)

        if (isCrosshairActive && event.actionMasked == MotionEvent.ACTION_MOVE && !scaleDetector.isInProgress) {
            selectedCandleIndex = xToCandleIndex(event.x)
            invalidate()
        }

        return scaleHandled || gestureHandled || super.onTouchEvent(event)
    }

    /**
     * Được hệ thống gọi tự động trong quá trình draw() khi OverScroller đang chạy hiệu ứng Fling.
     */
    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            scrollOffsetPx = scroller.currX.toFloat().coerceIn(0f, maxScrollOffsetPx)
            postInvalidateOnAnimation()
        }
    }

    // =========================================================================
    // 8. VÒNG ĐỜI HIỂN THỊ & FOCUS (onVisibilityChanged & onWindowFocusChanged)
    // =========================================================================
    /**
     * Được gọi khi bản thân View hoặc ViewGroup tổ tiên đổi trạng thái VISIBLE / INVISIBLE / GONE.
     * Rất quan trọng để tạm dừng Animator, tránh tốn CPU ngầm khi View đã bị ẩn.
     */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        val visName = when (visibility) {
            VISIBLE -> "VISIBLE"
            INVISIBLE -> "INVISIBLE"
            GONE -> "GONE"
            else -> visibility.toString()
        }
        logLifecycle("8. onVisibilityChanged(): $visName")

        if (visibility == VISIBLE) {
            pulseAnimator?.resume()
        } else {
            pulseAnimator?.pause()
        }
    }

    /**
     * Được gọi khi cửa sổ chứa View nhận hoặc mất Focus (ví dụ có Dialog đè lên hoặc bấm Home).
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        logLifecycle("9. onWindowFocusChanged(): hasFocus=$hasWindowFocus")
        if (hasWindowFocus && visibility == VISIBLE) {
            pulseAnimator?.resume()
        } else {
            pulseAnimator?.pause()
        }
    }

    // =========================================================================
    // 9. LƯU & KHÔI PHỤC TRẠNG THÁI (onSaveInstanceState / onRestoreInstanceState)
    // • [CÂU #009]: View tự lưu State qua BaseSavedState khi Configuration Change (xoay màn hình)
    // =========================================================================
    /**
     * [CÂU #009]: Lưu trạng thái cuộn, mức độ zoom, nến đang chọn khi xoay màn hình.
     * Để hoạt động, bắt buộc View phải có id cụ thể và isSaveEnabled = true (đã set trong init).
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        logLifecycle("10. onSaveInstanceState(): [CÂU #009] Lưu vị trí cuộn, mức zoom & chế độ chart")
        return SavedState(superState).apply {
            savedCandleWidth = this@CustomView.candleWidthPx
            savedScrollOffset = this@CustomView.scrollOffsetPx
            savedSelectedIndex = this@CustomView.selectedCandleIndex
            savedCrosshairActive = this@CustomView.isCrosshairActive
            savedChartModeOrdinal = this@CustomView.chartMode.ordinal
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            candleWidthPx = state.savedCandleWidth
            candleSpacingPx = (candleWidthPx * 0.28f).coerceAtLeast(dpToPx(2f))
            scrollOffsetPx = state.savedScrollOffset
            selectedCandleIndex = state.savedSelectedIndex
            isCrosshairActive = state.savedCrosshairActive
            chartMode = ChartMode.entries.getOrElse(state.savedChartModeOrdinal) {
                ChartMode.CANDLESTICK
            }
            logLifecycle("11. onRestoreInstanceState(): [CÂU #009] Khôi phục thành công trạng thái chart")
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    // =========================================================================
    // 10. GỠ KHỎI WINDOW & DỌN DẸP TÀI NGUYÊN (onDetachedFromWindow)
    // =========================================================================
    /**
     * Được gọi khi View bị gỡ khỏi Window (Activity đóng hoặc View bị remove khỏi bố cục).
     * BẮT BUỘC dừng mọi ValueAnimator, Handler, Callback tại đây để tránh Memory Leak.
     */
    override fun onDetachedFromWindow() {
        logLifecycle("12. onDetachedFromWindow(): Huỷ Animator & Scroller để chống Memory Leak")
        introAnimator?.cancel()
        introAnimator = null
        pulseAnimator?.cancel()
        pulseAnimator = null
        scroller.abortAnimation()
        super.onDetachedFromWindow()
    }

    // =========================================================================
    // CÁC HÀM ĐIỀU KHIỂN CÔNG KHAI (ĐỂ TEST TRỰC TIẾP TỪ MÀN HÌNH CHÍNH)
    // =========================================================================

    /**
     * Mô phỏng một phiên giao dịch mới (thêm 1 cây nến mới và gọi invalidate() -> chỉ chạy onDraw).
     */
    fun appendLiveCandle() {
        val last = candles.lastOrNull() ?: return
        val nextIndex = last.index + 1
        val hour = 9 + (nextIndex / 12) % 6
        val minute = (nextIndex % 12) * 5
        val label = String.format("%02d:%02d", hour, minute)

        val delta = (Random.nextFloat() - 0.48f) * 3.2f
        val open = last.close
        val close = (open + delta).coerceAtLeast(15f)
        val high = max(open, close) + Random.nextFloat() * 1.8f
        val low = min(open, close) - Random.nextFloat() * 1.8f
        val volume = 120f + Random.nextFloat() * 380f

        val wasAtRightEdge = scrollOffsetPx >= maxScrollOffsetPx - candleStepPx()
        candles.add(Candle(nextIndex, label, open, high, low, close, volume))
        recalculateMovingAverages()
        updateScrollBounds()

        if (wasAtRightEdge) {
            scrollOffsetPx = maxScrollOffsetPx
        }
        logLifecycle("appendLiveCandle() -> invalidate() (Chỉ gọi onDraw, không đo lại)")
        invalidate()
    }

    /**
     * Chạy lại hiệu ứng mọc nến (minh họa ValueAnimator + invalidate()).
     */
    fun replayIntroAnimation() {
        logLifecycle("replayIntroAnimation() -> Chạy lại ValueAnimator")
        startIntroAnimation()
    }

    private fun startIntroAnimation() {
        introAnimator?.cancel()
        introAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                introProgress = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { anim ->
                livePulseFraction = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // =========================================================================
    // HÀM BỔ TRỢ TÍNH TOÁN TOẠ ĐỘ & DỮ LIỆU
    // =========================================================================

    private fun generateInitialStockData(count: Int) {
        candles.clear()
        val random = Random(42)
        var currentPrice = 128.5f

        for (i in 0 until count) {
            val hour = 9 + (i / 12) % 6
            val minute = (i % 12) * 5
            val timeLabel = String.format("%02d:%02d", hour, minute)

            val change = (random.nextFloat() - 0.485f) * 3.0f
            val open = currentPrice
            val close = (open + change).coerceAtLeast(20f)
            val high = max(open, close) + random.nextFloat() * 1.9f
            val low = min(open, close) - random.nextFloat() * 1.9f
            val volume = 100f + random.nextFloat() * 420f

            candles.add(Candle(i, timeLabel, open, high, low, close, volume))
            currentPrice = close
        }
        recalculateMovingAverages()
    }

    private fun recalculateMovingAverages() {
        ma7Values.clear()
        ma20Values.clear()
        var sum7 = 0f
        var sum20 = 0f

        for (i in candles.indices) {
            val close = candles[i].close
            sum7 += close
            sum20 += close

            if (i >= 7) sum7 -= candles[i - 7].close
            if (i >= 20) sum20 -= candles[i - 20].close

            ma7Values.add(if (i >= 6) sum7 / 7f else null)
            ma20Values.add(if (i >= 19) sum20 / 20f else null)
        }
    }

    private fun candleStepPx(): Float = candleWidthPx + candleSpacingPx

    private fun updateScrollBounds() {
        if (priceChartRect.width() <= 0f) return
        val totalContentWidth = candles.size * candleStepPx() + dpToPx(24f)
        maxScrollOffsetPx = max(0f, totalContentWidth - priceChartRect.width())
        scrollOffsetPx = scrollOffsetPx.coerceIn(0f, maxScrollOffsetPx)
    }

    private fun candleIndexToCenterX(index: Int): Float {
        return priceChartRect.left + index * candleStepPx() + candleStepPx() / 2f - scrollOffsetPx
    }

    private fun xToCandleIndex(x: Float): Int {
        val rawIndex = ((x - priceChartRect.left + scrollOffsetPx) / candleStepPx()).toInt()
        return rawIndex.coerceIn(0, candles.lastIndex)
    }

    private fun priceToY(price: Float, minPrice: Float, maxPrice: Float): Float {
        val ratio = (price - minPrice) / (maxPrice - minPrice)
        return priceChartRect.bottom - ratio * priceChartRect.height()
    }

    private fun formatPrice(price: Float): String = String.format("%.2f", price)

    private fun measureModeName(mode: Int): String = when (mode) {
        MeasureSpec.EXACTLY -> "EXACTLY"
        MeasureSpec.AT_MOST -> "AT_MOST"
        MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        else -> "UNKNOWN"
    }

    private fun logLifecycle(message: String) {
        onLifecycleEvent?.invoke(message)
    }

    // =========================================================================
    // [CÂU #010]: Chuyển đổi px, dp, sp trên Android 14+
    // - Dùng TypedValue.applyDimension với COMPLEX_UNIT_DIP và COMPLEX_UNIT_SP
    // - Bẫy: Từ Android 14 có Non-linear font scaling (chữ to tăng chậm hơn), không được
    //   nhân nhẩm sp * density mà bắt buộc dùng TypedValue hoặc Resources.
    // =========================================================================
    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    // =========================================================================
    // LỚP LƯU TRẠNG THÁI (BaseSavedState)
    // =========================================================================
    private class SavedState : BaseSavedState {
        var savedCandleWidth: Float = 0f
        var savedScrollOffset: Float = 0f
        var savedSelectedIndex: Int = -1
        var savedCrosshairActive: Boolean = false
        var savedChartModeOrdinal: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            savedCandleWidth = parcel.readFloat()
            savedScrollOffset = parcel.readFloat()
            savedSelectedIndex = parcel.readInt()
            savedCrosshairActive = parcel.readInt() == 1
            savedChartModeOrdinal = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(savedCandleWidth)
            out.writeFloat(savedScrollOffset)
            out.writeInt(savedSelectedIndex)
            out.writeInt(if (savedCrosshairActive) 1 else 0)
            out.writeInt(savedChartModeOrdinal)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
