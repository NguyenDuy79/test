package com.example.fitness.myapplication.ui.labs

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.myapplication.CustomView.Candle
import com.example.fitness.myapplication.CustomView.ChartMode
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Controller quản lý dữ liệu và trạng thái hiển thị của Stock Chart phiên bản Jetpack Compose.
 */
class StockChartComposeState(
    initialCandleCount: Int = 80
) {
    val candles = mutableStateListOf<Candle>()
    val ma7Values = mutableStateListOf<Float?>()
    val ma20Values = mutableStateListOf<Float?>()

    var chartMode by mutableStateOf(ChartMode.CANDLESTICK)
    var candleWidthPx by mutableFloatStateOf(32f)
    var candleSpacingPx by mutableFloatStateOf(8f)
    var scrollOffsetPx by mutableFloatStateOf(0f)
    var selectedIndex by mutableIntStateOf(-1)
    var isCrosshairActive by mutableStateOf(false)
    var drawCount by mutableIntStateOf(0)

    var onLifecycleEvent: ((String) -> Unit)? = null

    init {
        generateInitialData(initialCandleCount)
    }

    private fun generateInitialData(count: Int) {
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
        recalculateMA()
    }

    fun recalculateMA() {
        ma7Values.clear()
        ma20Values.clear()

        for (i in candles.indices) {
            if (i >= 6) {
                var sum = 0f
                for (j in (i - 6)..i) sum += candles[j].close
                ma7Values.add(sum / 7f)
            } else {
                ma7Values.add(null)
            }

            if (i >= 19) {
                var sum = 0f
                for (j in (i - 19)..i) sum += candles[j].close
                ma20Values.add(sum / 20f)
            } else {
                ma20Values.add(null)
            }
        }
    }

    fun appendLiveCandle() {
        val lastIndex = candles.size
        val lastCandle = candles.lastOrNull()
        val lastClose = lastCandle?.close ?: 130f

        val hour = 9 + (lastIndex / 12) % 6
        val minute = (lastIndex % 12) * 5
        val label = String.format("%02d:%02d", hour, minute)

        val random = Random(System.currentTimeMillis())
        val change = (random.nextFloat() - 0.48f) * 3.2f
        val open = lastClose
        val close = (open + change).coerceAtLeast(20f)
        val high = max(open, close) + random.nextFloat() * 2.0f
        val low = min(open, close) - random.nextFloat() * 2.0f
        val volume = 120f + random.nextFloat() * 380f

        candles.add(Candle(lastIndex, label, open, high, low, close, volume))
        recalculateMA()

        onLifecycleEvent?.invoke("⚡ [Compose Canvas] appendLiveCandle() -> Thêm nến #${lastIndex} (Total: ${candles.size})")
    }
}

@Composable
fun rememberStockChartComposeState(initialCandleCount: Int = 80): StockChartComposeState {
    return remember { StockChartComposeState(initialCandleCount) }
}

/**
 * Thành phần Stock Chart thuần Jetpack Compose (Sử dụng Compose Canvas & Modifiers):
 * Minh họa cách triển khai Custom Graphics, Gesture Handling (Zoom, Scroll, Crosshair)
 * và State Animation hoàn toàn bằng Jetpack Compose để đối chiếu với XML CustomView truyền thống.
 */
