package com.example.fitness.myapplication.ui.labs

// Import các thành phần hỗ trợ Animation và Bố cục giao diện của Jetpack Compose
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Phòng thí nghiệm trực quan hoá các bẫy phỏng vấn cốt lõi về Fragment và Navigation:
 * - Câu #042: Tại sao phải gán `_binding = null` tại onDestroyView() và sự tách biệt vòng đời giữa:
 *   + Fragment Instance (sống từ onAttach -> onDetach, được giữ trong BackStack).
 *   + View của Fragment (chết tại onDestroyView, chỉ sống từ onCreateView -> onDestroyView).
 *   + viewLifecycleOwner: Phải luôn truyền viewLifecycleOwner khi observe LiveData/Flow trong Fragment,
 *     nếu truyền `this` sẽ gây observe lặp nhiều lần (Duplicate Observer bug).
 * - Câu #045: Bug 2 Fragment đè lên nhau khi xoay màn hình (Configuration Change):
 *   + Xảy ra khi lập trình viên quên check `if (savedInstanceState == null)` trước khi thực hiện FragmentTransaction.add().
 * - Câu #046 & #047: Cơ chế giao tiếp giữa 2 Fragment:
 *   + Fragment Result API (setFragmentResult & setFragmentResultListener): Chuẩn mới, thay thế interface cũ.
 *   + Shared ViewModel (activityViewModels() / navGraphViewModels()): Dùng khi chia sẻ state phức tạp.
 * - Câu #050: Predictive Back Animation (Android 14 - Android 16):
 *   + Hệ thống cử chỉ vuốt lùi có thể xem trước màn hình cha thu nhỏ dần trước khi nhả tay xác nhận thoát.
 */
