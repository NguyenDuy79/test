package com.example.fitness.myapplication

// Import lớp Bundle để xử lý trạng thái lưu trữ khi Activity khởi tạo hoặc tái tạo
import android.os.Bundle
// Import lớp View của hệ thống Android để quản lý trạng thái hiển thị (VISIBLE/INVISIBLE/GONE)
import android.view.View
// Import ViewGroup để thiết lập LayoutParams cho View truyền thống
import android.view.ViewGroup
// Import ComponentActivity - lớp cơ sở hiện đại của AndroidX hỗ trợ Jetpack Compose
import androidx.activity.ComponentActivity
// Import setContent để gán giao diện Jetpack Compose vào Activity
import androidx.activity.compose.setContent
// Import hàm kích hoạt chế độ hiển thị tràn viền (Edge-to-Edge) từ Android 15+
import androidx.activity.enableEdgeToEdge
// Import các tiện ích nền, thanh cuộn và layout của Jetpack Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
// Import các thành phần Material Design 3
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
// Import các hàm quản lý State và Vòng đời trong Jetpack Compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import AndroidView - Cầu nối cốt lõi để nhúng Custom View truyền thống vào giao diện Compose
import androidx.compose.ui.viewinterop.AndroidView
// Import các lớp Model và Repository dữ liệu câu hỏi phỏng vấn
import com.example.fitness.myapplication.data.LabType
import com.example.fitness.myapplication.data.QuestionRepository
import com.example.fitness.myapplication.ui.QuestionBrowserScreen
// Import các phòng thí nghiệm trực quan hoá tính năng
import com.example.fitness.myapplication.ui.labs.FragmentNavLab
import com.example.fitness.myapplication.ui.labs.MeasureSpecLab
import com.example.fitness.myapplication.ui.labs.RecyclerViewPayloadLab
import com.example.fitness.myapplication.ui.labs.StockChartCompose
import com.example.fitness.myapplication.ui.labs.TouchDispatchLab
import com.example.fitness.myapplication.ui.labs.rememberStockChartComposeState
import com.example.fitness.myapplication.ui.theme.MyApplicationTheme

/**
 * Định nghĩa danh sách các Tab chính trong ứng dụng:
 * Mỗi tab tương ứng với một chủ đề lớn thường gặp trong phỏng vấn Android Senior:
 * - QUESTIONS: Trình duyệt tra cứu 50 câu hỏi lý thuyết kèm code & mẹo phỏng vấn.
 * - STOCK_CHART: Phòng thí nghiệm Custom View biểu đồ nến chứng khoán (Vòng đời & onDraw).
 * - MEASURE_SPEC: Phòng thí nghiệm đo đạc kích thước ViewGroup & View con.
 * - TOUCH_DISPATCH: Phòng thí nghiệm luồng phân phối sự kiện cảm ứng (Touch Event Flow).
 * - RECYCLER_VIEW: Phòng thí nghiệm tối ưu DiffUtil và cơ chế Payload Partial Bind.
 * - FRAGMENT_NAV: Phòng thí nghiệm vòng đời Fragment, ViewBinding leak & Predictive Back.
 */
enum class MainAppTab(val title: String) {
    QUESTIONS("📚 50 Câu Hỏi"),
    STOCK_CHART("📈 Biểu Đồ Stock"),
    MEASURE_SPEC("📏 MeasureSpec"),
    TOUCH_DISPATCH("👆 Touch Flow"),
    RECYCLER_VIEW("⚡ RecyclerView"),
    FRAGMENT_NAV("🧩 Fragment & Back")
}

/**
 * Activity chính của ứng dụng: Điểm nhập (Entry Point) của toàn bộ ứng dụng Android.
 */
class MainActivity : ComponentActivity() {

