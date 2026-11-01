package com.example.fitness.myapplication.ui.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Phòng thí nghiệm trực quan hoá:
 * - Câu #007: Luồng phân phát Touch Event, onInterceptTouchEvent, ACTION_CANCEL
 *   và requestDisallowInterceptTouchEvent.
 */
@Composable
fun TouchDispatchLab() {
    var disallowInterceptEnabled by remember { mutableStateOf(false) }
    var parentInterceptsDrag by remember { mutableStateOf(true) }
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    var isCancelled by remember { mutableStateOf(false) }

    val logs = remember {
        mutableStateListOf(
            "👉 Chạm hoặc vuốt vào ô View Con màu xanh bên dưới để quan sát luồng Event.",
            "ℹ️ Thử bật/tắt Switch 'Disallow Intercept' để xem Cha có cướp được Touch không."
        )
    }

    fun addLog(msg: String) {
        logs.add(0, msg)
        if (logs.size > 50) logs.removeAt(logs.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "👆 Lab #007: Luồng Touch Event & ACTION_CANCEL",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        // BẢNG ĐIỀU KHIỂN RÀNG BUỘC
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "requestDisallowInterceptTouchEvent(true)",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Con cấm Cha chặn touch (khoá onInterceptTouchEvent)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = disallowInterceptEnabled,
                        onCheckedChange = { disallowInterceptEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cha cướp Touch khi vuốt dọc > 30px",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Mô phỏng ScrollView/ViewPager chặn để cuộn trang",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = parentInterceptsDrag,
                        onCheckedChange = { parentInterceptsDrag = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                    )
                }
            }
        }

        // KHUNG MÔ PHỎNG TOUCH TƯƠNG TÁC
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isCancelled) Color(0xFF7F1D1D) else Color(0xFF065F46),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.5.dp,
                        if (isCancelled) Color(0xFFEF4444) else Color(0xFF10B981),
                        RoundedCornerShape(8.dp)
                    )
                    .pointerInput(disallowInterceptEnabled, parentInterceptsDrag) {
                        detectDragGestures(
                            onDragStart = {
                                dragAccumulatedY = 0f
                                isCancelled = false
                                addLog("🟢 [ACTION_DOWN] Cha.dispatch -> Cha.onIntercept(false) -> Con.onTouch: Nhận touch")
                            },
                            onDragEnd = {
                                isCancelled = false
                                addLog("🏁 [ACTION_UP] Hoàn tất cử chỉ chạm")
                            },
                            onDragCancel = {
                                isCancelled = true
                                addLog("🔴 [ACTION_CANCEL] View Con bị ngắt touch! Cha đã cướp quyền xử lý.")
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatedY += abs(dragAmount.y)

                                if (!isCancelled) {
                                    if (parentInterceptsDrag && !disallowInterceptEnabled && dragAccumulatedY > 40f) {
                                        isCancelled = true
                                        addLog("⚠️ [Cha.onInterceptTouchEvent = TRUE] Vuốt dọc > 40px -> Cha cướp touch!")
                                        addLog("🚨 [Con nhận ACTION_CANCEL] Con phải dọn dẹp state (reset màu, dừng scroll)")
                                    } else {
                                        addLog("🔵 [ACTION_MOVE] Con xử lý dy=${dragAmount.y.toInt()}px (Disallow=$disallowInterceptEnabled)")
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isCancelled) "❌ CON ĐÃ NHẬN ACTION_CANCEL!" else "🎯 VIEW CON (Chạm & Vuốt vào đây)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isCancelled) "Cha (ScrollView) đang sở hữu Touch" else "Đang nhận MotionEvent bình thường",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // LOG REALTIME CONSOLE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF021024), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhật ký Dispatch Console (LIFO):",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { logs.clear() }) {
                    Text("Xoá log", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(logs) { index, line ->
                    Text(
                        text = line,
                        color = when {
                            index == 0 -> Color(0xFFFDE047)
                            line.contains("ACTION_CANCEL") -> Color(0xFFF87171)
                            line.contains("ACTION_DOWN") -> Color(0xFF34D399)
                            else -> Color(0xFFCBD5E1)
                        },
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
