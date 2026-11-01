package com.example.fitness.myapplication

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitness.myapplication.data.LabType
import com.example.fitness.myapplication.data.QuestionRepository
import com.example.fitness.myapplication.ui.QuestionBrowserScreen
import com.example.fitness.myapplication.ui.labs.FragmentNavLab
import com.example.fitness.myapplication.ui.labs.MeasureSpecLab
import com.example.fitness.myapplication.ui.labs.RecyclerViewPayloadLab
import com.example.fitness.myapplication.ui.labs.TouchDispatchLab
import com.example.fitness.myapplication.ui.theme.MyApplicationTheme

enum class MainAppTab(val title: String) {
    QUESTIONS("📚 50 Câu Hỏi"),
    STOCK_CHART("📈 Biểu Đồ Stock"),
    MEASURE_SPEC("📏 MeasureSpec"),
    TOUCH_DISPATCH("👆 Touch Flow"),
    RECYCLER_VIEW("⚡ RecyclerView"),
    FRAGMENT_NAV("🧩 Fragment & Back")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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

@Composable
fun MasteryAppRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val allQuestions = remember { QuestionRepository.getQuestions(context) }
    var currentTab by remember { mutableStateOf(MainAppTab.QUESTIONS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // THANH TAB NAVIGATION Ở ĐẦU
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainAppTab.entries.forEach { tab ->
                FilterChip(
                    selected = currentTab == tab,
                    onClick = { currentTab = tab },
                    label = { Text(tab.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        // NỘI DUNG TỪNG TAB
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                MainAppTab.QUESTIONS -> {
                    QuestionBrowserScreen(
                        questions = allQuestions,
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
                MainAppTab.STOCK_CHART -> {
                    StockChartLifecycleLabScreen(modifier = Modifier.fillMaxSize())
                }
                MainAppTab.MEASURE_SPEC -> {
                    MeasureSpecLab()
                }
                MainAppTab.TOUCH_DISPATCH -> {
                    TouchDispatchLab()
                }
                MainAppTab.RECYCLER_VIEW -> {
                    RecyclerViewPayloadLab()
                }
                MainAppTab.FRAGMENT_NAV -> {
                    FragmentNavLab()
                }
            }
        }
    }
}

@Composable
fun StockChartLifecycleLabScreen(modifier: Modifier = Modifier) {
    val lifecycleLogs = remember {
        mutableStateListOf(
            "👉 Vuốt ngang để cuộn (có quán tính Fling), dùng 2 ngón để Zoom, chạm/nhấn giữ để bật tia ngắm Crosshair."
        )
    }
    var customViewRef by remember { mutableStateOf<CustomView?>(null) }
    var isAttached by remember { mutableStateOf(true) }
    var isCompactHeight by remember { mutableStateOf(false) }
    var isViewVisible by remember { mutableStateOf(true) }
    var chartMode by remember { mutableStateOf(CustomView.ChartMode.CANDLESTICK) }
    var drawCounterSnapshot by remember { mutableIntStateOf(0) }

    fun pushLog(event: String) {
        lifecycleLogs.add(0, event)
        if (lifecycleLogs.size > 40) {
            lifecycleLogs.removeAt(lifecycleLogs.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // 1. VÙNG CHỨA CUSTOM VIEW BIỂU ĐỒ CHỨNG KHOÁN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompactHeight) 280.dp else 370.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isAttached) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
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
                    update = { view ->
                        view.visibility = if (isViewVisible) View.VISIBLE else View.INVISIBLE
                        view.chartMode = chartMode
                        drawCounterSnapshot = view.drawCount
                    },
                    onRelease = { _ ->
                        customViewRef = null
                    }
                )
            } else {
                Text(
                    text = "View đã bị gỡ khỏi Window (onDetachedFromWindow)",
                    color = Color(0xFFF87171),
                    fontSize = 14.sp
                )
            }
        }

        // 2. BẢNG NÚT BẤM THỰC HÀNH CÁC NHÁNH VÒNG ĐỜI VIEW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LifecycleActionButton(
                    label = "+ Nến mới (invalidate #003)",
                    color = Color(0xFF10B981)
                ) {
                    customViewRef?.appendLiveCandle()
                    drawCounterSnapshot = customViewRef?.drawCount ?: 0
                }

                LifecycleActionButton(
                    label = if (chartMode == CustomView.ChartMode.CANDLESTICK) {
                        "Đổi sang Line"
                    } else {
                        "Đổi sang Nến"
                    },
                    color = Color(0xFF0284C7)
                ) {
                    chartMode = if (chartMode == CustomView.ChartMode.CANDLESTICK) {
                        CustomView.ChartMode.LINE_AREA
                    } else {
                        CustomView.ChartMode.CANDLESTICK
                    }
                }

                LifecycleActionButton(
                    label = "Đổi kích thước (requestLayout #003)",
                    color = Color(0xFFD97706)
                ) {
                    pushLog("⚡ Kích hoạt requestLayout() -> Chạy onMeasure/onLayout")
                    isCompactHeight = !isCompactHeight
                    customViewRef?.requestLayout()
                }

                LifecycleActionButton(
                    label = if (isViewVisible) "Ẩn View (INVISIBLE)" else "Hiện View (VISIBLE)",
                    color = Color(0xFF7C3AED)
                ) {
                    isViewVisible = !isViewVisible
                }

                LifecycleActionButton(
                    label = if (isAttached) "Gỡ View (Detach #001)" else "Gắn lại (Attach)",
                    color = Color(0xFFE11D48)
                ) {
                    isAttached = !isAttached
                }

                LifecycleActionButton(
                    label = "Chạy lại Intro Animator",
                    color = Color(0xFF475569)
                ) {
                    customViewRef?.replayIntroAnimation()
                }
            }
        }

        // 3. NHẬT KÝ VÒNG ĐỜI TRỰC TIẾP (REALTIME LIFECYCLE CONSOLE)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(lifecycleLogs) { index, log ->
                    Text(
                        text = log,
                        color = if (index == 0) Color(0xFFFDE047) else Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

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
