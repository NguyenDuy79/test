package com.example.fitness.myapplication.ui.labs

// Import View từ Android framework để sử dụng lớp MeasureSpec và các hằng số đo lường
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
 * - Câu #002: MeasureSpec (EXACTLY, AT_MOST, UNSPECIFIED), ma trận ViewGroup.getChildMeasureSpec & resolveSize.
 *   + MeasureSpec là một số nguyên 32-bit: 2 bit đầu chứa MODE (chế độ đo), 30 bit sau chứa SIZE (kích thước pixel).
 *   + EXACTLY: Cha ấn định kích thước chính xác (ví dụ con đặt 100dp hoặc match_parent với cha có kích thước cố định).
 *   + AT_MOST: Cha đặt giới hạn trần (con không được vượt quá kích thước này, tương ứng wrap_content).
 *   + UNSPECIFIED: Cha không giới hạn kích thước con (con muốn bao nhiêu cũng được, như trong ScrollView theo chiều cuộn).
 * - Câu #010: dp, px, sp và cách chuyển đổi đúng bằng TypedValue.applyDimension.
 * - Câu #014: Bẫy view.width == 0 trong onCreate() và cách đo đúng bằng view.post { ... } hoặc ViewTreeObserver.
 */
@Composable
fun MeasureSpecLab() {
    // Chế độ đo của ViewGroup cha áp đặt lên con (Mặc định là EXACTLY)
    var parentSpecMode by remember { mutableStateOf(ParentSpecMode.EXACTLY) }

    // Kích thước của ViewGroup cha cung cấp tính bằng pixel (mặc định 320px)
    var parentSizePx by remember { mutableFloatStateOf(320f) }

    // Cấu hình LayoutParams của View con (wrap_content, match_parent hoặc kích thước cố định dp)
    var childLayoutParams by remember { mutableStateOf(ChildLayoutParamsType.WRAP_CONTENT) }

    // Kích thước nội dung bên trong mà View con mong muốn (desired content width) không tính padding
    var childDesiredContentPx by remember { mutableFloatStateOf(180f) }

    // Giá trị padding mỗi bên của View con tính bằng pixel (mặc định 24px)
    var paddingPx by remember { mutableFloatStateOf(24f) }

    // Cờ kiểm tra lập trình viên có nhớ cộng Padding vào kích thước mong muốn hay không (Bẫy Câu #002)
    var isPaddingIncluded by remember { mutableStateOf(true) }

    // =========================================================================
    // TÍNH TOÁN MEASURESPEC CỦA CON DỰA TRÊN QUY TẮC ViewGroup.getChildMeasureSpec
    // =========================================================================
    // Hàm này kết hợp (Parent Mode + Parent Size) và (Child LayoutParams) để sinh ra Child MeasureSpec 32-bit
    val childSpec = calculateChildMeasureSpec(parentSpecMode, parentSizePx.toInt(), childLayoutParams)

    // Trích xuất tên của chế độ đo (Mode) từ MeasureSpec 32-bit của con bằng View.MeasureSpec.getMode()
    val childModeName = when (View.MeasureSpec.getMode(childSpec)) {
        View.MeasureSpec.EXACTLY -> "EXACTLY"
        View.MeasureSpec.AT_MOST -> "AT_MOST"
        View.MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        else -> "UNKNOWN"
    }

    // Trích xuất giá trị kích thước (Size) bằng pixel từ MeasureSpec 32-bit bằng View.MeasureSpec.getSize()
    val childSpecSize = View.MeasureSpec.getSize(childSpec)

    // =========================================================================
    // TÍNH KÍCH THƯỚC CON QUA CƠ CHẾ resolveSize() TRONG onMeasure()
    // =========================================================================
    // Tổng kích thước con mong muốn = (Nội dung mong muốn) + (Padding 2 bên nếu có cộng)
    val totalDesired = childDesiredContentPx + (if (isPaddingIncluded) paddingPx * 2 else 0f)

    // Thuật toán của hàm View.resolveSize(desiredSize, measureSpec):
    // - EXACTLY: Kích thước bắt buộc lấy theo cha (childSpecSize).
    // - AT_MOST: Lấy giá trị nhỏ hơn giữa kích thước con mong muốn và giới hạn trần của cha (minOf(desired, size)).
    // - UNSPECIFIED: Lấy đúng kích thước con mong muốn (desiredSize) vì cha không hề giới hạn trần.
    val finalMeasuredWidth = when (View.MeasureSpec.getMode(childSpec)) {
        View.MeasureSpec.EXACTLY -> childSpecSize
        View.MeasureSpec.AT_MOST -> min(totalDesired.toInt(), childSpecSize)
        else -> totalDesired.toInt()
    }

    // Bố cục tổng thể của màn hình thí nghiệm
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Màu nền Slate 900
            .verticalScroll(rememberScrollState()) // Cho phép cuộn dọc nội dung
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp) // Giãn cách 14dp
    ) {
        // Tiêu đề thí nghiệm
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

        // =========================================================================
        // 1. KHUNG ĐIỀU KHIỂN CẤU HÌNH VIEW CHA (PARENT SPEC)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "1. Yêu cầu từ View Cha (Parent Spec):",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                // Các chip chọn chế độ đo của cha: EXACTLY, AT_MOST, UNSPECIFIED
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
                // Nếu cha không phải UNSPECIFIED thì cho phép trượt đổi kích thước cha cung cấp
                if (parentSpecMode != ParentSpecMode.UNSPECIFIED) {
                    Text(
                        text = "Kích thước Cha cung cấp: ${parentSizePx.toInt()}px",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    // Thanh trượt điều chỉnh kích thước cha từ 160px đến 400px
                    Slider(
                        value = parentSizePx,
                        onValueChange = { parentSizePx = it },
                        valueRange = 160f..400f
                    )
                } else {
                    // Chú thích ý nghĩa của UNSPECIFIED
                    Text(
                        text = "ℹ️ UNSPECIFIED: Thường xuất hiện trong ScrollView / RecyclerView theo chiều cuộn (không giới hạn trần).",
                        color = Color(0xFFFDE047),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // =========================================================================
        // 2. KHUNG ĐIỀU KHIỂN CẤU HÌNH VIEW CON (LAYOUTPARAMS & CONTENT SIZE)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "2. Cấu hình View Con (LayoutParams & Content):",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                // Chọn LayoutParams: 150dp cụ thể, match_parent hoặc wrap_content
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

                // Điều chỉnh kích thước nội dung mong muốn của View con (ví dụ độ dài text/ảnh)
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

                // Hàng bật/tắt bẫy quên cộng Padding trong hàm onMeasure()
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
                    // Nút bấm chuyển đổi: Có cộng padding hay quên cộng padding
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

        // =========================================================================
        // 3. KẾT QUẢ TÍNH TOÁN THEO MA TRẬN MEASURESPEC VÀ RESOLVESIZE
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF021D3A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📊 Kết Quả Đo Đạc Thực Tế (getChildMeasureSpec):",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                // Hiển thị MeasureSpec con nhận được
                Text(
                    text = "• Spec con nhận được: mode = $childModeName, size = ${childSpecSize}px",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                // Hiển thị kích thước con mong muốn
                Text(
                    text = "• Tổng mong muốn (desired): ${totalDesired.toInt()}px",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                // Hiển thị kích thước cuối cùng sau resolveSize() chốt vào setMeasuredDimension
                Text(
                    text = "• Kích thước chốt (setMeasuredDimension): ${finalMeasuredWidth}px",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )

                // Cảnh báo nếu lập trình viên mắc phải bẫy quên padding
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

        // =========================================================================
        // 4. MÔ PHỎNG HÌNH ẢNH TRỰC QUAN CỦA VIEW CHA VÀ VIEW CON
        // =========================================================================
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

            // Khung đại diện cho View Cha (màu xám xanh)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF64748B), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Khung đại diện cho View Con (màu xanh dương đậm)
                val visualW = (finalMeasuredWidth.dp * 0.7f).coerceIn(40.dp, 300.dp)
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(visualW) // Chiều rộng tương ứng với kết quả đo đạc thực tế
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

/**
 * Định nghĩa 3 chế độ đo MeasureSpec của View Cha:
 * - EXACTLY: Cha đã biết chính xác kích thước.
 * - AT_MOST: Cha chỉ biết kích thước tối đa.
 * - UNSPECIFIED: Cha hoàn toàn không áp đặt giới hạn kích thước lên con.
 */
enum class ParentSpecMode {
    EXACTLY,
    AT_MOST,
    UNSPECIFIED
}

/**
 * Định nghĩa loại kích thước mà lập trình viên khai báo trong XML cho View Con:
 * - EXACT_DP: Khai báo cứng con số dp cụ thể (ví dụ layout_width="150dp").
 * - MATCH_PARENT: Muốn mở rộng bằng kích thước cha cho phép (layout_width="match_parent", giá trị số là -1).
 * - WRAP_CONTENT: Muốn vừa khít nội dung bên trong (layout_width="wrap_content", giá trị số là -2).
 */
enum class ChildLayoutParamsType(val displayName: String) {
    EXACT_DP("150dp cụ thể"),
    MATCH_PARENT("match_parent"),
    WRAP_CONTENT("wrap_content")
}

/**
 * Hàm mô phỏng chính xác thuật toán ViewGroup.getChildMeasureSpec(parentSpec, padding, childDimension):
 * Đây là câu hỏi kinh điển trong phỏng vấn Android Senior về ma trận kết hợp giữa Cha và Con:
 *
 * @param parentMode Chế độ đo của View Cha
 * @param parentSize Kích thước của View Cha (đã trừ padding cha)
 * @param childLp Khai báo LayoutParams của View Con trong file XML
 * @return Số nguyên 32-bit chứa cả Mode và Size cho View Con
 */
private fun calculateChildMeasureSpec(
    parentMode: ParentSpecMode,
    parentSize: Int,
    childLp: ChildLayoutParamsType
): Int {
    // Chuyển enum của bài Lab sang hằng số nguyên chuẩn của View.MeasureSpec
    val pMode = when (parentMode) {
        ParentSpecMode.EXACTLY -> View.MeasureSpec.EXACTLY
        ParentSpecMode.AT_MOST -> View.MeasureSpec.AT_MOST
        ParentSpecMode.UNSPECIFIED -> View.MeasureSpec.UNSPECIFIED
    }

    // Giá trị số nguyên tương ứng trong LayoutParams của Android:
    // >= 0 là số pixel cụ thể, -1 là MATCH_PARENT, -2 là WRAP_CONTENT
    val childDimension = when (childLp) {
        ChildLayoutParamsType.EXACT_DP -> 150 // Giả lập 150px cụ thể
        ChildLayoutParamsType.MATCH_PARENT -> -1 // ViewGroup.LayoutParams.MATCH_PARENT
        ChildLayoutParamsType.WRAP_CONTENT -> -2 // ViewGroup.LayoutParams.WRAP_CONTENT
    }

    // Kết hợp Mode và Size thành số nguyên 32-bit thông qua hàm makeMeasureSpec:
    return View.MeasureSpec.makeMeasureSpec(
        // =====================================================================
        // NHÁNH 1: TÍNH TOÁN GIÁ TRỊ SIZE TRUYỀN CHO CON
        // =====================================================================
        when (pMode) {
            // Khi Cha là EXACTLY (Ví dụ cha có size cố định 300dp)
            View.MeasureSpec.EXACTLY -> when {
                childDimension >= 0 -> childDimension // Con muốn 150px -> Trả về đúng 150px
                childDimension == -1 -> parentSize    // Con muốn match_parent -> Trả về toàn bộ kích thước của cha
                else -> parentSize                   // Con muốn wrap_content -> Trả về kích thước tối đa của cha để con không vượt quá
            }
            // Khi Cha là AT_MOST (Ví dụ cha là wrap_content có trần tối đa)
            View.MeasureSpec.AT_MOST -> when {
                childDimension >= 0 -> childDimension // Con muốn 150px -> Trả về đúng 150px
                childDimension == -1 -> parentSize    // Con muốn match_parent -> Con không được vượt quá trần của cha
                else -> parentSize                   // Con muốn wrap_content -> Con không được vượt quá trần của cha
            }
            // Khi Cha là UNSPECIFIED (Ví dụ trong ScrollView/RecyclerView cuộn dọc)
            else -> when {
                childDimension >= 0 -> childDimension // Con muốn 150px -> Trả về đúng 150px
                else -> 0                            // Con match_parent hoặc wrap_content -> Size là 0 (vì chưa biết giới hạn)
            }
        },
        // =====================================================================
        // NHÁNH 2: TÍNH TOÁN CHẾ ĐỘ MODE TRUYỀN CHO CON
        // =====================================================================
        when (pMode) {
            View.MeasureSpec.EXACTLY -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY // Con chỉ định cụ thể -> Chế độ đo của con là EXACTLY
                childDimension == -1 -> View.MeasureSpec.EXACTLY // Con match_parent với cha EXACTLY -> Con nhận EXACTLY
                else -> View.MeasureSpec.AT_MOST                // Con wrap_content -> Con nhận AT_MOST (tối đa bằng cha)
            }
            View.MeasureSpec.AT_MOST -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY // Con chỉ định cụ thể -> Con nhận EXACTLY
                childDimension == -1 -> View.MeasureSpec.AT_MOST // Con match_parent nhưng cha chỉ là AT_MOST -> Con chỉ nhận AT_MOST
                else -> View.MeasureSpec.AT_MOST                // Con wrap_content -> Con nhận AT_MOST
            }
            else -> when {
                childDimension >= 0 -> View.MeasureSpec.EXACTLY // Con chỉ định cụ thể -> Con nhận EXACTLY
                else -> View.MeasureSpec.UNSPECIFIED            // Cha không giới hạn thì con cũng nhận UNSPECIFIED
            }
        }
    )
}
