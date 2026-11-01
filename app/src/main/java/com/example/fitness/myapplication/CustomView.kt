package com.example.fitness.myapplication

// Import lớp quản lý hiệu ứng hoạt ảnh biến đổi giá trị theo thời gian
import android.animation.ValueAnimator
// Import Context - cung cấp quyền truy cập tài nguyên (Resources), Theme và thông tin hệ thống
import android.content.Context
// Import Canvas - bề mặt vẽ đồ họa 2D chính của Android
import android.graphics.Canvas
// Import Color - cung cấp các tiện ích xử lý màu sắc (RGB, Hex, Alpha)
import android.graphics.Color
// Import DashPathEffect - tạo hiệu ứng nét đứt cho đường kẻ (dùng cho Crosshair và Live Price)
import android.graphics.DashPathEffect
// Import LinearGradient - tạo dải màu chuyển tiếp gradient (dùng cho biểu đồ Line Area)
import android.graphics.LinearGradient
// Import Paint - cấu hình phong cách vẽ: màu sắc, độ dày, kiểu nét vẽ (Stroke/Fill), font chữ
import android.graphics.Paint
// Import Path - tập hợp các toạ độ hình học đa giác, đường cong phức tạp để vẽ lên Canvas
import android.graphics.Path
// Import RectF - hình chữ nhật toạ độ số thực (Float) dùng để quản lý ranh giới các vùng vẽ
import android.graphics.RectF
// Import Shader - lớp cơ sở cho việc tô màu bề mặt bằng gradient hoặc hoa văn bitmap
import android.graphics.Shader
// Import Parcel - đối tượng vùng nhớ tạm dùng để tuần tự hoá dữ liệu nhị phân khi lưu State
import android.os.Parcel
// Import Parcelable - giao diện bắt buộc của Android để đóng gói dữ liệu truyền qua IPC hoặc lưu Instance State
import android.os.Parcelable
// Import AttributeSet - tập hợp các thuộc tính XML được truyền vào khi View được khai báo trong file layout
import android.util.AttributeSet
// Import TypedValue - tiện ích chuyển đổi linh hoạt giữa các đơn vị đo lường (dp, sp, px, pt, in, mm)
import android.util.TypedValue
// Import GestureDetector - bộ nhận diện các cử chỉ chạm thông thường (Tap, Double Tap, Scroll, Fling, Long Press)
import android.view.GestureDetector
// Import MotionEvent - chứa toàn bộ thông tin sự kiện cảm ứng chạm (toạ độ X/Y, con trỏ, loại ACTION)
import android.view.MotionEvent
// Import ScaleGestureDetector - bộ nhận diện cử chỉ thu phóng 2 ngón tay (Pinch-to-Zoom)
import android.view.ScaleGestureDetector
// Import View - lớp cơ sở của toàn bộ cây phân cấp giao diện người dùng trên Android
import android.view.View
// Import ViewConfiguration - cung cấp các hằng số chuẩn của hệ thống (TouchSlop, FlingVelocity, Timeout)
import android.view.ViewConfiguration
// Import DecelerateInterpolator - bộ giảm tốc hoạt ảnh (bắt đầu nhanh, chậm dần về cuối)
import android.view.animation.DecelerateInterpolator
// Import OverScroller - công cụ tính toán vật lý quán tính cuộn (Fling) mượt mà không bị giật lag
import android.widget.OverScroller
// Import các hàm toán học tối ưu từ Kotlin Math
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Biểu đồ chứng khoán chuyên sâu (Candlestick + Volume + MA7/MA20 + Crosshair)
 * được thiết kế để minh họa toàn diện và trả lời trực tiếp 10 CÂU HỎI ĐẦU TIÊN (#001 - #010):
 *
 * • [CÂU #001]: Các callback vòng đời chính (Constructor -> onAttachedToWindow -> onMeasure -> onSizeChanged -> onLayout -> onDraw -> onDetachedFromWindow).
 * • [CÂU #002]: Cơ chế MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED) & resolveSize trong onMeasure().
 * • [CÂU #003]: Khác biệt invalidate() (chỉ vẽ lại onDraw) và requestLayout() (buộc hệ thống tính lại kích thước từ onMeasure).
 * • [CÂU #004]: 4 Constructors của View & Bẫy nguy hiểm của chú thích @JvmOverloads (tự điền defStyleAttr = 0 làm mất style hệ thống).
 * • [CÂU #005]: Thứ tự ưu tiên attribute: XML direct > style="" > defStyleAttr > defStyleRes > Theme mặc định.
 * • [CÂU #006]: Tối ưu onDraw: Tuyệt đối không tạo Paint/Path mới trong onDraw, Viewport Culling, canvas.clipRect.
 * • [CÂU #007]: Luồng Touch Event, ACTION_CANCEL & requestDisallowInterceptTouchEvent().
 * • [CÂU #008]: Accessibility & TalkBack: contentDescription, isFocusable, kích thước tối thiểu 48dp.
 * • [CÂU #009]: Tự lưu State khi xoay màn hình (onSaveInstanceState, onRestoreInstanceState, BaseSavedState).
 * • [CÂU #010]: Chuyển đổi dp, sp trên Android 14+ với TypedValue.applyDimension hỗ trợ Non-linear Font Scaling.
 */
class CustomView @JvmOverloads constructor(
    // Tham số 1: Context của Activity hoặc Application đang chứa View này
    context: Context,
    // Tham số 2: Tập thuộc tính được định nghĩa trực tiếp trong tệp XML (nếu khởi tạo từ code Java/Kotlin thì là null)
    attrs: AttributeSet? = null,
    // Tham số 3: Thuộc tính style mặc định được tham chiếu trong Theme hiện tại (mặc định bằng 0)
    defStyleAttr: Int = 0
    // =========================================================================
    // [CÂU #004 - BẪY PHỎNG VẤN VỀ @JvmOverloads]:
    // Annotation @JvmOverloads sẽ tự động sinh ra 3 constructors trong bytecode Java:
    // 1. constructor(context)
    // 2. constructor(context, attrs)
    // 3. constructor(context, attrs, defStyleAttr)
    // BẪY NGUY HIỂM: Nếu bạn kế thừa một Widget có sẵn của Android (như Button, EditText, Switch),
    // các widget đó luôn có style mặc định (ví dụ android.R.attr.buttonStyle).
    // Nếu dùng @JvmOverloads, tham số defStyleAttr sẽ bị gán cứng bằng 0, làm MẤT TOÀN BỘ style mặc định!
    // -> QUY TẮC: Khi kế thừa trực tiếp lớp View thì dùng @JvmOverloads an toàn.
    //             Khi kế thừa Button/EditText thì phải tự viết tay từng constructor và gọi super đúng defStyleAttr!
    // =========================================================================
) : View(context, attrs, defStyleAttr) {

    /**
     * Lớp dữ liệu đại diện cho 1 cây nến trong phiên giao dịch chứng khoán (OHLCV):
     * @property index Số thứ tự của cây nến trong danh sách
     * @property timeLabel Nhãn thời gian hiển thị dưới trục X (ví dụ "09:30")
     * @property open Giá mở cửa (Open)
     * @property high Giá cao nhất trong phiên (High)
     * @property low Giá thấp nhất trong phiên (Low)
     * @property close Giá đóng cửa (Close)
     * @property volume Khối lượng cổ phiếu giao dịch (Volume)
     */
    data class Candle(
        val index: Int,
        val timeLabel: String,
        val open: Float,
        val high: Float,
        val low: Float,
        val close: Float,
        val volume: Float
    ) {
        // Thuộc tính tính toán: true nếu giá đóng cửa >= giá mở cửa (Nến tăng / Bullish), false là Nến giảm / Bearish
        val isBullish: Boolean get() = close >= open
    }

    /**
     * Định nghĩa 2 chế độ hiển thị biểu đồ giá:
     * - CANDLESTICK: Biểu đồ nến Nhật truyền thống kèm đường trung bình MA
     * - LINE_AREA: Biểu đồ đường liên tục có dải màu chuyển tiếp (Area Chart)
     */
    enum class ChartMode {
        CANDLESTICK,
        LINE_AREA
    }

    // Callback lambda thông báo sự kiện vòng đời cho màn hình bên ngoài (MainActivity) để in log theo dõi
    var onLifecycleEvent: ((event: String) -> Unit)? = null

    // Bộ đếm số lần phương thức onDraw() được gọi (dùng để chứng minh invalidate() chỉ chạy onDraw)
    var drawCount: Int = 0
        private set

    // =========================================================================
    // NGUỒN DỮ LIỆU CỦA BIỂU ĐỒ (DÙNG MUTABLELIST ĐỂ TÁI SỬ DỤNG VÙNG NHỚ)
    // =========================================================================
    // Danh sách lưu trữ toàn bộ các cây nến
    private val candles = mutableListOf<Candle>()
    // Danh sách giá trị đường trung bình MA7 (7 phiên gần nhất)
    private val ma7Values = mutableListOf<Float?>()
    // Danh sách giá trị đường trung bình MA20 (20 phiên gần nhất)
    private val ma20Values = mutableListOf<Float?>()

    // Chế độ biểu đồ hiện tại: Khi thay đổi setter, chỉ gọi invalidate() để vẽ lại (onDraw), không cần đo lại (onMeasure)
    var chartMode: ChartMode = ChartMode.CANDLESTICK
        set(value) {
            // Kiểm tra xem giá trị mới có khác giá trị cũ hay không để tránh vẽ lại dư thừa
            if (field != value) {
                field = value
                // Ghi log chứng minh invalidate() được kích hoạt
                logLifecycle("invalidate() -> Đổi chế độ sang $value (chỉ gọi onDraw)")
                // [CÂU #003]: invalidate() đánh dấu vùng hiển thị bị bẩn (dirty) và yêu cầu hệ thống gọi onDraw() ở frame tiếp theo
                invalidate()
            }
        }

    // =========================================================================
    // [CÂU #006]: CẤP PHÁT CÁC ĐỐI TƯỢNG VÙNG VẼ (RECTF) TẠI ĐÂY (TRÁNH CẤP PHÁT TRONG ONDRAW)
    // =========================================================================
    // Vùng chứa thông tin Header ở trên cùng (Mã cổ phiếu, giá hiện tại, % tăng giảm, OHLCV)
    private val headerRect = RectF()
    // Vùng vẽ biểu đồ giá (Nến hoặc Đường)
    private val priceChartRect = RectF()
    // Vùng vẽ biểu đồ cột khối lượng giao dịch (Volume)
    private val volumeChartRect = RectF()
    // Vùng vẽ trục nhãn thời gian ở đáy biểu đồ
    private val timeAxisRect = RectF()
    // Đối tượng RectF tạm dùng để tính toán nhanh khung chữ, tránh new đối tượng
    private val tempRect = RectF()

    // =========================================================================
    // CÁC THÔNG SỐ VÀ TRẠNG THÁI TƯƠNG TÁC (ZOOM, CUỘN, TRA CỨU GIÁ)
    // =========================================================================
    // Chiều rộng của 1 thân nến tính theo pixel (mặc định 14dp)
    private var candleWidthPx = dpToPx(14f)
    // Chiều rộng nến tối thiểu cho phép khi người dùng thu nhỏ (Zoom Out) hết cỡ (6dp)
    private val minCandleWidthPx = dpToPx(6f)
    // Chiều rộng nến tối đa cho phép khi người dùng phóng to (Zoom In) hết cỡ (38dp)
    private val maxCandleWidthPx = dpToPx(38f)
    // Khoảng cách giữa 2 cây nến liền kề tính bằng pixel (mặc định 4dp)
    private var candleSpacingPx = dpToPx(4f)

    // Khoảng cách cuộn hiện tại tính từ mốc bắt đầu (pixel)
    private var scrollOffsetPx = 0f
    // Khoảng cách cuộn tối đa cho phép để không cuộn tràn ra ngoài dữ liệu (pixel)
    private var maxScrollOffsetPx = 0f
    // Chỉ số của cây nến đang được người dùng chạm hoặc chọn qua Crosshair (-1 nghĩa là không chọn)
    private var selectedCandleIndex: Int = -1
    // Trạng thái bật/tắt tia ngắm chữ thập tra cứu giá (Crosshair)
    private var isCrosshairActive: Boolean = false

    // =========================================================================
    // BIẾN TIẾN TRÌNH HOẠT ẢNH (ANIMATION PROGRESS)
    // =========================================================================
    // Tiến trình mở màn biểu đồ (từ 0.0f đến 1.0f): Các cây nến sẽ mọc cao dần lên
    private var introProgress = 1f
    // Tiến trình nhấp nháy của chấm sáng giá trực tiếp (Live Price Pulse) (từ 0.0f đến 1.0f)
    private var livePulseFraction = 0f

    // =========================================================================
    // [CÂU #006 - TỐI ƯU ONDRAW]: KHỞI TẠO TẤT CẢ PAINT VÀ PATH TRONG KHỞI TẠO
    // TUYỆT ĐỐI KHÔNG DÙNG "val paint = Paint()" TRONG HÀM onDraw()
    // Vì onDraw được gọi 60 - 120 lần/giây, cấp phát object trong onDraw sẽ làm bộ nhớ heap phình to,
    // kích hoạt Garbage Collection (GC) liên tục gây giật lag khung hình (Jank/Stutter)!
    // =========================================================================

    // Cọ vẽ nền tối phong cách bảng điện chứng khoán
    private val bgPaint = Paint().apply {
        color = Color.parseColor("#0F172A") // Màu Slate 900
        style = Paint.Style.FILL // Tô đặc toàn bộ vùng vẽ
    }

    // Cọ vẽ đường lưới (Grid Lines)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B") // Màu xám xanh Slate 800
        strokeWidth = dpToPx(1f) // Độ dày đường kẻ 1dp
        style = Paint.Style.STROKE // Chỉ vẽ viền đường kẻ
    }

    // Cọ vẽ nhãn chữ trên các trục toạ độ X và Y
    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8") // Màu xám nhạt Slate 400
        textSize = spToPx(10f) // Kích thước chữ 10sp (tự co giãn theo cài đặt trợ năng hệ thống)
    }

    // Cọ vẽ tiêu đề mã cổ phiếu trong Header
    private val titleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F8FAFC") // Màu trắng sáng
        textSize = spToPx(16f) // Kích thước chữ 16sp
        isFakeBoldText = true // Kích hoạt chữ in đậm giả lập
    }

    // Cọ vẽ phụ đề và thông số OHLCV trong Header
    private val subtitleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBD5E1") // Màu xám trắng
        textSize = spToPx(11.5f) // Kích thước chữ 11.5sp
    }

    // Cọ vẽ nến tăng giá (Xanh lục / Bullish)
    private val bullPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981") // Xanh ngọc Emerald
        style = Paint.Style.FILL
        strokeWidth = dpToPx(1.5f) // Độ dày râu nến 1.5dp
    }

    // Cọ vẽ nến giảm giá (Đỏ / Bearish)
    private val bearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444") // Đỏ Red 500
        style = Paint.Style.FILL
        strokeWidth = dpToPx(1.5f) // Độ dày râu nến 1.5dp
    }

    // Cọ vẽ đường trung bình động MA7
    private val ma7Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B") // Màu vàng hổ phách
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.6f) // Độ dày đường MA là 1.6dp
    }

    // Cọ vẽ đường trung bình động MA20
    private val ma20Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8") // Màu xanh dương Cyan
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.6f) // Độ dày đường MA là 1.6dp
    }

    // Cọ vẽ đường giá chính trong chế độ Line Chart
    private val linePricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22D3EE") // Màu xanh Cyan rực rỡ
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2.2f) // Nét vẽ dày 2.2dp
    }

    // Cọ vẽ dải màu chuyển tiếp phía dưới đường giá (Area Gradient)
    private val lineAreaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL // Tô đặc diện tích nằm dưới đường giá
    }

    // Cọ vẽ đường đứt nét gióng ngang mức giá hiện tại (Live Price Line)
    private val livePriceLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1f)
        // Tạo hiệu ứng nét đứt: dài 6dp, cách 4dp
        pathEffect = DashPathEffect(floatArrayOf(dpToPx(6f), dpToPx(4f)), 0f)
    }

    // Cọ vẽ tia ngắm chữ thập tra cứu giá (Crosshair)
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E2E8F0") // Trắng bạc
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1f)
        // Tạo hiệu ứng nét đứt: dài 4dp, cách 4dp
        pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(4f)), 0f)
    }

    // Cọ vẽ nền hộp nhãn giá và thời gian khi di chuyển tia ngắm (Badge Background)
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155") // Nền thẻ Slate 700
        style = Paint.Style.FILL
    }

    // Cọ vẽ chữ số bên trong hộp nhãn giá và thời gian
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = spToPx(10.5f)
        isFakeBoldText = true
    }

    // Cọ vẽ chấm sáng nhấp nháy tại cây nến cuối cùng (Live Pulse)
    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.FILL
    }

    // =========================================================================
    // CÁC ĐỐI TƯỢNG PATH ĐƯỢC CẤP PHÁT SẴN ĐỂ TÁI SỬ DỤNG TRONG MỖI FRAME
    // =========================================================================
    private val ma7Path = Path() // Lưu đường vẽ MA7
    private val ma20Path = Path() // Lưu đường vẽ MA20
    private val linePath = Path() // Lưu đường vẽ Line Chart
    private val areaPath = Path() // Lưu vùng khép kín của Area Gradient

    // =========================================================================
    // BỘ XỬ LÝ VẬT LÝ CUỘN VÀ CỬ CHỈ CHẠM
    // =========================================================================
    // OverScroller tính toán toạ độ quán tính (Fling) mượt mà chuẩn 60/120fps
    private val scroller = OverScroller(context)
    // Ngưỡng khoảng cách tối thiểu ngón tay phải di chuyển trước khi hệ thống coi là hành động vuốt (Touch Slop)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    // Bộ nhận diện cử chỉ phóng to thu nhỏ 2 ngón (ScaleGestureDetector)
    private val scaleDetector: ScaleGestureDetector
    // Bộ nhận diện cử chỉ chạm 1 ngón (GestureDetector)
    private val gestureDetector: GestureDetector

    // =========================================================================
    // BỘ ANIMATOR ĐƯỢC QUẢN LÝ THEO VÒNG ĐỜI VIEW
    // =========================================================================
    // Hiệu ứng mọc nến lúc mở màn hình
    private var introAnimator: ValueAnimator? = null
    // Hiệu ứng nhịp đập của chấm sáng giá trực tiếp
    private var pulseAnimator: ValueAnimator? = null

    // =========================================================================
    // 1. GIAI ĐOẠN KHỞI TẠO (CONSTRUCTOR / INIT)
    // • [CÂU #008]: Accessibility - Cung cấp contentDescription & focus cho TalkBack
    // • [CÂU #009]: Điều kiện để onSaveInstanceState hoạt động (id != NO_ID & isSaveEnabled = true)
    // =========================================================================
    init {
        // [CÂU #009]: Hai điều kiện BẮT BUỘC để Android tự động kích hoạt onSaveInstanceState khi xoay màn hình:
        // 1. View phải có một ID cụ thể trong cây View (không được là View.NO_ID).
        // Nếu trong XML chưa gán id, ta tự động sinh một ID duy nhất bằng generateViewId()
        if (id == NO_ID) {
            id = generateViewId()
        }
        // 2. Thuộc tính isSaveEnabled phải mang giá trị true
        isSaveEnabled = true

        // [CÂU #008]: Hỗ trợ khả năng tiếp cận (Accessibility & TalkBack) cho người khiếm thị:
        // Cho phép View nhận tiêu điểm (Focus) khi người dùng dùng bàn phím ngoài hoặc TalkBack
        isFocusable = true
        // Mô tả bằng âm thanh nội dung và cách thức thao tác trên biểu đồ
        contentDescription = "Biểu đồ chứng khoán FPT. Vuốt 1 ngón để cuộn, 2 ngón để phóng to/thu nhỏ, chạm để tra cứu giá."

        // Sinh 80 cây nến mẫu ban đầu để sẵn sàng hiển thị lên màn hình
        generateInitialStockData(count = 80)

        // Khởi tạo bộ lắng nghe cử chỉ phóng to thu nhỏ 2 ngón (Pinch-to-Zoom)
        scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                /**
                 * Được gọi liên tục khi 2 ngón tay đang di chuyển để co hoặc dãn khoảng cách
                 */
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // Bước nhảy cũ của mỗi cây nến (độ rộng + khoảng cách)
                    val oldStep = candleStepPx()
                    // Toạ độ X của tâm điểm nằm giữa 2 ngón tay tính theo vùng vẽ giá
                    val focusX = detector.focusX - priceChartRect.left
                    // Tính xem vị trí ngón tay đang trỏ vào cây nến thứ bao nhiêu (dạng số thực)
                    val centerCandleFloat = (scrollOffsetPx + focusX) / oldStep

                    // Nhân tỷ lệ phóng to/thu nhỏ (scaleFactor) vào độ rộng nến hiện tại
                    candleWidthPx = (candleWidthPx * detector.scaleFactor)
                        // Giới hạn độ rộng nến không vượt quá ngưỡng min và max đã định
                        .coerceIn(minCandleWidthPx, maxCandleWidthPx)
                    // Tự động điều chỉnh khoảng cách giữa các cây nến tỉ lệ theo độ rộng nến
                    candleSpacingPx = (candleWidthPx * 0.28f).coerceAtLeast(dpToPx(2f))

                    // Cập nhật lại giới hạn cuộn tối đa sau khi kích thước nến thay đổi
                    updateScrollBounds()

                    // Thuật toán Zoom neo tại tâm điểm (Focal Point Anchoring):
                    // Giữ nguyên cây nến nằm ngay dưới tâm 2 ngón tay, không làm biểu đồ bị trôi lệch sang chỗ khác
                    val newStep = candleStepPx()
                    scrollOffsetPx = (centerCandleFloat * newStep - focusX)
                        .coerceIn(0f, maxScrollOffsetPx)

                    // Yêu cầu vẽ lại frame mới với tỷ lệ zoom vừa tính
                    invalidate()
                    return true
                }
            }
        )

        // Khởi tạo bộ lắng nghe cử chỉ chạm vuốt 1 ngón (GestureDetector)
        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                /**
                 * Sự kiện chạm ngón tay đầu tiên xuống bề mặt (ACTION_DOWN):
                 * Bắt buộc phải return true để thông báo cho hệ thống biết View này muốn đón nhận
                 * toàn bộ các sự kiện tiếp theo của chu kỳ chạm (MOVE, UP, CANCEL).
                 */
                override fun onDown(e: MotionEvent): Boolean {
                    // Nếu biểu đồ đang cuộn theo quán tính từ lần vuốt trước, dừng ngay lại khi chạm tay vào
                    if (!scroller.isFinished) {
                        scroller.abortAnimation()
                    }
                    return true
                }

                /**
                 * Sự kiện người dùng chạm nhanh một lần vào màn hình (Single Tap)
                 */
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Kiểm tra xem vị trí chạm có nằm trong khu vực biểu đồ giá hoặc volume hay không
                    if (priceChartRect.contains(e.x, e.y) || volumeChartRect.contains(e.x, e.y)) {
                        val tappedIndex = xToCandleIndex(e.x)
                        // Nếu đang mở Crosshair ở chính cây nến này thì chạm lần nữa sẽ tắt tia ngắm
                        if (isCrosshairActive && selectedCandleIndex == tappedIndex) {
                            isCrosshairActive = false
                            selectedCandleIndex = -1
                        } else {
                            // Ngược lại, kích hoạt tia ngắm tại cây nến vừa chạm
                            isCrosshairActive = true
                            selectedCandleIndex = tappedIndex
                        }
                        // Vẽ lại biểu đồ để cập nhật trạng thái tia ngắm
                        invalidate()
                    }
                    return true
                }

                /**
                 * Sự kiện người dùng nhấn giữ lâu trên màn hình (Long Press)
                 */
                override fun onLongPress(e: MotionEvent) {
                    if (priceChartRect.contains(e.x, e.y) || volumeChartRect.contains(e.x, e.y)) {
                        // Kích hoạt chế độ tra cứu giá Crosshair
                        isCrosshairActive = true
                        selectedCandleIndex = xToCandleIndex(e.x)
                        // [CÂU #007]: Yêu cầu ViewGroup cha (như ScrollView) không được chặn touch khi đang nhấn giữ
                        parent?.requestDisallowInterceptTouchEvent(true)
                        invalidate()
                    }
                }

                /**
                 * Sự kiện người dùng kéo ngón tay trên màn hình (Pan / Drag Scroll)
                 */
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    // Nếu đang dùng 2 ngón tay Zoom thì bỏ qua không cuộn
                    if (scaleDetector.isInProgress) return false

                    // Cộng dồn khoảng cách di chuyển theo trục X vào biến scrollOffsetPx
                    scrollOffsetPx = (scrollOffsetPx + distanceX)
                        .coerceIn(0f, maxScrollOffsetPx) // Giới hạn không cho cuộn vượt mép

                    // Nếu tia ngắm đang bật thì cập nhật cây nến theo vị trí ngón tay đang di chuyển
                    if (isCrosshairActive) {
                        selectedCandleIndex = xToCandleIndex(e2.x)
                    }
                    // Yêu cầu vẽ lại màn hình theo vị trí cuộn mới
                    invalidate()
                    return true
                }

                /**
                 * Sự kiện người dùng vuốt nhanh rồi nhấc tay lên (Fling / Cuộn quán tính)
                 */
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    // Nếu đang zoom thì bỏ qua cuộn quán tính
                    if (scaleDetector.isInProgress) return false

                    // Bắt đầu tính toán chuyển động vật lý quán tính thông qua OverScroller
                    scroller.fling(
                        scrollOffsetPx.toInt(), // Vị trí xuất phát X
                        0,                      // Vị trí xuất phát Y (không cuộn dọc)
                        (-velocityX).toInt(),   // Vận tốc ban đầu theo trục X (đảo dấu cho đúng hướng kéo)
                        0,                      // Vận tốc theo trục Y
                        0,                      // Toạ độ X tối thiểu cho phép
                        maxScrollOffsetPx.toInt(), // Toạ độ X tối đa cho phép
                        0,                      // Toạ độ Y tối thiểu
                        0                       // Toạ độ Y tối đa
                    )
                    // [CÂU #001]: Kích hoạt hàm computeScroll() đồng bộ với nhịp làm tươi (VSYNC) tiếp theo của màn hình
                    postInvalidateOnAnimation()
                    return true
                }
            }
        )

        // In log thông báo hoàn tất giai đoạn khởi tạo Constructor
        logLifecycle("1. init: Khởi tạo Paint, Path, GestureDetector & 80 cây nến mẫu")
    }

    // =========================================================================
    // 2. GẮN VÀO CỬA SỔ (onAttachedToWindow)
    // • [CÂU #001]: Callback vòng đời - Điểm lý tưởng nhất để bắt đầu Animator / Coroutine / BroadcastReceiver
    // =========================================================================
    override fun onAttachedToWindow() {
        // Luôn luôn phải gọi super trước
        super.onAttachedToWindow()
        logLifecycle("2. onAttachedToWindow(): Gắn vào Window -> Bắt đầu Animator")
        // Tránh chạy animation khi đang ở chế độ xem trước Layout Preview trong Android Studio
        if (!isInEditMode) {
            startIntroAnimation() // Chạy hoạt ảnh mọc nến mở màn
            startPulseAnimation() // Chạy nhịp tim nhấp nháy cho giá trực tiếp
        }
    }

    // =========================================================================
    // 3. ĐO ĐẠC KÍCH THƯỚC (onMeasure)
    // • [CÂU #002]: Giải mã MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED) & sử dụng resolveSize()
    // • BẪY PHỎNG VẤN: Luôn phải cộng thêm padding (Left + Right, Top + Bottom) vào kích thước mong muốn!
    // =========================================================================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // [CÂU #002 - BẪY PHỎNG VẤN]: Nếu quên cộng paddingLeft/Right, nội dung vẽ sẽ bị co hẹp hoặc tràn ra ngoài mép
        // Kích thước chiều rộng lý tưởng mong muốn: 360dp + padding 2 bên
        val desiredWidth = dpToPx(360f).toInt() + paddingLeft + paddingRight
        // Kích thước chiều cao lý tưởng mong muốn: 340dp + padding trên dưới
        val desiredHeight = dpToPx(340f).toInt() + paddingTop + paddingBottom

        // [CÂU #002]: Hàm resolveSize() của View phân giải kích thước chuẩn theo thông điệp từ ViewGroup cha:
        // - EXACTLY: Lấy đúng kích thước cha áp đặt (ví dụ layout_width="match_parent" hoặc 200dp)
        // - AT_MOST: Lấy min(desired, size_cha) (ví dụ layout_width="wrap_content")
        // - UNSPECIFIED: Lấy desired (ví dụ khi View nằm trong ScrollView cuộn ngang/dọc)
        val measuredW = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredH = resolveSize(desiredHeight, heightMeasureSpec)

        // BẮT BUỘC: Phải gọi setMeasuredDimension để lưu kết quả đo đạc vào hệ thống.
        // Nếu không gọi hàm này, Android sẽ ném IllegalStateException ("did not call setMeasuredDimension") ngay lập tức!
        setMeasuredDimension(measuredW, measuredH)

        // Trích xuất tên của chế độ đo để in log theo dõi
        val wMode = measureModeName(MeasureSpec.getMode(widthMeasureSpec))
        val hMode = measureModeName(MeasureSpec.getMode(heightMeasureSpec))
        logLifecycle("3. onMeasure(): w=${measuredW}px ($wMode), h=${measuredH}px ($hMode)")
    }

    // =========================================================================
    // 4. THAY ĐỔI KÍCH THƯỚC BỐ CỤC (onSizeChanged)
    // • [CÂU #001 & #006]: Nơi chuẩn xác nhất để tính toán các toạ độ RectF và khởi tạo Shader/Gradient.
    //   TUYỆT ĐỐI KHÔNG TẠO MỚI RectF VÀ LinearGradient TRONG onDraw()!
    // =========================================================================
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Gọi hàm của lớp cha
        super.onSizeChanged(w, h, oldw, oldh)

        // Toạ độ lề trái có chừa padding và khoảng cách đệm 12dp
        val contentLeft = paddingLeft + dpToPx(12f)
        // Dành riêng 56dp ở lề phải để vẽ các nhãn giá trên trục Y (Y-Axis)
        val yAxisWidth = dpToPx(56f)
        // Toạ độ mép phải của vùng biểu đồ (đã trừ cột trục Y)
        val contentRight = (w - paddingRight - yAxisWidth).coerceAtLeast(contentLeft + dpToPx(60f))
        // Toạ độ đỉnh của vùng nội dung
        val contentTop = paddingTop + dpToPx(8f)
        // Toạ độ đáy của vùng nội dung
        val contentBottom = (h - paddingBottom - dpToPx(8f)).coerceAtLeast(contentTop + dpToPx(80f))

        // Chiều cao khả dụng tổng thể của View
        val totalHeight = contentBottom - contentTop
        // Chiều cao dành riêng cho vùng Header (tối đa 24% tổng chiều cao)
        val headerHeight = dpToPx(58f).coerceAtMost(totalHeight * 0.24f)
        // Chiều cao dành riêng cho trục nhãn thời gian X ở đáy (tối đa 12% tổng chiều cao)
        val timeAxisHeight = dpToPx(24f).coerceAtMost(totalHeight * 0.12f)
        // Chiều cao còn lại dành cho 2 biểu đồ (Giá + Khối lượng Volume)
        val chartsAvailableHeight = (totalHeight - headerHeight - timeAxisHeight).coerceAtLeast(dpToPx(40f))

        // Phân chia tỷ lệ không gian: 74% chiều cao cho Biểu đồ giá, 26% cho Biểu đồ Volume
        val priceHeight = chartsAvailableHeight * 0.74f
        // Khoảng cách ngăn cách giữa biểu đồ giá và biểu đồ volume (8dp)
        val gapBetweenCharts = dpToPx(8f)

        // Cập nhật toạ độ cho từng hình chữ nhật vùng vẽ:
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

        // [CÂU #006]: Khởi tạo LinearGradient một lần duy nhất tại onSizeChanged() theo chiều cao thực tế
        lineAreaPaint.shader = LinearGradient(
            0f,
            priceChartRect.top,
            0f,
            priceChartRect.bottom,
            Color.argb(110, 34, 211, 238), // Xanh Cyan bán trong suốt ở trên đỉnh
            Color.argb(0, 34, 211, 238),   // Trong suốt hoàn toàn ở đáy
            Shader.TileMode.CLAMP
        )

        // Kiểm tra xem trước khi đổi kích thước, biểu đồ có đang được cuộn ở sát mép phải hay không
        val wasAtEnd = maxScrollOffsetPx == 0f || scrollOffsetPx >= maxScrollOffsetPx - dpToPx(10f)
        // Cập nhật lại giới hạn cuộn
        updateScrollBounds()
        // Nếu là lần đầu tiên mở (oldw == 0) hoặc đang ở mép cuối, tự động cuộn đến cây nến mới nhất
        if (oldw == 0 || wasAtEnd) {
            scrollOffsetPx = maxScrollOffsetPx
        }

        logLifecycle("4. onSizeChanged(): ${oldw}x${oldh} -> ${w}x${h} (Tạo lại RectF & Shader)")
    }

    // =========================================================================
    // 5. BỐ CỤC VỊ TRÍ VIEW CON (onLayout)
    // =========================================================================
    /**
     * Với một View đơn lẻ (không chứa View con như ViewGroup), onLayout chủ yếu dùng để
     * cập nhật các ràng buộc cuối cùng sau khi đã có toạ độ thực tế trên màn hình.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateScrollBounds()
        // Nếu kích thước hoặc toạ độ thực sự thay đổi thì ghi log
        if (changed) {
            logLifecycle("5. onLayout(): changed=true, bounds=[$left,$top - $right,$bottom]")
        }
    }

    // =========================================================================
    // 6. VẼ NỘI DUNG LÊN MÀN HÌNH (onDraw)
    // • [CÂU #006]: CÁC KỸ THUẬT TỐI ƯU ONDRAW ĐỈNH CAO:
    //   1. TUYỆT ĐỐI KHÔNG TẠO NEW OBJECT TRONG ONDRAW (Tránh Garbage Collection giật lag).
    //   2. Viewport Culling: Chỉ tính toán và vẽ những cây nến thực sự nằm trong tầm nhìn màn hình.
    //   3. canvas.clipRect(): Giới hạn khung vẽ biểu đồ để nến không bị tràn sang cột trục Y khi cuộn.
    // =========================================================================
    override fun onDraw(canvas: Canvas) {
        // Luôn gọi hàm của lớp cha
        super.onDraw(canvas)
        // Tăng biến đếm số lần vẽ frame
        drawCount++

        // 1. Vẽ nền tối màu Slate 900 phủ kín toàn bộ diện tích View
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Nếu danh sách rỗng hoặc chiều rộng chưa sẵn sàng thì dừng lại không vẽ tiếp
        if (candles.isEmpty() || priceChartRect.width() <= 0f) return

        // =====================================================================
        // [CÂU #006]: THUẬT TOÁN VIEWPORT CULLING (CHỈ VẼ CÁC CÂY NẾN ĐANG NHÌN THẤY)
        // Kể cả danh sách có 100.000 cây nến thì mỗi frame Canvas chỉ vẽ ~20-30 cây nến -> Đạt mượt mà 120 FPS!
        // =====================================================================
        val step = candleStepPx()
        // Tìm chỉ số cây nến đầu tiên nằm lọt vào mép trái màn hình (trừ hao 1 cây để mượt khi cuộn)
        val firstVisibleIndex = max(0, (scrollOffsetPx / step).toInt() - 1)
        // Tính số lượng cây nến có thể hiển thị vừa trong chiều ngang của khung giá
        val visibleCount = (priceChartRect.width() / step).toInt() + 3
        // Chỉ số cây nến cuối cùng nằm trong tầm nhìn mép phải
        val lastVisibleIndex = min(candles.lastIndex, firstVisibleIndex + visibleCount)

        // Tìm giá cao nhất (Max High) và giá thấp nhất (Min Low) trong tập nến đang nhìn thấy để tự động co giãn trục Y
        var minPrice = Float.MAX_VALUE
        var maxPrice = -Float.MAX_VALUE
        var maxVolume = 1f

        for (i in firstVisibleIndex..lastVisibleIndex) {
            val c = candles[i]
            if (c.low < minPrice) minPrice = c.low
            if (c.high > maxPrice) maxPrice = c.high
            if (c.volume > maxVolume) maxVolume = c.volume
            // Bao gồm cả giá trị đường MA7 để đường vẽ không bị cắt cụt ngoài mép
            ma7Values.getOrNull(i)?.let {
                if (it < minPrice) minPrice = it
                if (it > maxPrice) maxPrice = it
            }
            // Bao gồm cả giá trị đường MA20
            ma20Values.getOrNull(i)?.let {
                if (it < minPrice) minPrice = it
                if (it > maxPrice) maxPrice = it
            }
        }

        // Đảm bảo không bị lỗi chia cho 0 nếu tất cả các giá đều bằng nhau
        if (minPrice >= maxPrice) {
            minPrice -= 1f
            maxPrice += 1f
        }
        // Thêm khoảng đệm 8% trên đỉnh và dưới đáy để biểu đồ thoáng đãng, không bị sát mép viền
        val pricePadding = (maxPrice - minPrice) * 0.08f
        minPrice -= pricePadding
        maxPrice += pricePadding

        // 2. Vẽ các đường lưới ngang dọc (Grid) và nhãn giá/thời gian trên trục toạ độ
        drawGridAndAxes(canvas, minPrice, maxPrice, firstVisibleIndex, lastVisibleIndex)

        // 3. [CÂU #006]: CẮT KHUNG VẼ (CLIPRECT) ĐỂ CHẶN HIỆN TƯỢNG VẼ TRÀN (OVERDRAW BLEEDING)
        // Lưu lại trạng thái Canvas hiện tại vào ngăn xếp Matrix
        canvas.save()
        // Giới hạn khu vực vẽ chỉ được phép nằm lọt bên trong priceChartRect và volumeChartRect
        canvas.clipRect(
            priceChartRect.left,
            priceChartRect.top,
            priceChartRect.right,
            volumeChartRect.bottom
        )

        // Phân nhánh vẽ theo chế độ biểu đồ hiện tại
        if (chartMode == ChartMode.CANDLESTICK) {
            // Vẽ các cây nến Nhật kèm theo cột khối lượng Volume
            drawCandlesticksAndVolume(
                canvas,
                firstVisibleIndex,
                lastVisibleIndex,
                minPrice,
                maxPrice,
                maxVolume
            )
            // Vẽ 2 đường trung bình động MA7 và MA20
            drawMovingAverages(canvas, firstVisibleIndex, lastVisibleIndex, minPrice, maxPrice)
        } else {
            // Vẽ biểu đồ dạng đường chuyển tiếp (Line Area Chart)
            drawLineAreaAndVolume(
                canvas,
                firstVisibleIndex,
                lastVisibleIndex,
                minPrice,
                maxPrice,
                maxVolume
            )
        }

        // Vẽ đường nét đứt biểu thị giá hiện tại (Live Price) kèm chấm tròn nhấp nháy ở cây nến cuối
        drawLivePriceIndicator(canvas, minPrice, maxPrice)

        // Khôi phục lại trạng thái Canvas ban đầu (loại bỏ vùng giới hạn clipRect)
        canvas.restore()

        // 4. Vẽ tia ngắm chữ thập tra cứu thông tin (Crosshair) nếu người dùng đang chạm/nhấn giữ
        if (isCrosshairActive && selectedCandleIndex in candles.indices) {
            drawCrosshair(canvas, selectedCandleIndex, minPrice, maxPrice)
        }

        // 5. Vẽ thông tin Header ở trên cùng (Mã cổ phiếu, mức biến động % và thông số OHLCV)
        drawHeaderInfo(canvas)
    }

    /**
     * Hàm phụ trách vẽ đường lưới (Grid Lines) và các mức giá bên cột Y-Axis
     */
    private fun drawGridAndAxes(
        canvas: Canvas,
        minPrice: Float,
        maxPrice: Float,
        firstIndex: Int,
        lastIndex: Int
    ) {
        // Chia vùng giá thành 4 khoảng lưới ngang đều nhau
        val horizontalLines = 4
        for (i in 0..horizontalLines) {
            val fraction = i.toFloat() / horizontalLines
            // Toạ độ Y của đường lưới ngang
            val y = priceChartRect.top + fraction * priceChartRect.height()
            // Vẽ đường kẻ ngang mờ xuyên qua biểu đồ
            canvas.drawLine(priceChartRect.left, y, priceChartRect.right, y, gridPaint)

            // Tính mức giá tương ứng tại đường kẻ ngang này
            val priceAtLine = maxPrice - fraction * (maxPrice - minPrice)
            val label = formatPrice(priceAtLine)
            // Vẽ nhãn chữ hiển thị giá bên ngoài mép phải (vùng trục Y)
            canvas.drawText(
                label,
                priceChartRect.right + dpToPx(6f),
                y + dpToPx(3.5f), // Căn chỉnh giữa dòng theo trục Y
                axisTextPaint
            )
        }

        // Vẽ đường phân cách ngăn cách giữa biểu đồ giá và biểu đồ Volume
        canvas.drawLine(
            volumeChartRect.left,
            volumeChartRect.top,
            volumeChartRect.right,
            volumeChartRect.top,
            gridPaint
        )
        // Vẽ đường đáy của biểu đồ Volume
        canvas.drawLine(
            volumeChartRect.left,
            volumeChartRect.bottom,
            volumeChartRect.right,
            volumeChartRect.bottom,
            gridPaint
        )
        // Vẽ chữ "VOL" để người dùng nhận diện khu vực khối lượng giao dịch
        canvas.drawText(
            "VOL",
            volumeChartRect.right + dpToPx(6f),
            volumeChartRect.top + dpToPx(12f),
            axisTextPaint
        )

        // Tính khoảng cách nhãn thời gian trục X sao cho không bị đè chữ lên nhau (cách nhau ít nhất 65dp)
        val labelInterval = max(1, (dpToPx(65f) / candleStepPx()).toInt())
        for (i in firstIndex..lastIndex) {
            // Chỉ vẽ nhãn thời gian tại các cây nến chia hết cho bước nhảy labelInterval
            if (i % labelInterval == 0) {
                val cx = candleIndexToCenterX(i)
                // Chỉ vẽ nếu vị trí nến nằm hoàn toàn bên trong khung nhìn
                if (cx in priceChartRect.left..priceChartRect.right) {
                    // Kẻ một đường dọc mảnh mờ
                    canvas.drawLine(cx, priceChartRect.top, cx, volumeChartRect.bottom, gridPaint)
                    val timeText = candles[i].timeLabel
                    // Đo độ dài của chữ để canh giữa ngay tâm cây nến
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

    /**
     * Hàm phụ trách vẽ từng cây nến Nhật (Thân + Râu) và cột khối lượng Volume tương ứng
     */
    private fun drawCandlesticksAndVolume(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float,
        maxVolume: Float
    ) {
        val halfBody = candleWidthPx / 2f // Nửa độ rộng thân nến để vẽ hình chữ nhật đối xứng
        val baselinePrice = (minPrice + maxPrice) / 2f // Mức giá trung tâm dùng làm mốc co giãn khi chạy intro animation

        for (i in firstIndex..lastIndex) {
            val c = candles[i]
            val cx = candleIndexToCenterX(i) // Toạ độ X tâm cây nến
            val paint = if (c.isBullish) bullPaint else bearPaint // Chọn màu cọ xanh (tăng) hoặc đỏ (giảm)

            // Áp dụng introProgress để tạo hiệu ứng nến mọc dần ra từ trung tâm khi mở màn hình
            val animatedHigh = baselinePrice + (c.high - baselinePrice) * introProgress
            val animatedLow = baselinePrice + (c.low - baselinePrice) * introProgress
            val animatedOpen = baselinePrice + (c.open - baselinePrice) * introProgress
            val animatedClose = baselinePrice + (c.close - baselinePrice) * introProgress

            // Chuyển đổi mức giá thành toạ độ pixel Y trên Canvas
            val highY = priceToY(animatedHigh, minPrice, maxPrice)
            val lowY = priceToY(animatedLow, minPrice, maxPrice)
            val openY = priceToY(animatedOpen, minPrice, maxPrice)
            val closeY = priceToY(animatedClose, minPrice, maxPrice)

            // 1. Vẽ râu nến trên và râu nến dưới (High - Low Wick)
            canvas.drawLine(cx, highY, cx, lowY, paint)

            // 2. Vẽ thân nến (Open - Close Body)
            val topBody = min(openY, closeY)
            // Đảm bảo thân nến tối thiểu dày 1.5dp ngay cả khi giá Open == Close (nến Doji)
            val bottomBody = max(openY, closeY).coerceAtLeast(topBody + dpToPx(1.5f))
            canvas.drawRect(cx - halfBody, topBody, cx + halfBody, bottomBody, paint)

            // 3. Vẽ cột khối lượng Volume ở biểu đồ bên dưới
            val volHeight = (c.volume / maxVolume) * volumeChartRect.height() * introProgress
            val volTop = volumeChartRect.bottom - volHeight
            paint.alpha = 140 // Độ trong suốt bán phần cho cột Volume
            canvas.drawRect(cx - halfBody, volTop, cx + halfBody, volumeChartRect.bottom, paint)
            paint.alpha = 255 // Khôi phục lại độ đục 100% cho lượt vẽ nến tiếp theo
        }
    }

    /**
     * Hàm vẽ 2 đường trung bình động MA7 (vàng) và MA20 (xanh dương)
     */
    private fun drawMovingAverages(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float
    ) {
        // [CÂU #006]: Tái sử dụng đối tượng Path bằng cách gọi reset() thay vì new Path()
        ma7Path.reset()
        ma20Path.reset()
        var ma7Started = false
        var ma20Started = false

        for (i in firstIndex..lastIndex) {
            val cx = candleIndexToCenterX(i)
            // Lấy giá trị MA7 của cây nến thứ i (nếu chưa đủ 7 phiên sẽ là null)
            ma7Values.getOrNull(i)?.let { ma ->
                val y = priceToY(ma, minPrice, maxPrice)
                if (!ma7Started) {
                    ma7Path.moveTo(cx, y) // Điểm bắt đầu của đường vẽ
                    ma7Started = true
                } else {
                    ma7Path.lineTo(cx, y) // Nối tiếp đường vẽ đến điểm mới
                }
            }
            // Lấy giá trị MA20 của cây nến thứ i
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

        // Vẽ các đường Path hoàn chỉnh lên Canvas
        if (ma7Started) canvas.drawPath(ma7Path, ma7Paint)
        if (ma20Started) canvas.drawPath(ma20Path, ma20Paint)
    }

    /**
     * Hàm vẽ biểu đồ giá dạng đường (Line Area) kèm dải màu Gradient phía dưới
     */
    private fun drawLineAreaAndVolume(
        canvas: Canvas,
        firstIndex: Int,
        lastIndex: Int,
        minPrice: Float,
        maxPrice: Float,
        maxVolume: Float
    ) {
        // Tái sử dụng đối tượng Path
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

            // Vẽ cột Volume bên dưới
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

        // Khép kín đa giác diện tích bằng cách kéo đường xuống đáy biểu đồ giá
        areaPath.lineTo(lastX, priceChartRect.bottom)
        areaPath.lineTo(firstX, priceChartRect.bottom)
        areaPath.close()

        // Vẽ dải màu chuyển tiếp Gradient
        canvas.drawPath(areaPath, lineAreaPaint)
        // Vẽ đường viền giá sắc nét ở trên cùng
        canvas.drawPath(linePath, linePricePaint)
    }

    /**
     * Hàm vẽ đường giá trực tiếp (Live Price) và chấm sáng nhấp nháy ở cây nến cuối cùng
     */
    private fun drawLivePriceIndicator(canvas: Canvas, minPrice: Float, maxPrice: Float) {
        // Lấy cây nến mới nhất ở cuối danh sách
        val lastCandle = candles.lastOrNull() ?: return
        val y = priceToY(lastCandle.close, minPrice, maxPrice)
        // Nếu mức giá vượt ra ngoài vùng nhìn thấy thì không vẽ
        if (y !in priceChartRect.top..priceChartRect.bottom) return

        // Đổi màu đường giá trực tiếp tuỳ theo nến xanh hay đỏ
        livePriceLinePaint.color =
            if (lastCandle.isBullish) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        // Vẽ đường đứt nét gióng ngang toàn bộ bề ngang biểu đồ
        canvas.drawLine(priceChartRect.left, y, priceChartRect.right, y, livePriceLinePaint)

        // Tính toạ độ X của cây nến cuối cùng
        val lastX = candleIndexToCenterX(candles.lastIndex)
        // Nếu cây nến cuối cùng đang nằm lọt trong màn hình thì vẽ hiệu ứng nhịp đập (Pulse)
        if (lastX in priceChartRect.left..priceChartRect.right) {
            pulsePaint.color = livePriceLinePaint.color
            val maxPulseRadius = dpToPx(11f)
            // Bán kính vòng sóng to dần ra theo tiến trình livePulseFraction
            val currentRadius = dpToPx(3.5f) + maxPulseRadius * livePulseFraction
            // Vòng sóng càng loang rộng thì độ đục alpha càng mờ dần về 0
            pulsePaint.alpha = ((1f - livePulseFraction) * 140).toInt().coerceIn(0, 255)
            // Vẽ vòng sóng nhấp nháy bên ngoài
            canvas.drawCircle(lastX, y, currentRadius, pulsePaint)

            // Vẽ chấm tròn tâm đặc 100% độ đục ở chính giữa
            pulsePaint.alpha = 255
            canvas.drawCircle(lastX, y, dpToPx(3.5f), pulsePaint)
        }
    }

    /**
     * Hàm vẽ tia ngắm chữ thập tra cứu thông tin giá (Crosshair)
     */
    private fun drawCrosshair(canvas: Canvas, index: Int, minPrice: Float, maxPrice: Float) {
        val candle = candles[index]
        val cx = candleIndexToCenterX(index)
        // Nếu nến đang chọn bị cuộn ra ngoài màn hình thì không vẽ
        if (cx !in priceChartRect.left..priceChartRect.right) return

        val cy = priceToY(candle.close, minPrice, maxPrice)

        // 1. Vẽ đường gióng dọc đi xuyên qua cả vùng giá và volume
        canvas.drawLine(cx, priceChartRect.top, cx, volumeChartRect.bottom, crosshairPaint)
        // 2. Vẽ đường gióng ngang mức giá đóng cửa của cây nến
        canvas.drawLine(priceChartRect.left, cy, priceChartRect.right, cy, crosshairPaint)

        // 3. Vẽ nhãn giá nổi bật ở cột Y-Axis mép phải
        val priceLabel = formatPrice(candle.close)
        val badgeH = dpToPx(18f)
        tempRect.set(
            priceChartRect.right + dpToPx(2f),
            cy - badgeH / 2f,
            width - paddingRight - dpToPx(2f),
            cy + badgeH / 2f
        )
        // Vẽ hình chữ nhật bo góc 4dp làm nền nhãn
        canvas.drawRoundRect(tempRect, dpToPx(4f), dpToPx(4f), badgeBgPaint)
        // Vẽ chữ số giá màu trắng vào giữa nhãn
        canvas.drawText(
            priceLabel,
            tempRect.left + dpToPx(4f),
            cy + dpToPx(3.5f),
            badgeTextPaint
        )

        // 4. Vẽ nhãn thời gian nổi bật ở trục X dưới đáy
        val timeText = candle.timeLabel
        val timeW = badgeTextPaint.measureText(timeText) + dpToPx(12f)
        val clampedLeft = (cx - timeW / 2f).coerceIn(priceChartRect.left, priceChartRect.right - timeW)
        tempRect.set(
            clampedLeft,
            timeAxisRect.top + dpToPx(2f),
            clampedLeft + timeW,
            timeAxisRect.bottom
        )
        // Vẽ hình chữ nhật bo góc làm nền nhãn thời gian
        canvas.drawRoundRect(tempRect, dpToPx(4f), dpToPx(4f), badgeBgPaint)
        // Vẽ chuỗi thời gian vào giữa
        canvas.drawText(
            timeText,
            tempRect.left + dpToPx(6f),
            tempRect.centerY() + dpToPx(3.5f),
            badgeTextPaint
        )
    }

    /**
     * Hàm vẽ thông tin tổng quan ở phần Header trên cùng biểu đồ
     */
    private fun drawHeaderInfo(canvas: Canvas) {
        // Nếu đang bật Crosshair thì lấy dữ liệu của cây nến đang tra cứu, ngược lại lấy cây nến mới nhất
        val displayCandle = if (isCrosshairActive && selectedCandleIndex in candles.indices) {
            candles[selectedCandleIndex]
        } else {
            candles.last()
        }
        // Giá đóng cửa của phiên trước đó để tính % tăng giảm
        val prevClose = if (displayCandle.index > 0) {
            candles[displayCandle.index - 1].close
        } else {
            displayCandle.open
        }
        // Chênh lệch giá tuyệt đối
        val diff = displayCandle.close - prevClose
        // Tỷ lệ % thay đổi
        val pct = if (prevClose != 0f) (diff / prevClose) * 100f else 0f
        // Thêm dấu "+" nếu giá tăng
        val sign = if (diff >= 0) "+" else ""

        // Dòng 1: Mã chứng khoán
        val symbolText = "FPT • VNINDEX"
        canvas.drawText(
            symbolText,
            headerRect.left,
            headerRect.top + dpToPx(18f),
            titleTextPaint
        )

        // Dòng 1: Giá khớp lệnh và tỷ lệ % biến động
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

        // Dòng 2: Chi tiết 5 thông số kỹ thuật Open - High - Low - Close - Volume (OHLCV)
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

        // Dòng 3: Giá trị chỉ số trung bình MA7 và MA20
        val maIndex = displayCandle.index
        val ma7Str = ma7Values.getOrNull(maIndex)?.let { formatPrice(it) } ?: "--"
        val ma20Str = ma20Values.getOrNull(maIndex)?.let { formatPrice(it) } ?: "--"

        // Vẽ chú thích MA7 (màu vàng)
        ma7Paint.style = Paint.Style.FILL
        ma7Paint.textSize = spToPx(10.5f)
        val ma7Label = "MA7: $ma7Str"
        canvas.drawText(ma7Label, headerRect.left, headerRect.top + dpToPx(53f), ma7Paint)
        ma7Paint.style = Paint.Style.STROKE // Trả lại style STROKE cho lần vẽ đường sau

        // Vẽ chú thích MA20 (màu xanh dương)
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
        // [CÂU #007 - BẪY PHỎNG VẤN TOUCH DISPATCH]:
        // Khi người dùng bắt đầu chạm ngón tay (ACTION_DOWN), ta gửi lệnh yêu cầu ViewGroup cha (như ScrollView/ViewPager):
        // "Tôi đang xử lý biểu đồ, Cha TUYỆT ĐỐI KHÔNG ĐƯỢC chặn cướp quyền chạm của tôi!"
        // Bằng cách gọi: parent?.requestDisallowInterceptTouchEvent(true)
        // Khi người dùng thả tay (ACTION_UP) hoặc cử chỉ bị hệ thống huỷ (ACTION_CANCEL):
        // Phải nhả cờ lại cho cha bằng: parent?.requestDisallowInterceptTouchEvent(false)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        // Chuyển sự kiện cho ScaleGestureDetector xử lý thu phóng 2 ngón
        val scaleHandled = scaleDetector.onTouchEvent(event)
        // Chuyển sự kiện cho GestureDetector xử lý chạm, cuộn 1 ngón
        val gestureHandled = gestureDetector.onTouchEvent(event)

        // Nếu tia ngắm Crosshair đang bật và ngón tay đang trượt (ACTION_MOVE)
        if (isCrosshairActive && event.actionMasked == MotionEvent.ACTION_MOVE && !scaleDetector.isInProgress) {
            selectedCandleIndex = xToCandleIndex(event.x)
            invalidate()
        }

        // Trả về true nếu một trong các bộ xử lý cử chỉ đã tiêu thụ sự kiện
        return scaleHandled || gestureHandled || super.onTouchEvent(event)
    }

    /**
     * [CÂU #001]: computeScroll() được hệ thống gọi tự động trong phương thức draw() của View
     * mỗi khi OverScroller đang tính toán toạ độ quán tính (Fling) sau cú vuốt nhanh của người dùng.
     */
    override fun computeScroll() {
        super.computeScroll()
        // Kiểm tra xem hiệu ứng cuộn quán tính có còn đang tiếp tục hay không
        if (scroller.computeScrollOffset()) {
            // Lấy toạ độ X hiện tại do Scroller tính toán và gán vào scrollOffsetPx
            scrollOffsetPx = scroller.currX.toFloat().coerceIn(0f, maxScrollOffsetPx)
            // Yêu cầu vẽ lại frame tiếp theo đồng bộ theo tần số quét màn hình (60Hz / 120Hz)
            postInvalidateOnAnimation()
        }
    }

    // =========================================================================
    // 8. VÒNG ĐỜI HIỂN THỊ & FOCUS (onVisibilityChanged & onWindowFocusChanged)
    // =========================================================================
    /**
     * Được gọi khi bản thân View hoặc một ViewGroup tổ tiên đổi trạng thái VISIBLE / INVISIBLE / GONE.
     * RẤT QUAN TRỌNG: Phải tạm dừng ValueAnimator khi View bị ẩn để tiết kiệm pin và tránh hao tốn CPU ngầm!
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

        // Nếu hiển thị thì tiếp tục chạy hoạt ảnh, nếu bị ẩn thì tạm dừng
        if (visibility == VISIBLE) {
            pulseAnimator?.resume()
        } else {
            pulseAnimator?.pause()
        }
    }

    /**
     * Được gọi khi cửa sổ chứa View nhận hoặc mất Focus (ví dụ có Dialog/Bottom Sheet mở lên hoặc người dùng bấm nút Home).
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        logLifecycle("9. onWindowFocusChanged(): hasFocus=$hasWindowFocus")
        // Nếu cửa sổ lấy lại focus và View đang hiển thị thì resume animator
        if (hasWindowFocus && visibility == VISIBLE) {
            pulseAnimator?.resume()
        } else {
            pulseAnimator?.pause()
        }
    }

    // =========================================================================
    // 9. LƯU & KHÔI PHỤC TRẠNG THÁI (onSaveInstanceState / onRestoreInstanceState)
    // • [CÂU #009]: View tự bảo lưu State qua BaseSavedState khi Configuration Change (xoay màn hình)
    // =========================================================================
    /**
     * [CÂU #009]: Lưu trạng thái cuộn, mức độ zoom, nến đang chọn khi xoay màn hình.
     * Để hoạt động, bắt buộc View phải có id cụ thể và isSaveEnabled = true (đã set trong init).
     */
    override fun onSaveInstanceState(): Parcelable {
        // Lấy trạng thái của lớp cha
        val superState = super.onSaveInstanceState()
        logLifecycle("10. onSaveInstanceState(): [CÂU #009] Lưu vị trí cuộn, mức zoom & chế độ chart")
        // Tạo đối tượng SavedState đóng gói các dữ liệu quan trọng của CustomView
        return SavedState(superState).apply {
            savedCandleWidth = this@CustomView.candleWidthPx
            savedScrollOffset = this@CustomView.scrollOffsetPx
            savedSelectedIndex = this@CustomView.selectedCandleIndex
            savedCrosshairActive = this@CustomView.isCrosshairActive
            savedChartModeOrdinal = this@CustomView.chartMode.ordinal
        }
    }

    /**
     * Khôi phục lại trạng thái đã lưu sau khi Activity và View được tái tạo lại sau khi xoay màn hình.
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        // Kiểm tra đúng kiểu SavedState của View
        if (state is SavedState) {
            // Khôi phục trạng thái lớp cha
            super.onRestoreInstanceState(state.superState)
            // Khôi phục mức zoom (độ rộng nến)
            candleWidthPx = state.savedCandleWidth
            candleSpacingPx = (candleWidthPx * 0.28f).coerceAtLeast(dpToPx(2f))
            // Khôi phục vị trí cuộn
            scrollOffsetPx = state.savedScrollOffset
            // Khôi phục nến đang chọn
            selectedCandleIndex = state.savedSelectedIndex
            // Khôi phục trạng thái tia ngắm Crosshair
            isCrosshairActive = state.savedCrosshairActive
            // Khôi phục chế độ biểu đồ (Nến hay Đường)
            chartMode = ChartMode.entries.getOrElse(state.savedChartModeOrdinal) {
                ChartMode.CANDLESTICK
            }
            logLifecycle("11. onRestoreInstanceState(): [CÂU #009] Khôi phục thành công trạng thái chart")
            // Yêu cầu vẽ lại giao diện hoàn chỉnh theo trạng thái vừa khôi phục
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    // =========================================================================
    // 10. GỠ KHỎI CỬA SỔ & DỌN DẸP BỘ NHỚ (onDetachedFromWindow)
    // • [CÂU #001]: BẮT BUỘC dừng mọi Animator, Handler, Listener để CHỐNG RÒ RỈ BỘ NHỚ (MEMORY LEAK)!
    // =========================================================================
    override fun onDetachedFromWindow() {
        logLifecycle("12. onDetachedFromWindow(): Huỷ Animator & Scroller để chống Memory Leak")
        // Huỷ và giải phóng tham chiếu của ValueAnimator mở màn
        introAnimator?.cancel()
        introAnimator = null
        // Huỷ và giải phóng tham chiếu của ValueAnimator nhịp tim
        pulseAnimator?.cancel()
        pulseAnimator = null
        // Dừng chuyển động quán tính nếu đang cuộn dở
        scroller.abortAnimation()
        // Gọi hàm của lớp cha
        super.onDetachedFromWindow()
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC ĐIỀU KHIỂN CÔNG KHAI (PUBLIC API CHO MÀN HÌNH BÊN NGOÀI GỌI)
    // =========================================================================

    /**
     * Mô phỏng một phiên giao dịch trực tiếp: Thêm 1 cây nến mới vào cuối danh sách.
     * [CÂU #003]: Chỉ gọi invalidate() vì cấu trúc kích thước View không hề đổi -> Chỉ kích hoạt onDraw(),
     * KHÔNG gọi onMeasure() hay onLayout().
     */
    fun appendLiveCandle() {
        val last = candles.lastOrNull() ?: return
        val nextIndex = last.index + 1
        // Tạo nhãn thời gian tiếp theo theo bước nhảy 5 phút
        val hour = 9 + (nextIndex / 12) % 6
        val minute = (nextIndex % 12) * 5
        val label = String.format("%02d:%02d", hour, minute)

        // Tính toán biến động giá ngẫu nhiên
        val delta = (Random.nextFloat() - 0.48f) * 3.2f
        val open = last.close
        val close = (open + delta).coerceAtLeast(15f)
        val high = max(open, close) + Random.nextFloat() * 1.8f
        val low = min(open, close) - Random.nextFloat() * 1.8f
        val volume = 120f + Random.nextFloat() * 380f

        // Kiểm tra xem trước khi thêm nến thì người dùng có đang đứng ở mép phải cuối cùng hay không
        val wasAtRightEdge = scrollOffsetPx >= maxScrollOffsetPx - candleStepPx()
        // Thêm cây nến mới vào danh sách
        candles.add(Candle(nextIndex, label, open, high, low, close, volume))
        // Tính toán lại giá trị đường trung bình MA7 và MA20
        recalculateMovingAverages()
        // Cập nhật lại giới hạn cuộn
        updateScrollBounds()

        // Nếu đang ở mép cuối, tự động đẩy cuộn sang phải để bám sát cây nến mới nhất vừa xuất hiện
        if (wasAtRightEdge) {
            scrollOffsetPx = maxScrollOffsetPx
        }
        logLifecycle("appendLiveCandle() -> invalidate() (Chỉ gọi onDraw, không đo lại)")
        // Kích hoạt vẽ lại màn hình
        invalidate()
    }

    /**
     * Chạy lại hiệu ứng hoạt ảnh mọc nến mở màn (dùng để test từ giao diện Compose)
     */
    fun replayIntroAnimation() {
        logLifecycle("replayIntroAnimation() -> Chạy lại ValueAnimator")
        startIntroAnimation()
    }

    /**
     * Khởi chạy ValueAnimator mọc nến trong thời gian 650 mili-giây
     */
    private fun startIntroAnimation() {
        introAnimator?.cancel()
        // Biến đổi giá trị từ 0.0f đến 1.0f
        introAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650L // Thời gian chạy 650ms
            interpolator = DecelerateInterpolator() // Giảm tốc dần về cuối
            // Lắng nghe từng frame cập nhật để vẽ lại
            addUpdateListener { anim ->
                introProgress = anim.animatedValue as Float
                // [CÂU #003]: Mỗi frame animator cập nhật, ta gọi invalidate() để vẽ lại Canvas
                invalidate()
            }
            start()
        }
    }

    /**
     * Khởi chạy ValueAnimator nhấp nháy chấm sáng giá trực tiếp (chạy lặp vô tận)
     */
    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400L // Một chu kỳ nhịp tim kéo dài 1.4 giây
            repeatCount = ValueAnimator.INFINITE // Chạy lặp vô tận
            repeatMode = ValueAnimator.RESTART // Bắt đầu lại từ đầu sau mỗi chu kỳ
            addUpdateListener { anim ->
                livePulseFraction = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TÍNH TOÁN HÌNH HỌC VÀ TOẠ ĐỘ
    // =========================================================================

    /**
     * Tạo dữ liệu giả lập ban đầu cho 80 cây nến theo thuật toán bước đi ngẫu nhiên (Random Walk)
     */
    private fun generateInitialStockData(count: Int) {
        candles.clear()
        val random = Random(42) // Dùng seed cố định để dữ liệu luôn nhất quán
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
        // Tính toán các đường trung bình động MA
        recalculateMovingAverages()
    }

    /**
     * Thuật toán tính toán đường trung bình trượt MA7 và MA20 bằng kỹ thuật Sliding Window O(N)
     */
    private fun recalculateMovingAverages() {
        ma7Values.clear()
        ma20Values.clear()
        var sum7 = 0f
        var sum20 = 0f

        for (i in candles.indices) {
            val close = candles[i].close
            sum7 += close
            sum20 += close

            // Trừ bớt phần tử rơi ra khỏi cửa sổ trượt
            if (i >= 7) sum7 -= candles[i - 7].close
            if (i >= 20) sum20 -= candles[i - 20].close

            // Đủ 7 phần tử thì lưu giá trị trung bình MA7, ngược lại lưu null
            ma7Values.add(if (i >= 6) sum7 / 7f else null)
            // Đủ 20 phần tử thì lưu giá trị trung bình MA20, ngược lại lưu null
            ma20Values.add(if (i >= 19) sum20 / 20f else null)
        }
    }

    /**
     * Bước nhảy toạ độ của mỗi cây nến = Độ rộng nến + Khoảng cách giữa 2 nến
     */
    private fun candleStepPx(): Float = candleWidthPx + candleSpacingPx

    /**
     * Cập nhật giới hạn cuộn tối đa dựa trên tổng số lượng cây nến và chiều rộng khung vẽ
     */
    private fun updateScrollBounds() {
        if (priceChartRect.width() <= 0f) return
        // Tổng chiều rộng của toàn bộ dữ liệu nến
        val totalContentWidth = candles.size * candleStepPx() + dpToPx(24f)
        // Khoảng cách cuộn tối đa = Tổng chiều rộng nội dung - Chiều rộng khung nhìn màn hình
        maxScrollOffsetPx = max(0f, totalContentWidth - priceChartRect.width())
        // Giới hạn khoảng cách cuộn hiện tại nằm trong khoảng [0, maxScrollOffsetPx]
        scrollOffsetPx = scrollOffsetPx.coerceIn(0f, maxScrollOffsetPx)
    }

    /**
     * Chuyển đổi số thứ tự (index) của cây nến thành toạ độ X trung tâm trên Canvas
     */
    private fun candleIndexToCenterX(index: Int): Float {
        return priceChartRect.left + index * candleStepPx() + candleStepPx() / 2f - scrollOffsetPx
    }

    /**
     * Chuyển đổi toạ độ X người dùng chạm trên màn hình thành chỉ số index của cây nến tương ứng
     */
    private fun xToCandleIndex(x: Float): Int {
        val rawIndex = ((x - priceChartRect.left + scrollOffsetPx) / candleStepPx()).toInt()
        return rawIndex.coerceIn(0, candles.lastIndex)
    }

    /**
     * Ánh xạ mức giá (Price) sang toạ độ Y pixel trên Canvas:
     * Lưu ý trong hệ toạ độ Canvas Android, toạ độ Y = 0 nằm ở ĐỈNH trên cùng,
     * nên giá cao nhất sẽ có toạ độ Y nhỏ nhất (gần đỉnh), giá thấp nhất có toạ độ Y lớn nhất (ở đáy).
     */
    private fun priceToY(price: Float, minPrice: Float, maxPrice: Float): Float {
        val ratio = (price - minPrice) / (maxPrice - minPrice)
        return priceChartRect.bottom - ratio * priceChartRect.height()
    }

    /**
     * Định dạng số thực hiển thị mức giá với 2 chữ số thập phân
     */
    private fun formatPrice(price: Float): String = String.format("%.2f", price)

    /**
     * Chuyển đổi mã chế độ đo MeasureSpec thành chuỗi văn bản dễ đọc
     */
    private fun measureModeName(mode: Int): String = when (mode) {
        MeasureSpec.EXACTLY -> "EXACTLY"
        MeasureSpec.AT_MOST -> "AT_MOST"
        MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        else -> "UNKNOWN"
    }

    /**
     * Gửi thông điệp vòng đời ra ngoài qua callback onLifecycleEvent
     */
    private fun logLifecycle(message: String) {
        onLifecycleEvent?.invoke(message)
    }

    // =========================================================================
    // [CÂU #010]: CHUYỂN ĐỔI DP, SP SANG PX TRÊN ANDROID 14+
    // - Luôn dùng TypedValue.applyDimension với COMPLEX_UNIT_DIP và COMPLEX_UNIT_SP
    // - BẪY PHỎNG VẤN KINH ĐIỂN: Từ Android 14 (API 34), Google áp dụng Non-linear font scaling
    //   (cỡ chữ càng to thì khi phóng đại sẽ tăng chậm hơn để không vỡ giao diện).
    //   Vì vậy TUYỆT ĐỐI KHÔNG ĐƯỢC tự tính nhẩm theo kiểu `sp * displayMetrics.density`,
    //   mà bắt buộc phải dùng TypedValue.applyDimension() hoặc TypedArray.getDimension()!
    // =========================================================================
    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    // =========================================================================
    // [CÂU #009]: LỚP LƯU TRỮ TRẠNG THÁI TÙY BIẾN (KẾ THỪA TỪ BaseSavedState)
    // =========================================================================
    /**
     * Lớp SavedState kế thừa từ View.BaseSavedState:
     * Chịu trách nhiệm ghi (writeToParcel) và đọc (read) trạng thái vào gói Parcel khi Activity bị tiêu huỷ do xoay màn hình.
     */
    private class SavedState : BaseSavedState {
        // Biến lưu độ rộng nến (mức Zoom)
        var savedCandleWidth: Float = 0f
        // Biến lưu khoảng cách cuộn
        var savedScrollOffset: Float = 0f
        // Biến lưu nến đang chọn
        var savedSelectedIndex: Int = -1
        // Biến lưu trạng thái bật/tắt Crosshair
        var savedCrosshairActive: Boolean = false
        // Biến lưu số thứ tự enum của chế độ biểu đồ
        var savedChartModeOrdinal: Int = 0

        // Constructor dùng khi lưu trạng thái từ superState
        constructor(superState: Parcelable?) : super(superState)

        // Constructor private dùng để giải mã đọc dữ liệu từ Parcel nhị phân theo ĐÚNG THỨ TỰ đã ghi
        private constructor(parcel: Parcel) : super(parcel) {
            savedCandleWidth = parcel.readFloat()
            savedScrollOffset = parcel.readFloat()
            savedSelectedIndex = parcel.readInt()
            savedCrosshairActive = parcel.readInt() == 1
            savedChartModeOrdinal = parcel.readInt()
        }

        /**
         * Ghi dữ liệu vào Parcel nhị phân khi xoay màn hình
         */
        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(savedCandleWidth)
            out.writeFloat(savedScrollOffset)
            out.writeInt(savedSelectedIndex)
            out.writeInt(if (savedCrosshairActive) 1 else 0)
            out.writeInt(savedChartModeOrdinal)
        }

        // Đối tượng CREATOR bắt buộc của giao diện Parcelable để hệ thống Android tái tạo SavedState từ Parcel
        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