@Composable
fun FragmentNavLab() {
    // Trạng thái màn hình hiện tại: đang ở Fragment B (Chi tiết) hay Fragment A (Danh sách)
    var isDetailOpen by remember { mutableStateOf(false) }

    // Trạng thái bật/tắt cơ chế gán _binding = null trong onDestroyView() (Bẫy Câu #042)
    // true: Đúng chuẩn, giải phóng ViewBinding khi Fragment vào BackStack để tránh Memory Leak
    // false: Quên set null, giữ tham chiếu View đã bị hủy -> Gây rò rỉ bộ nhớ nghiêm trọng
    var nullifyBindingOnDestroy by remember { mutableStateOf(true) }

    // Trạng thái bật/tắt kiểm tra savedInstanceState == null khi xoay màn hình (Bẫy Câu #045)
    // true: Kiểm tra đúng, FragmentManager tự khôi phục, không add thêm instance mới
    // false: Quên kiểm tra, tạo mới và add chồng thêm một Fragment nữa lên giao diện
    var checkSavedStateOnRotate by remember { mutableStateOf(true) }

    // Đếm số lượng Fragment instance đang bị chồng đè lên nhau trong container (Mô phỏng Bug #045)
    var stackedFragmentCount by remember { mutableIntStateOf(1) }

    // Tiến trình vuốt Predictive Back Gesture từ mép màn hình (giá trị từ 0.0f đến 1.0f)
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }

    // Biến lưu trữ dữ liệu chia sẻ giữa 2 Fragment (Mô phỏng Shared ViewModel hoặc Fragment Result API)
    var sharedStateValue by remember { mutableStateOf("Thiết bị #01 (Bình thường)") }

    // Danh sách nhật ký sự kiện vòng đời hiển thị trên màn hình Console LIFO
    val logs = remember {
        mutableStateListOf(
            "👉 Thử mở màn hình Chi tiết (Fragment B) rồi bấm Back hoặc mô phỏng xoay màn hình.",
            "ℹ️ Quan sát vòng đời tách biệt giữa Fragment Instance và ViewLifecycleOwner."
        )
    }

    // Hàm tiện ích thêm log lên đầu danh sách và giới hạn tối đa 40 dòng
    fun addLog(msg: String) {
        logs.add(0, msg)
        if (logs.size > 40) logs.removeAt(logs.lastIndex)
    }

    // Cột bố cục tổng thể có thể cuộn dọc
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tiêu đề phòng thí nghiệm
        Text(
            text = "🧩 Lab #042, #045 & #050: Fragment & Predictive Back",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        // =========================================================================
        // BẢNG ĐIỀU KHIỂN CÁC RÀNG BUỘC VÒNG ĐỜI VÀ BẪY PHỎNG VẤN
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. SWITCH NULLIFY VIEW BINDING TẠI ONDESTROYVIEW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "_binding = null ở onDestroyView (Câu #042)",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (nullifyBindingOnDestroy)
                                "✅ Đúng: Giải phóng View khi vào BackStack (chống Leak)"
                            else
                                "❌ Sai: Giữ tham chiếu View chết gây rò rỉ bộ nhớ!",
                            color = if (nullifyBindingOnDestroy) Color(0xFF34D399) else Color(0xFFF87171),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = nullifyBindingOnDestroy,
                        onCheckedChange = { nullifyBindingOnDestroy = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }

                // 2. SWITCH CHECK SAVEDINSTANCESTATE KHI XOAY MÀN HÌNH (CÂU #045)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "if (savedInstanceState == null) (Câu #045)",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (checkSavedStateOnRotate)
                                "✅ Đúng: Không add đè Fragment mới sau xoay"
                            else
                                "❌ Sai: Add thêm Fragment mới -> Bị đè 2 lớp!",
                            color = if (checkSavedStateOnRotate) Color(0xFF34D399) else Color(0xFFF87171),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = checkSavedStateOnRotate,
                        onCheckedChange = { checkSavedStateOnRotate = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                    )
                }

                // 3. NÚT BẤM MÔ PHỎNG XOAY MÀN HÌNH (CONFIGURATION CHANGE)
                Button(
                    onClick = {
                        if (checkSavedStateOnRotate) {
                            // Đúng chuẩn: savedInstanceState != null nên không gọi fragmentTransaction.add() thêm lần nữa
                            addLog("🔄 [Xoay màn hình] savedInstanceState != null -> FragmentManager tự khôi phục, giữ nguyên 1 Fragment duy nhất.")
                        } else {
                            // Mắc bẫy: Mỗi lần xoay màn hình lại add thêm 1 Fragment mới
                            stackedFragmentCount++
                            addLog("🚨 [BUG CÂU #045] Xoay màn hình: Không check null -> Add thêm Fragment! Hiện có $stackedFragmentCount Fragment chồng đè!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔄 Mô phỏng xoay màn hình (Configuration Change)")
                }
            }
        }

        // =========================================================================
        // THANH TRƯỢT MÔ PHỎNG PREDICTIVE BACK GESTURE (CÂU #050)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🔙 Predictive Back Gesture (Android 14–16 - Câu #050):",
                    color = Color(0xFFFDE047),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Kéo thanh trượt để xem trước màn hình cha co lại trước khi nhả tay thoát hoàn toàn:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
                // Slider mô phỏng việc người dùng vuốt từ cạnh màn hình vào trong
                Slider(
                    value = predictiveBackProgress,
                    onValueChange = {
                        predictiveBackProgress = it
                        if (it > 0.05f) {
                            addLog("🔙 [Predictive Back] Tiến trình vuốt: ${(it * 100).toInt()}% (OnBackInvokedCallback)")
                        }
                    },
                    valueRange = 0f..1f
                )
            }
        }

        // =========================================================================
        // KHUNG CONTAINER CHỨA VÀ HIỂN THỊ FRAGMENT (FRAGMENT CONTAINER VIEW)
        // =========================================================================
        // Hiệu ứng thu nhỏ kích thước của màn hình theo cử chỉ Predictive Back
        val containerScale by animateFloatAsState(
            targetValue = 1f - (predictiveBackProgress * 0.15f), // Thu nhỏ tối đa còn 85%
            label = "predictiveScale"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .graphicsLayer {
                    // Áp dụng tỉ lệ co giãn theo cử chỉ Back
                    scaleX = containerScale
                    scaleY = containerScale
                }
                .background(Color(0xFF021D3A), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isDetailOpen) {
                // =================================================================
                // GIAO DIỆN FRAGMENT A (MÀN HÌNH DANH SÁCH)
                // =================================================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📱 Fragment A (Danh Sách Thiết Bị)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Dữ liệu Shared: $sharedStateValue",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    // Cảnh báo trực quan nếu bị dính lỗi 2 Fragment chồng đè nhau
                    if (stackedFragmentCount > 1) {
                        Text(
                            text = "⚠️ Có $stackedFragmentCount instance đang chạy đè lên nhau (Bug #045)!",
                            color = Color(0xFFEF4444),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Nút chuyển trang: Gọi FragmentTransaction replace và addToBackStack
                    Button(
                        onClick = {
                            isDetailOpen = true
                            addLog("🚀 [FragmentTransaction.replace] Mở Fragment B (addToBackStack). View A bị destroy nhưng Fragment A còn sống.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Mở Fragment B Chi Tiết ->", fontSize = 12.sp)
                    }
                }
            } else {
                // =================================================================
                // GIAO DIỆN FRAGMENT B (MÀN HÌNH CHI TIẾT)
                // =================================================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📄 Fragment B (Chi Tiết Cài Đặt)",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Nút mô phỏng Fragment Result API truyền kết quả ngược về cho Fragment A
                        Button(
                            onClick = {
                                sharedStateValue = "Đã đổi: Chế độ Tiết Kiệm Điện (Fragment Result API #046)"
                                addLog("📬 [setFragmentResult] B gửi kết quả về A qua Fragment Result API.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Gửi Kết Quả Về A", fontSize = 11.sp)
                        }

                        // Nút mô phỏng người dùng bấm Back để thoát Fragment B
                        Button(
                            onClick = {
                                isDetailOpen = false
                                predictiveBackProgress = 0f
                                if (nullifyBindingOnDestroy) {
                                    // Đúng chuẩn: View của B bị hủy hoàn toàn và giải phóng bộ nhớ
                                    addLog("🧹 [onDestroyView] _binding = null -> Thu hồi toàn bộ View cây con sạch sẽ.")
                                } else {
                                    // Bẫy leak: Tham chiếu binding còn giữ cây View trong bộ nhớ heap
                                    addLog("🚨 [LEAK BẪY #042] _binding KHÔNG null -> Giữ tham chiếu View dù View đã chết!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Bấm Back (Quay lại A)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // BẢNG CONSOLE NHẬT KÝ THAO TÁC VÒNG ĐỜI
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF021024), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhật ký Vòng đời Fragment (LIFO):",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    logs.clear()
                    stackedFragmentCount = 1
                }) {
                    Text("Reset", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            // Danh sách log hiển thị theo màu sắc phân loại
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(logs) { index, line ->
                    Text(
                        text = line,
                        color = when {
                            index == 0 -> Color(0xFFFDE047)
                            line.contains("LEAK") || line.contains("BUG") -> Color(0xFFF87171)
                            line.contains("setFragmentResult") -> Color(0xFF34D399)
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
