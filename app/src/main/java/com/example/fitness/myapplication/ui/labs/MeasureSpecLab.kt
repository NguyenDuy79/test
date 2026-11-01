package com.example.fitness.myapplication.ui.labs

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Phòng thí nghiệm trực quan hoá:
 * - Câu #002: MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED), ma trận getChildMeasureSpec & resolveSize
 * - Câu #010: dp, px, sp và chuyển đổi
 * - Câu #014: view.width == 0 trong onCreate và cách đo đúng
 */
@Composable
fun MeasureSpecLab() {
    var parentSpecMode by remember { mutableStateOf(ParentSpecMode.EXACTLY) }
    var parentSizePx by remember { mutableFloatStateOf(320f) }
    var childLayoutParams by remember { mutableStateOf(ChildLayoutParamsType.WRAP_CONTENT) }
    var childDesiredContentPx by remember { mutableFloatStateOf(180f) }
    var paddingPx by remember { mutableFloatStateOf(24f) }
    var isPaddingIncluded by remember { mutableStateOf(true) }

    // Tính toán MeasureSpec con dựa trên quy tắc ViewGroup.getChildMeasureSpec
    val childSpec = calculateChildMeasureSpec(parentSpecMode, parentSizePx.toInt(), childLayoutParams)
    val childModeName = when (View.MeasureSpec.getMode(childSpec)) {
        View.MeasureSpec.EXACTLY -> "EXACTLY"
        View.MeasureSpec.AT_MOST -> "AT_MOST"
        View.MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        else -> "UNKNOWN"
    }
    val childSpecSize = View.MeasureSpec.getSize(childSpec)

    // Tính kích thước con qua resolveSize
    val totalDesired = childDesiredContentPx + (if (isPaddingIncluded) paddingPx * 2 else 0f)
    val finalMeasuredWidth = when (View.MeasureSpec.getMode(childSpec)) {
        View.MeasureSpec.EXACTLY -> childSpecSize
        View.MeasureSpec.AT_MOST -> min(totalDesired.toInt(), childSpecSize)
        else -> totalDesired.toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "🧪 Lab #002 & #014: Trực quan hoá MeasureSpec",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )
        Text(
            text = "Khám phá cách View cha áp đặt MeasureSpec lên View con và cách resolveSize tính toán kích thước thực tế.",
            fontSize = 12.5.sp,
            color = Color(0xFF94A3B8)
        )

        // 1. CẤU HÌNH VIEW CHA
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. Yêu cầu từ View Cha (Parent Spec):",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ParentSpecMode.entries.forEach { mode ->
                        FilterChip(
                            selected = parentSpecMode == mode,
                            onClick = { parentSpecMode = mode },
                            label = { Text(mode.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                if (parentSpecMode != ParentSpecMode.UNSPECIFIED) {
                    Text(
                        text = "Kích thước Cha cung cấp: ${parentSizePx.toInt()}px",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = parentSizePx,
                        onValueChange = { parentSizePx = it },
                        valueRange = 160f..400f
                    )
                } else {
                    Text(
                        text = "ℹ️ UNSPECIFIED: Thường xuất hiện trong ScrollView / RecyclerView theo chiều cuộn (không giới hạn trần).",
                        color = Color(0xFFFDE047),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // 2. CẤU HÌNH VIEW CON (LayoutParams & Desired Size)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "2. Cấu hình View Con (LayoutParams & Content):",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChildLayoutParamsType.entries.forEach { lp ->
                        FilterChip(
                            selected = childLayoutParams == lp,
                            onClick = { childLayoutParams = lp },
                            label = { Text(lp.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "Nội dung mong muốn (desired content): ${childDesiredContentPx.toInt()}px",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
                Slider(
                    value = childDesiredContentPx,
                    onValueChange = { childDesiredContentPx = it },
                    valueRange = 80f..300f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Padding 2 bên: ${(paddingPx * 2).toInt()}px",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { isPaddingIncluded = !isPaddingIncluded },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaddingIncluded) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPaddingIncluded) "Đã cộng Padding (ĐÚNG)" else "Quên Padding (BẪY)",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. KẾT QUẢ TÍNH TOÁN
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF021D3A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "📊 Kết Quả Đo Đạc Thực Tế (getChildMeasureSpec):",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = "• Spec con nhận được: mode = $childModeName, size = ${childSpecSize}px",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = "• Tổng mong muốn (desired): ${totalDesired.toInt()}px",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = "• Kích thước chốt (setMeasuredDimension): ${finalMeasuredWidth}px",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )

                if (!isPaddingIncluded) {
                    Text(
                        text = "⚠️ BẪY CÂU #002: Bạn quên cộng padding vào desired! Nội dung sẽ bị tràn hoặc bị cắt mép.",
                        color = Color(0xFFF87171),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. MÔ PHỎNG VISUAL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Hình ảnh mô phỏng View Cha & View Con trên màn hình:",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            // Khung View cha
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF64748B), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Khung View con
                val visualW = (finalMeasuredWidth.dp * 0.7f).coerceIn(40.dp, 300.dp)
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(visualW)
                        .height(60.dp)
                        .background(Color(0xFF0284C7), RoundedCornerShape(6.dp))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Con: ${finalMeasuredWidth}px",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

enum class ParentSpecMode {
    EXACTLY,
    AT_MOST,
    UNSPECIFIED
}

enum class ChildLayoutParamsType(val displayName: String) {
    EXACT_DP("150dp cụ thể"),
    MATCH_PARENT("match_parent"),
    WRAP_CONTENT("wrap_content")
}

private fun calculateChildMeasureSpec(
    parentMode: ParentSpecMode,
    parentSize: Int,
    childLp: ChildLayoutParamsType
): Int {
    val pMode = when (parentMode) {
        ParentSpecMode.EXACTLY -> View.MeasureSpec.EXACTLY
        ParentSpecMode.AT_MOST -> View.MeasureSpec.AT_MOST
        ParentSpecMode.UNSPECIFIED -> View.MeasureSpec.UNSPECIFIED
    }
    val childDimension = when (childLp) {
        ChildLayoutParamsType.EXACT_DP -> 150
        ChildLayoutParamsType.MATCH_PARENT -> -1 // MATCH_PARENT
        ChildLayoutParamsType.WRAP_CONTENT -> -2 // WRAP_CONTENT
    }
    return View.MeasureSpec.makeMeasureSpec(
        when (pMode) {
            View.MeasureSpec.EXACTLY -> when {
                childDimension >= 0 -> childDimension
                childDimension == -1 -> parentSize
                else -> parentSize
            }
            View.MeasureSpec.AT_MOST -> when {
                childDimension >= 0 -> childDimension
                childDimension == -1 -> parentSize
                else -> parentSize
            }
            else -> when {
                childDimension >= 0 -> childDimension
                else -> 0
            }
        },
        when (pMode) {
            View.MeasureSpec.EXACTLY -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY
                childDimension == -1 -> View.MeasureSpec.EXACTLY
                else -> View.MeasureSpec.AT_MOST
            }
            View.MeasureSpec.AT_MOST -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY
                childDimension == -1 -> View.MeasureSpec.AT_MOST
                else -> View.MeasureSpec.AT_MOST
            }
            else -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY
                else -> View.MeasureSpec.UNSPECIFIED
            }
        }
    )
}
