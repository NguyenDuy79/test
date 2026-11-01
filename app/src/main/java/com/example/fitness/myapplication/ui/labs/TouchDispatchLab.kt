package com.example.fitness.myapplication.ui.labs

// Import hàm animate và phát hiện cử chỉ kéo (drag) trong Jetpack Compose
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
 * - Câu #007: Luồng phân phát Touch Event trong Android View Hierarchy.
 *   + dispatchTouchEvent(): Quyết định ai sẽ xử lý sự kiện tiếp theo (từ ViewGroup cha truyền xuống con).
 *   + onInterceptTouchEvent(): ViewGroup cha kiểm tra xem có chặn cướp quyền touch của con hay không.
 *   + onTouchEvent(): Nơi trực tiếp xử lý sự kiện cảm ứng (ACTION_DOWN, ACTION_MOVE, ACTION_UP).
 *   + ACTION_CANCEL: Sự kiện hệ thống gửi tới View con khi ViewGroup cha cướp mất quyền touch giữa chừng.
 *   + requestDisallowInterceptTouchEvent(true): Lệnh con cấm cha cướp touch (chặn hàm onIntercept của cha).
 */
@Composable
fun TouchDispatchLab() {
    // Trạng thái bật/tắt cờ requestDisallowInterceptTouchEvent(true) từ phía View con
    // Nếu true: View con yêu cầu ViewGroup cha KHÔNG được gọi onInterceptTouchEvent để cướp quyền
    var disallowInterceptEnabled by remember { mutableStateOf(false) }

    // Trạng thái mô phỏng ViewGroup cha (như ScrollView) có cơ chế rình cướp Touch khi vuốt dọc hay không
    var parentInterceptsDrag by remember { mutableStateOf(true) }

    // Biến tích lũy quãng đường vuốt theo trục Y (tính bằng pixel) để so sánh với TouchSlop
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }

    // Cờ đánh dấu View con đã bị hủy quyền điều khiển và nhận sự kiện ACTION_CANCEL hay chưa
    var isCancelled by remember { mutableStateOf(false) }

    // Danh sách các dòng nhật ký mô phỏng luồng gọi hàm giữa Cha và Con theo thời gian thực
    val logs = remember {
        mutableStateListOf(
            "👉 Chạm hoặc vuốt vào ô View Con màu xanh bên dưới để quan sát luồng Event.",
            "ℹ️ Thử bật/tắt Switch 'Disallow Intercept' để xem Cha có cướp được Touch không."
        )
    }

    // Hàm tiện ích: Thêm log mới lên đầu danh sách (LIFO) và giới hạn tối đa 50 dòng để tiết kiệm RAM
    fun addLog(msg: String) {
        // Chèn vào vị trí đầu tiên (index 0) để log mới nhất luôn hiện trên cùng
        logs.add(0, msg)
        // Nếu số lượng log vượt quá 50 dòng thì xóa bớt dòng cũ nhất ở cuối
        if (logs.size > 50) logs.removeAt(logs.lastIndex)
    }

    // Bố cục tổng thể dạng cột thẳng đứng chiếm toàn bộ không gian màn hình
    Column(
        modifier = Modifier
            .fillMaxSize() // Chiếm trọn chiều rộng và chiều cao có sẵn
            .background(Color(0xFF0F172A)) // Nền tối màu Slate 900
            .padding(14.dp), // Lề trong 14dp
        verticalArrangement = Arrangement.spacedBy(10.dp) // Khoảng cách giữa các thành phần con là 10dp
    ) {
        // Tiêu đề phòng thí nghiệm
        Text(
            text = "👆 Lab #007: Luồng Touch Event & ACTION_CANCEL",
            fontSize = 17.sp, // Kích thước chữ 17sp
            fontWeight = FontWeight.Bold, // Chữ in đậm nổi bật
            color = Color(0xFF38BDF8) // Màu xanh lam Cyan sáng
        )

        // =========================================================================
        // BẢNG ĐIỀU KHIỂN CÁC RÀNG BUỘC CỦA TOUCH EVENT
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Màu thẻ Slate 800
            shape = RoundedCornerShape(10.dp) // Bo cong góc 10dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp), // Padding bên trong thẻ
                verticalArrangement = Arrangement.spacedBy(8.dp) // Giãn cách 8dp
            ) {
                // Hàng điều khiển 1: Bật/tắt cấm cha cướp quyền
                Row(
                    modifier = Modifier.fillMaxWidth(), // Trải rộng toàn bộ chiều ngang
                    horizontalArrangement = Arrangement.SpaceBetween, // Căn đều 2 bên mép
                    verticalAlignment = Alignment.CenterVertically // Canh giữa theo chiều dọc
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
                            color = Color(0xFF94A3B8), // Màu chữ chú thích xám nhạt
                            fontSize = 11.sp
                        )
                    }
                    // Công tắc chuyển đổi cờ Disallow Intercept
                    Switch(
                        checked = disallowInterceptEnabled,
                        onCheckedChange = { disallowInterceptEnabled = it }, // Cập nhật trạng thái
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981)) // Nút gạt màu xanh ngọc
                    )
                }

                // Hàng điều khiển 2: Bật/tắt tính năng ViewGroup cha cướp quyền khi phát hiện vuốt
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
                    // Công tắc chuyển đổi cơ chế cướp touch của cha
                    Switch(
                        checked = parentInterceptsDrag,
                        onCheckedChange = { parentInterceptsDrag = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                    )
                }
            }
        }

        // =========================================================================
        // KHUNG MÔ PHỎNG TOUCH TƯƠNG TÁC (VIEWGROUP CHA & VIEW CON)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth() // Chiều rộng tối đa
                .height(130.dp) // Chiều cao cố định 130dp mô phỏng khu vực chạm
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)) // Nền khung cha
                .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(12.dp)) // Đường viền ngoài của cha
                .padding(12.dp), // Lề đệm để View con nằm lọt bên trong cha
            contentAlignment = Alignment.Center // Canh giữa View con
        ) {
            // Đây là đại diện cho VIEW CON nhận sự kiện cảm ứng
            Box(
                modifier = Modifier
                    .fillMaxSize() // View con phủ kín không gian cha cho phép
                    .background(
                        // Nếu bị CANCEL đổi màu đỏ cảnh báo, nếu đang nhận touch bình thường giữ màu xanh lục
                        if (isCancelled) Color(0xFF7F1D1D) else Color(0xFF065F46),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.5.dp,
                        if (isCancelled) Color(0xFFEF4444) else Color(0xFF10B981),
                        RoundedCornerShape(8.dp)
                    )
                    // Lắng nghe cử chỉ chạm trên View con theo cơ chế Gesture Detector
                    .pointerInput(disallowInterceptEnabled, parentInterceptsDrag) {
                        detectDragGestures(
                            // 1. Khi ngón tay bắt đầu chạm vào màn hình (Tương đương ACTION_DOWN)
                            onDragStart = {
                                dragAccumulatedY = 0f // Đặt lại khoảng cách vuốt về 0
                                isCancelled = false // Đặt lại cờ hủy
                                // Ghi log luồng phân phát dispatchTouchEvent:
                                // Window -> Activity -> ViewGroup Cha (onIntercept = false) -> View Con (onTouchEvent = true)
                                addLog("🟢 [ACTION_DOWN] Cha.dispatch -> Cha.onIntercept(false) -> Con.onTouch: Nhận touch")
                            },
                            // 2. Khi người dùng nhấc tay lên kết thúc hành vi chạm (Tương đương ACTION_UP)
                            onDragEnd = {
                                isCancelled = false // Kết thúc tương tác bình thường
                                addLog("🏁 [ACTION_UP] Hoàn tất cử chỉ chạm thành công (không bị cướp)")
                            },
                            // 3. Khi cử chỉ bị hệ thống hoặc cha hủy bỏ (Tương đương ACTION_CANCEL)
                            onDragCancel = {
                                isCancelled = true // Đánh dấu đã bị hủy
                                addLog("🔴 [ACTION_CANCEL] View Con bị ngắt touch! Cha đã cướp quyền xử lý.")
                            },
                            // 4. Khi ngón tay đang di chuyển trên bề mặt (Tương đương ACTION_MOVE)
                            onDrag = { change, dragAmount ->
                                // Đánh dấu sự kiện con trỏ này đã được xử lý để tránh xung đột
                                change.consume()

                                // Tích lũy quãng đường di chuyển theo phương thẳng đứng Y
                                dragAccumulatedY += abs(dragAmount.y)

                                // Nếu View con chưa bị ngắt quyền xử lý (chưa nhận CANCEL)
                                if (!isCancelled) {
                                    // Kiểm tra điều kiện cướp quyền của ViewGroup Cha:
                                    // 1. Cha có bật tính năng chặn (parentInterceptsDrag == true)
                                    // 2. Con KHÔNG cấm cha (disallowInterceptEnabled == false)
                                    // 3. Khoảng cách vuốt đã vượt quá ngưỡng TouchSlop (ở đây giả lập là 40px)
                                    if (parentInterceptsDrag && !disallowInterceptEnabled && dragAccumulatedY > 40f) {
                                        // Cha quyết định trả về true trong onInterceptTouchEvent() -> Cướp quyền!
                                        isCancelled = true // View con chính thức bị vô hiệu hóa trong lượt chạm này
                                        addLog("⚠️ [Cha.onInterceptTouchEvent = TRUE] Vuốt dọc > 40px -> Cha cướp touch!")
                                        addLog("🚨 [Con nhận ACTION_CANCEL] Con phải dọn dẹp state (reset màu, dừng scroll)")
                                    } else {
                                        // Con vẫn an toàn tiếp tục nhận sự kiện ACTION_MOVE
                                        addLog("🔵 [ACTION_MOVE] Con xử lý dy=${dragAmount.y.toInt()}px (Disallow=$disallowInterceptEnabled)")
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center // Canh giữa văn bản hiển thị trạng thái
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        // Thông báo chữ lớn trạng thái hiện tại của View con
                        text = if (isCancelled) "❌ CON ĐÃ NHẬN ACTION_CANCEL!" else "🎯 VIEW CON (Chạm & Vuốt vào đây)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        // Giải thích ý nghĩa của trạng thái bên dưới
                        text = if (isCancelled) "Cha (ScrollView) đang sở hữu Touch" else "Đang nhận MotionEvent bình thường",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // =========================================================================
        // NHẬT KÝ CONSOLE THEO DÕI LUỒNG DISPATCH TOUCH REALTIME
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxSize() // Chiếm toàn bộ phần chiều cao còn lại của màn hình
                .background(Color(0xFF021024), RoundedCornerShape(10.dp)) // Khung hiển thị màu xanh đêm đậm
                .padding(10.dp) // Padding 10dp
        ) {
            // Thanh tiêu đề của hộp Console và nút bấm Xóa Log
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
                TextButton(onClick = { logs.clear() }) { // Xóa sạch lịch sử log khi cần thử nghiệm lại từ đầu
                    Text("Xoá log", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            // Danh sách cuộn hiển thị từng dòng sự kiện đã diễn ra
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Khoảng cách giữa các dòng log là 4dp
            ) {
                itemsIndexed(logs) { index, line ->
                    Text(
                        text = line,
                        // Tô màu phân biệt các loại sự kiện quan trọng trong luồng touch:
                        color = when {
                            index == 0 -> Color(0xFFFDE047) // Dòng mới nhất luôn có màu vàng sáng
                            line.contains("ACTION_CANCEL") -> Color(0xFFF87171) // Sự kiện hủy báo màu đỏ
                            line.contains("ACTION_DOWN") -> Color(0xFF34D399) // Sự kiện bắt đầu báo màu xanh lá
                            else -> Color(0xFFCBD5E1) // Sự kiện di chuyển thông thường màu xám trắng
                        },
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace // Font Monospace giúp đọc log như terminal lập trình
                    )
                }
            }
        }
    }
}