@Composable
fun StockChartCompose(
    state: StockChartComposeState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Theo dõi vòng đời Composition của Compose View
    DisposableEffect(Unit) {
        state.onLifecycleEvent?.invoke("1. [Compose Lifecycle] Initial Composition (onRemember)")
        onDispose {
            state.onLifecycleEvent?.invoke("2. [Compose Lifecycle] Disposed from Composition (onCleared)")
        }
    }

    // Hiệu ứng mọc nến Intro Animation sử dụng Animatable của Compose
    val introProgress = remember { Animatable(0f) }

    fun runIntroAnim() {
        coroutineScope.launch {
            state.onLifecycleEvent?.invoke("⚡ [Compose] Kích hoạt Intro Animation (Animatable 0f -> 1f)")
            introProgress.snapTo(0f)
            introProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(Unit) {
        runIntroAnim()
    }

    // Hiệu ứng nhịp đập của giá trực tiếp (Live Pulse)
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseFraction"
    )

    val bullColor = Color(0xFF10B981)
    val bearColor = Color(0xFFEF4444)
    val ma7Color = Color(0xFFF59E0B)
    val ma20Color = Color(0xFF38BDF8)
    val lineColor = Color(0xFF22D3EE)
    val bgChartColor = Color(0xFF0F172A)
    val gridColor = Color(0xFF1E293B)
    val axisTextColor = Color(0xFF94A3B8)

    val activeCandle = if (state.isCrosshairActive && state.selectedIndex in state.candles.indices) {
        state.candles[state.selectedIndex]
    } else {
        state.candles.lastOrNull()
    }

    val prevClose = if (activeCandle != null && activeCandle.index > 0 && activeCandle.index - 1 in state.candles.indices) {
        state.candles[activeCandle.index - 1].close
    } else {
        activeCandle?.open ?: 130f
    }

    val diff = (activeCandle?.close ?: 0f) - prevClose
    val pct = if (prevClose != 0f) (diff / prevClose) * 100f else 0f
    val sign = if (diff >= 0) "+" else ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgChartColor, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // =========================================================================
        // 1. HEADER THÔNG TIN CHỨNG KHOÁN TRONG JETPACK COMPOSE
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FPT • VNINDEX",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⚡ JETPACK COMPOSE CANVAS",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(Color(0xFF0369A1), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (activeCandle != null) {
                    Text(
                        text = "${String.format("%.2f", activeCandle.close)} ($sign${String.format("%.2f", pct)}%)",
                        color = if (diff >= 0) bullColor else bearColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (activeCandle != null) {
                Text(
                    text = "T:${activeCandle.timeLabel}  O:${String.format("%.2f", activeCandle.open)}  H:${String.format("%.2f", activeCandle.high)}  L:${String.format("%.2f", activeCandle.low)}  C:${String.format("%.2f", activeCandle.close)}  V:${activeCandle.volume.toInt()}K",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val ma7Val = activeCandle?.let { state.ma7Values.getOrNull(it.index) }
                val ma20Val = activeCandle?.let { state.ma20Values.getOrNull(it.index) }

                Text(
                    text = "MA7: ${ma7Val?.let { String.format("%.2f", it) } ?: "--"}",
                    color = ma7Color,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "MA20: ${ma20Val?.let { String.format("%.2f", it) } ?: "--"}",
                    color = ma20Color,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Mode: ${state.chartMode.name}",
                    color = Color(0xFFA855F7),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // =========================================================================
        // 2. COMPOSE CANVAS VẼ BIỂU ĐỒ CHỨNG KHOÁN (CANDLESTICK + VOLUME + MA + CROSSHAIR)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Xử lý Zoom & Pan trong Compose
                            val oldWidth = state.candleWidthPx
                            state.candleWidthPx = (state.candleWidthPx * zoom).coerceIn(12f, 90f)
                            state.candleSpacingPx = (state.candleWidthPx * 0.25f).coerceAtLeast(4f)

                            val step = state.candleWidthPx + state.candleSpacingPx
                            val totalWidth = state.candles.size * step
                            val maxScroll = (totalWidth - size.width + 120f).coerceAtLeast(0f)

                            state.scrollOffsetPx = (state.scrollOffsetPx - pan.x).coerceIn(0f, maxScroll)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val step = state.candleWidthPx + state.candleSpacingPx
                                val index = ((offset.x + state.scrollOffsetPx) / step).toInt()
                                if (index in state.candles.indices) {
                                    if (state.isCrosshairActive && state.selectedIndex == index) {
                                        state.isCrosshairActive = false
                                        state.selectedIndex = -1
                                    } else {
                                        state.isCrosshairActive = true
                                        state.selectedIndex = index
                                    }
                                }
                            },
                            onLongPress = { offset ->
                                val step = state.candleWidthPx + state.candleSpacingPx
                                val index = ((offset.x + state.scrollOffsetPx) / step).toInt()
                                if (index in state.candles.indices) {
                                    state.isCrosshairActive = true
                                    state.selectedIndex = index
                                }
                            }
                        )
                    }
            ) {
                // Đếm số frame Compose Canvas vẽ lại
                state.drawCount++

                val w = size.width
                val h = size.height

                val yAxisWidth = 110f
                val chartRight = w - yAxisWidth
                val timeAxisHeight = 50f
                val chartBottom = h - timeAxisHeight

                val priceChartBottom = chartBottom * 0.72f
                val volumeChartTop = priceChartBottom + 16f
                val volumeChartBottom = chartBottom

                // 1. Vẽ Grid Lines nằm ngang
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (priceChartBottom / gridLines) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 1f
                    )
                }

                if (state.candles.isEmpty()) return@Canvas

                // 2. Tính toán khoảng giá Min/Max
                val step = state.candleWidthPx + state.candleSpacingPx
                val visibleStartIdx = max(0, (state.scrollOffsetPx / step).toInt())
                val visibleCount = ((chartRight / step) + 2).toInt()
                val visibleEndIdx = min(state.candles.lastIndex, visibleStartIdx + visibleCount)

                var minPrice = Float.MAX_VALUE
                var maxPrice = Float.MIN_VALUE
                var maxVolume = 1f

                for (i in visibleStartIdx..visibleEndIdx) {
                    val c = state.candles[i]
                    if (c.low < minPrice) minPrice = c.low
                    if (c.high > maxPrice) maxPrice = c.high
                    if (c.volume > maxVolume) maxVolume = c.volume
                }

                if (minPrice >= maxPrice) {
                    minPrice = 100f
                    maxPrice = 150f
                }
                val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)

                fun priceToY(price: Float): Float {
                    val normalized = (price - minPrice) / priceRange
                    return priceChartBottom - (normalized * (priceChartBottom - 20f))
                }

                // 3. Vẽ nhãn trục Y
                val nativePaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 24f
                    isAntiAlias = true
                }

                for (i in 0..gridLines) {
                    val priceVal = maxPrice - (priceRange / gridLines) * i
                    val y = (priceChartBottom / gridLines) * i
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format("%.1f", priceVal),
                        chartRight + 12f,
                        y + 8f,
                        nativePaint
                    )
                }

                // 4. Vẽ Candlestick hoặc Line Chart
                val anim = introProgress.value

                if (state.chartMode == ChartMode.CANDLESTICK) {
                    for (i in visibleStartIdx..visibleEndIdx) {
                        val c = state.candles[i]
                        val x = (i * step) - state.scrollOffsetPx + (state.candleWidthPx / 2f)

                        if (x < -state.candleWidthPx || x > chartRight + state.candleWidthPx) continue

                        val color = if (c.isBullish) bullColor else bearColor

                        // Râu nến
                        val highY = priceToY(c.high)
                        val lowY = priceToY(c.low)
                        drawLine(
                            color = color,
                            start = Offset(x, highY),
                            end = Offset(x, lowY),
                            strokeWidth = 3f
                        )

                        // Thân nến (kèm animation mọc)
                        val openY = priceToY(c.open)
                        val closeY = priceToY(c.close)
                        val topY = min(openY, closeY)
                        val rawHeight = max(abs(openY - closeY), 4f)
                        val animatedHeight = rawHeight * anim

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x - (state.candleWidthPx / 2f), topY),
                            size = Size(state.candleWidthPx, animatedHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Vẽ cột Volume
                        val volHeight = (c.volume / maxVolume) * (volumeChartBottom - volumeChartTop) * anim
                        drawRoundRect(
                            color = color.copy(alpha = 0.5f),
                            topLeft = Offset(x - (state.candleWidthPx / 2f), volumeChartBottom - volHeight),
                            size = Size(state.candleWidthPx, volHeight),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                } else {
                    // Line Area Mode
                    val linePath = Path()
                    val areaPath = Path()

                    var firstPoint = true
                    var lastX = 0f
                    var lastY = 0f

                    for (i in visibleStartIdx..visibleEndIdx) {
                        val c = state.candles[i]
                        val x = (i * step) - state.scrollOffsetPx + (state.candleWidthPx / 2f)
                        val y = priceToY(c.close)

                        if (firstPoint) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, priceChartBottom)
                            areaPath.lineTo(x, y)
                            firstPoint = false
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                        lastX = x
                        lastY = y
                    }

                    if (!firstPoint) {
                        areaPath.lineTo(lastX, priceChartBottom)
                        areaPath.close()

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.45f), Color.Transparent),
                                startY = 0f,
                                endY = priceChartBottom
                            )
                        )

                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 4f)
                        )
                    }
                }

                // 5. Vẽ MA7 và MA20 Paths
                val ma7Path = Path()
                val ma20Path = Path()
                var ma7First = true
                var ma20First = true

                for (i in visibleStartIdx..visibleEndIdx) {
                    val x = (i * step) - state.scrollOffsetPx + (state.candleWidthPx / 2f)

                    state.ma7Values.getOrNull(i)?.let { val7 ->
                        val y7 = priceToY(val7)
                        if (ma7First) {
                            ma7Path.moveTo(x, y7)
                            ma7First = false
                        } else {
                            ma7Path.lineTo(x, y7)
                        }
                    }

                    state.ma20Values.getOrNull(i)?.let { val20 ->
                        val y20 = priceToY(val20)
                        if (ma20First) {
                            ma20Path.moveTo(x, y20)
                            ma20First = false
                        } else {
                            ma20Path.lineTo(x, y20)
                        }
                    }
                }

                if (!ma7First) drawPath(ma7Path, color = ma7Color, style = Stroke(width = 3f))
                if (!ma20First) drawPath(ma20Path, color = ma20Color, style = Stroke(width = 3f))

                // 6. Chấm sáng nhấp nháy Live Price Pulse
                val lastCandle = state.candles.lastOrNull()
                if (lastCandle != null) {
                    val lastX = (lastCandle.index * step) - state.scrollOffsetPx + (state.candleWidthPx / 2f)
                    val lastY = priceToY(lastCandle.close)

                    if (lastX in 0f..chartRight) {
                        // Vòng tròn lan toả
                        drawCircle(
                            color = bullColor.copy(alpha = (1f - pulseFraction).coerceIn(0f, 1f)),
                            radius = 8f + (pulseFraction * 16f),
                            center = Offset(lastX, lastY)
                        )
                        // Chấm tâm
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(lastX, lastY)
                        )
                    }
                }

                // 7. Crosshair Tia Ngắm Tra Cứu Giá
                if (state.isCrosshairActive && state.selectedIndex in state.candles.indices) {
                    val selectedCandle = state.candles[state.selectedIndex]
                    val cx = (selectedCandle.index * step) - state.scrollOffsetPx + (state.candleWidthPx / 2f)
                    val cy = priceToY(selectedCandle.close)

                    // Đường đứt nét dọc & ngang
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, chartBottom),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )

                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(0f, cy),
                        end = Offset(chartRight, cy),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )

                    // Hộp nhãn giá bên phải
                    val priceBadgeText = String.format("%.2f", selectedCandle.close)
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(chartRight + 4f, cy - 18f),
                        size = Size(yAxisWidth - 8f, 36f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        priceBadgeText,
                        chartRight + 14f,
                        cy + 6f,
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 22f
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}