    /**
     * Callback vòng đời onCreate: Khởi tạo giao diện khi Activity được tạo lần đầu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi hàm của lớp cha để thực hiện các khởi tạo hệ thống cần thiết
        super.onCreate(savedInstanceState)

        // Kích hoạt giao diện hiển thị tràn viền (Edge-to-Edge) từ cạnh trên xuống cạnh dưới màn hình
        enableEdgeToEdge()

        // Thiết lập cây giao diện Compose thông qua hàm setContent
        setContent {
            // Áp dụng chủ đề thiết kế thống nhất của ứng dụng
            MyApplicationTheme {
                // Scaffold cung cấp cấu trúc layout chuẩn (tự động tính innerPadding để tránh che lấp bởi System Bar)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Khởi chạy thành phần gốc của ứng dụng và truyền padding hệ thống
                    MasteryAppRoot(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Màn hình gốc điều phối toàn bộ điều hướng và nội dung các Tab của ứng dụng.
 */
@Composable
fun MasteryAppRoot(modifier: Modifier = Modifier) {
    // Lấy Context hiện tại của ứng dụng từ môi trường Compose
    val context = LocalContext.current

    // Tải và ghi nhớ (cache) danh sách 50 câu hỏi từ tệp questions_50.json trong thư mục assets
    val allQuestions = remember { QuestionRepository.getQuestions(context) }

    // Trạng thái Tab hiện tại đang được người dùng lựa chọn (mặc định mở tab 50 Câu Hỏi)
    var currentTab by remember { mutableStateOf(MainAppTab.QUESTIONS) }

    // Bố cục cột chứa thanh điều hướng Tab ở trên và nội dung tương ứng ở dưới
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120)) // Màu nền tổng thể tối sang trọng
    ) {
        // =========================================================================
        // THANH TAB NAVIGATION Ở ĐẦU MÀN HÌNH (CHO PHÉP CUỘN NGANG)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .horizontalScroll(rememberScrollState()) // Cho phép cuộn ngang danh sách tab mượt mà
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các nút tab là 8dp
        ) {
            // Duyệt qua từng Tab để tạo nút FilterChip chọn lựa
            MainAppTab.entries.forEach { tab ->
                FilterChip(
                    selected = currentTab == tab, // Đánh dấu nếu đây là tab đang chọn
                    onClick = { currentTab = tab }, // Chuyển sang tab mới khi người dùng chạm
                    label = {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7), // Màu nền khi được chọn (xanh lam)
                        selectedLabelColor = Color.White, // Màu chữ khi được chọn (trắng)
                        containerColor = Color(0xFF1E293B), // Màu nền khi chưa chọn (xám tối)
                        labelColor = Color(0xFFCBD5E1) // Màu chữ khi chưa chọn
                    )
                )
            }
        }

        // =========================================================================
        // VÙNG NỘI DUNG CHÍNH (THAY ĐỔI THEO TAB ĐANG CHỌN)
        // =========================================================================
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                // Tab 1: Trình duyệt 50 câu hỏi phỏng vấn
                MainAppTab.QUESTIONS -> {
                    QuestionBrowserScreen(
                        questions = allQuestions,
                        // Callback khi người dùng bấm vào nút "Mở Phòng Thí Nghiệm" trong từng câu hỏi
                        onOpenLab = { lab ->
                            currentTab = when (lab) {
                                LabType.LIFECYCLE -> MainAppTab.STOCK_CHART
                                LabType.MEASURE_SPEC -> MainAppTab.MEASURE_SPEC
                                LabType.TOUCH -> MainAppTab.TOUCH_DISPATCH
                                LabType.RECYCLER_VIEW -> MainAppTab.RECYCLER_VIEW
                                LabType.FRAGMENT_NAV -> MainAppTab.FRAGMENT_NAV
                            }
                        }
                    )
                }
                // Tab 2: Phòng thí nghiệm Custom View biểu đồ Stock
                MainAppTab.STOCK_CHART -> {
                    StockChartLifecycleLabScreen(modifier = Modifier.fillMaxSize())
                }
                // Tab 3: Phòng thí nghiệm MeasureSpec và kích thước View
                MainAppTab.MEASURE_SPEC -> {
                    MeasureSpecLab()
                }
                // Tab 4: Phòng thí nghiệm luồng phân phối Touch Event
                MainAppTab.TOUCH_DISPATCH -> {
                    TouchDispatchLab()
                }
                // Tab 5: Phòng thí nghiệm RecyclerView & DiffUtil Payload
                MainAppTab.RECYCLER_VIEW -> {
                    RecyclerViewPayloadLab()
                }
                // Tab 6: Phòng thí nghiệm Fragment Lifecycle & Predictive Back
                MainAppTab.FRAGMENT_NAV -> {
                    FragmentNavLab()
                }
            }
        }
    }
}

/**
 * Enum định nghĩa 2 phương thức dựng giao diện Custom View / Custom Chart:
 * - XML_CUSTOM_VIEW: Sử dụng View truyền thống kế thừa android.view.View và vẽ bằng Canvas 2D
 * - JETPACK_COMPOSE: Sử dụng Jetpack Compose Canvas thuần
 */
enum class RenderEngine(val title: String) {
    XML_CUSTOM_VIEW("🎨 XML (CustomView)"),
    JETPACK_COMPOSE("⚡ Jetpack Compose (Canvas)")
}

/**
 * Màn hình thí nghiệm Vòng đời Custom View (StockChartLifecycleLabScreen):
 * Minh họa sự tương tác trực tiếp và so sánh giữa Jetpack Compose và Android Custom View truyền thống.
 */
@Composable
fun StockChartLifecycleLabScreen(modifier: Modifier = Modifier) {
    // Chế độ render hiện tại (XML CustomView hoặc Jetpack Compose)
    var renderEngine by remember { mutableStateOf(RenderEngine.XML_CUSTOM_VIEW) }

    // Danh sách nhật ký vòng đời (LIFO - sự kiện mới nhất nằm trên cùng)
    val lifecycleLogs = remember {
        mutableStateListOf(
            "👉 Vuốt ngang để cuộn (có quán tính Fling), dùng 2 ngón để Zoom, chạm/nhấn giữ để bật tia ngắm Crosshair."
        )
    }

    // Biến lưu tham chiếu trực tiếp đến đối tượng CustomView truyền thống
    var customViewRef by remember { mutableStateOf<CustomView?>(null) }

    // Controller lưu trạng thái của Stock Chart phiên bản Jetpack Compose
    val composeChartState = rememberStockChartComposeState()

    // Trạng thái gắn vào Window/Composition: true = hiển thị, false = gỡ bỏ
    var isAttached by remember { mutableStateOf(true) }

    // Trạng thái kích thước chiều cao: false = 370dp, true = 280dp (dùng để test requestLayout())
    var isCompactHeight by remember { mutableStateOf(false) }

    // Trạng thái ẩn/hiện: true = VISIBLE, false = INVISIBLE
    var isViewVisible by remember { mutableStateOf(true) }

    // Chế độ hiển thị biểu đồ: Nến (CANDLESTICK) hoặc Đường (LINE_AREA)
    var chartMode by remember { mutableStateOf(CustomView.ChartMode.CANDLESTICK) }

    // Chụp nhanh số lần hàm onDraw() đã chạy để theo dõi hiệu năng
    var drawCounterSnapshot by remember { mutableIntStateOf(0) }

    // Hàm tiện ích đẩy log mới vào đầu danh sách và giới hạn tối đa 40 dòng
    fun pushLog(event: String) {
        lifecycleLogs.add(0, event)
        if (lifecycleLogs.size > 40) {
            lifecycleLogs.removeAt(lifecycleLogs.lastIndex)
        }
    }

    // Lắng nghe callback sự kiện từ Compose Chart
    composeChartState.onLifecycleEvent = { event ->
        pushLog(event)
    }

    // Bố cục cột chứa Custom View, các nút bấm thao tác và bảng log console
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // =========================================================================
        // THANH NÚT CHUYỂN ĐỔI GIỮA XML CUSTOMVIEW VÀ JETPACK COMPOSE
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Công nghệ Render:",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            RenderEngine.entries.forEach { engine ->
                FilterChip(
                    selected = renderEngine == engine,
                    onClick = {
                        renderEngine = engine
                        pushLog("🔄 Đã chuyển sang chế độ: ${engine.title}")
                    },
                    label = {
                        Text(
                            text = engine.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (engine == RenderEngine.JETPACK_COMPOSE) Color(0xFF0284C7) else Color(0xFF7C3AED),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        // =========================================================================
        // 1. VÙNG CHỨA BIỂU ĐỒ CHỨNG KHOÁN (HIỂN THỊ THEO RENDER ENGINE ĐÃ CHỌN)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Thay đổi chiều cao linh hoạt để kích hoạt onMeasure() và onSizeChanged()
                .height(if (isCompactHeight) 280.dp else 370.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isAttached && isViewVisible) {
                if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                    // Thành phần AndroidView cho phép nhúng View truyền thống vào Compose:
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        // factory: Hàm tạo View một lần duy nhất khi thành phần xuất hiện lần đầu
                        factory = { context ->
                            CustomView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                onLifecycleEvent = { event ->
                                    pushLog(event)
                                    drawCounterSnapshot = this.drawCount
                                }
                            }.also { createdView ->
                                customViewRef = createdView
                            }
                        },
                        // update: Được Compose gọi mỗi khi State đầu vào thay đổi
                        update = { view ->
                            view.visibility = View.VISIBLE
                            view.chartMode = chartMode
                            drawCounterSnapshot = view.drawCount
                        },
                        // onRelease: Được gọi khi AndroidView bị gỡ khỏi cấu trúc Compose để dọn dẹp
                        onRelease = { _ ->
                            customViewRef = null
                        }
                    )
                } else {
                    // Jetpack Compose Custom Canvas Implementation:
                    StockChartCompose(
                        state = composeChartState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (!isAttached) {
                Text(
                    text = if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        "View đã bị gỡ khỏi Window (onDetachedFromWindow)"
                    } else {
                        "Compose Component đã bị gỡ khỏi Composition (Disposed)"
                    },
                    color = Color(0xFFF87171),
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "View đang ẩn (INVISIBLE / Hidden)",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        }

        // =========================================================================
        // 2. BẢNG CÁC NÚT BẤM THỰC HÀNH CÁC NHÁNH VÒNG ĐỜI VIEW & STATE COMPOSE
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Cho phép cuộn ngang thanh nút bấm
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút 1: Thêm nến mới
                LifecycleActionButton(
                    label = "+ Nến mới",
                    color = Color(0xFF10B981)
                ) {
                    if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        customViewRef?.appendLiveCandle()
                        drawCounterSnapshot = customViewRef?.drawCount ?: 0
                    } else {
                        composeChartState.appendLiveCandle()
                        drawCounterSnapshot = composeChartState.drawCount
                    }
                }

                // Nút 2: Đổi chế độ Nến / Đường
                LifecycleActionButton(
                    label = if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        if (chartMode == CustomView.ChartMode.CANDLESTICK) "Đổi sang Line" else "Đổi sang Nến"
                    } else {
                        if (composeChartState.chartMode == CustomView.ChartMode.CANDLESTICK) "Đổi sang Line" else "Đổi sang Nến"
                    },
                    color = Color(0xFF0284C7)
                ) {
                    if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        chartMode = if (chartMode == CustomView.ChartMode.CANDLESTICK) {
                            CustomView.ChartMode.LINE_AREA
                        } else {
                            CustomView.ChartMode.CANDLESTICK
                        }
                    } else {
                        composeChartState.chartMode = if (composeChartState.chartMode == CustomView.ChartMode.CANDLESTICK) {
                            CustomView.ChartMode.LINE_AREA
                        } else {
                            CustomView.ChartMode.CANDLESTICK
                        }
                        pushLog("⚡ [Compose] Đổi chartMode sang ${composeChartState.chartMode}")
                    }
                }

                // Nút 3: Đổi kích thước -> Test layout measurement / recomposition
                LifecycleActionButton(
                    label = "Đổi kích thước (Re-Layout)",
                    color = Color(0xFFD97706)
                ) {
                    pushLog("⚡ Kích hoạt đổi kích thước -> ${if (!isCompactHeight) "280dp" else "370dp"}")
                    isCompactHeight = !isCompactHeight
                    if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        customViewRef?.requestLayout()
                    }
                }

                // Nút 4: Ẩn/Hiện View
                LifecycleActionButton(
                    label = if (isViewVisible) "Ẩn View" else "Hiện View",
                    color = Color(0xFF7C3AED)
                ) {
                    isViewVisible = !isViewVisible
                    pushLog("⚡ Kích hoạt isViewVisible = $isViewVisible")
                }

                // Nút 5: Gỡ/Gắn View khỏi Window / Composition
                LifecycleActionButton(
                    label = if (isAttached) "Gỡ View (Detach)" else "Gắn lại (Attach)",
                    color = Color(0xFFE11D48)
                ) {
                    isAttached = !isAttached
                    pushLog("⚡ Kích hoạt isAttached = $isAttached")
                }

                // Nút 6: Chạy lại hiệu ứng mở màn
                LifecycleActionButton(
                    label = "Chạy lại Intro Animator",
                    color = Color(0xFF475569)
                ) {
                    if (renderEngine == RenderEngine.XML_CUSTOM_VIEW) {
                        customViewRef?.replayIntroAnimation()
                    } else {
                        pushLog("⚡ [Compose] Kích hoạt chạy lại hiệu ứng Intro Animation")
                    }
                }
            }
        }

        // =========================================================================
        // 3. NHẬT KÝ VÒNG ĐỜI TRỰC TIẾP (REALTIME LIFECYCLE CONSOLE)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            // Tiêu đề thanh Console và nút bấm Xóa Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhật ký Vòng đời (Console #001, #003, #009)",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { lifecycleLogs.clear() }) {
                    Text("Xoá log", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                }
            }

            // Danh sách cuộn hiển thị thứ tự gọi các hàm vòng đời
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(lifecycleLogs) { index, log ->
                    Text(
                        text = log,
                        // Dòng log mới nhất luôn hiển thị màu vàng nổi bật
                        color = if (index == 0) Color(0xFFFDE047) else Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Thành phần nút bấm thực hành vòng đời có giao diện chuẩn mực.
 */
@Composable
private fun LifecycleActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Color.White)
    }
}
