package com.example.fitness.myapplication.ui.labs

// Import các hiệu ứng hoạt ảnh chuyển đổi màu sắc trong Jetpack Compose
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/**
 * Model đại diện cho một thiết bị điện tử trong nhà thông minh:
 * @property id Định danh duy nhất của thiết bị (dùng cho DiffUtil.areItemsTheSame)
 * @property name Tên hiển thị của thiết bị
 * @property powerKw Công suất tiêu thụ điện hiện tại tính bằng kiloWatt (kW)
 * @property isOnline Trạng thái kết nối mạng của thiết bị
 * @property fullRebindTrigger Biến đếm số lần kích hoạt Full Rebind (dùng tạo hiệu ứng nháy đỏ)
 * @property partialPayloadTrigger Biến đếm số lần cập nhật Payload (dùng tạo hiệu ứng nháy xanh)
 */
data class DeviceItem(
    val id: String,
    val name: String,
    val powerKw: Float,
    val isOnline: Boolean,
    val fullRebindTrigger: Int = 0,
    val partialPayloadTrigger: Int = 0
)

/**
 * Phòng thí nghiệm trực quan hoá:
 * - Câu #035, #036: Cơ chế DiffUtil & DiffUtil.ItemCallback:
 *   + areItemsTheSame(oldItem, newItem): So sánh id xem có cùng 1 item logic hay không.
 *   + areContentsTheSame(oldItem, newItem): So sánh toàn bộ nội dung dữ liệu.
 *   + getChangePayload(oldItem, newItem): Khi id giống nhau nhưng nội dung khác, trả về gói "Payload"
 *     chỉ định chính xác trường dữ liệu nào vừa thay đổi.
 *   + onBindViewHolder(holder, position, payloads): Hàm nhận payload để cập nhật partial-bind,
 *     ngăn chặn việc RecyclerView re-bind toàn bộ ViewHolder gây hiện tượng NHẤP NHÁY (blinking/flashing).
 * - Câu #030: clipToPadding trong RecyclerView:
 *   + clipToPadding = false cho phép nội dung danh sách khi cuộn có thể trượt tràn ra phía sau vùng padding lề.
 */
@Composable
fun RecyclerViewPayloadLab() {
    // Trạng thái bật/tắt chế độ cập nhật: Có dùng Payload hay không
    // true: Mô phỏng DiffUtil gửi Payload (chỉ bind TextView số điện)
    // false: Mô phỏng gọi notifyItemChanged() không payload (gây nhấp nháy cả card)
    var usePayloadMode by remember { mutableStateOf(true) }

    // Trạng thái bật/tắt thuộc tính clipToPadding (Câu #030)
    var clipToPaddingEnabled by remember { mutableStateOf(false) }

    // Danh sách các thiết bị điện mẫu ban đầu được lưu trong State Compose
    val devices = remember {
        mutableStateListOf(
            DeviceItem("1", "Điều hoà phòng khách", 1.85f, true),
            DeviceItem("2", "Bình nóng lạnh", 2.40f, true),
            DeviceItem("3", "Tủ lạnh Inverter", 0.18f, true),
            DeviceItem("4", "Máy giặt sấy", 0.05f, false)
        )
    }

    // Chuỗi thông báo kết quả thao tác được in ra màn hình
    var logMessage by remember {
        mutableStateOf("Bấm nút 'Cập nhật chỉ số điện' để quan sát sự khác biệt giữa Full Rebind và Payload.")
    }

    /**
     * Hàm mô phỏng việc dữ liệu thiết bị từ server hoặc cảm biến vừa gửi về:
     * Chọn ngẫu nhiên 1 thiết bị và thay đổi chỉ số điện năng powerKw.
     */
    fun updateRandomDevice() {
        // Chọn ngẫu nhiên vị trí của 1 thiết bị trong danh sách
        val targetIndex = Random.nextInt(devices.size)
        // Lấy dữ liệu cũ của thiết bị đó
        val old = devices[targetIndex]
        // Tạo chỉ số điện mới dao động ngẫu nhiên xung quanh giá trị cũ
        val newPower = (old.powerKw + (Random.nextFloat() - 0.45f) * 0.35f).coerceAtLeast(0.05f)

        if (usePayloadMode) {
            // =====================================================================
            // CƠ CHẾ PAYLOAD (PARTIAL BIND):
            // DiffUtil phát hiện chỉ có `powerKw` đổi, trả về payload = "PAYLOAD_POWER"
            // Adapter gọi onBindViewHolder(holder, pos, payloads)
            // Code chỉ cập nhật holder.tvPower.text = newPower
            // Toàn bộ ảnh đại diện, tên thiết bị, layout card KHÔNG bị vẽ lại -> KHÔNG BỊ NHÁY!
            // =====================================================================
            devices[targetIndex] = old.copy(
                powerKw = newPower,
                partialPayloadTrigger = old.partialPayloadTrigger + 1 // Tăng trigger hiệu ứng xanh
            )
            logMessage = "⚡ [PAYLOAD PARTIAL BIND] Thiết bị '${old.name}': Chỉ cập nhật TextView số điện. Card KHÔNG bị nháy!"
        } else {
            // =====================================================================
            // CƠ CHẾ FULL REBIND (GÂY NHÁY TOÀN BỘ CARD):
            // notifyItemChanged(position) không truyền payload -> danh sách payloads rỗng
            // Adapter buộc phải gọi onBindViewHolder(holder, position) cơ bản
            // View bị gán lại toàn bộ từ đầu: set màu nền, set lại text tên, icon, re-layout -> NHÁP NHÁY!
            // =====================================================================
            devices[targetIndex] = old.copy(
                powerKw = newPower,
                fullRebindTrigger = old.fullRebindTrigger + 1 // Tăng trigger hiệu ứng đỏ cảnh báo
            )
            logMessage = "🚨 [FULL REBIND GÂY NHÁY] Gọi lại onBindViewHolder toàn bộ: Tái tạo ảnh, nền, layout -> Nhấp nháy toàn card!"
        }
    }

    // Bố cục cột chứa toàn bộ giao diện của phòng thí nghiệm
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Nền tối
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tiêu đề phòng thí nghiệm
        Text(
            text = "⚡ Lab #036 & #030: DiffUtil Payload & clipToPadding",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        // =========================================================================
        // BẢNG ĐIỀU KHIỂN CẤU HÌNH RECYCLERVIEW
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Switch 1: Chuyển đổi giữa Chế độ Payload và Chế độ Full Rebind
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (usePayloadMode) "Chế độ: Có Payload (Partial Bind)" else "Chế độ: Full Rebind (Gây nháy)",
                            color = if (usePayloadMode) Color(0xFF34D399) else Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (usePayloadMode) "onBindViewHolder(holder, pos, payloads)" else "onBindViewHolder(holder, pos) thường",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = usePayloadMode,
                        onCheckedChange = { usePayloadMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }

                // Switch 2: Thuộc tính clipToPadding (Câu #030)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "clipToPadding=\"false\" (Câu #030)",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Cho phép danh sách cuộn tràn dưới padding lề",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = clipToPaddingEnabled,
                        onCheckedChange = { clipToPaddingEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                    )
                }

                // Nút kích hoạt gửi dữ liệu mới để test
                Button(
                    onClick = { updateRandomDevice() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        // Nút màu xanh nếu đang ở chế độ Payload, màu đỏ nếu đang mô phỏng chế độ nhấp nháy
                        containerColor = if (usePayloadMode) Color(0xFF0284C7) else Color(0xFFE11D48)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔄 Gửi dữ liệu cảm biến mới (Random)")
                }
            }
        }

        // =========================================================================
        // HỘP THÔNG BÁO TRẠNG THÁI VÀ GIẢI THÍCH KỸ THUẬT
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF021024), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = logMessage,
                // Chữ màu vàng nếu dùng payload thành công, màu đỏ nhạt nếu bị nháy do full rebind
                color = if (usePayloadMode) Color(0xFFFDE047) else Color(0xFFFCA5A5),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // =========================================================================
        // DANH SÁCH MÔ PHỎNG RECYCLERVIEW (DÙNG LAZYCOLUMN)
        // =========================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827), RoundedCornerShape(10.dp))
                // Nếu clipToPaddingEnabled = true thì áp dụng padding trong 8dp, ngược lại 4dp
                .padding(if (clipToPaddingEnabled) 8.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Duyệt qua từng item thiết bị, cung cấp key ổn định (tương đương getStableIds hoặc ItemCallback.areItemsTheSame)
            items(devices, key = { it.id }) { device ->
                DeviceCardSimulated(device = device)
            }
        }
    }
}

/**
 * Thành phần Card mô phỏng một ViewHolder trong RecyclerView:
 * Minh họa trực quan sự khác biệt giữa Full Rebind và Partial Rebind.
 */
@Composable
private fun DeviceCardSimulated(device: DeviceItem) {
    // Hoạt ảnh màu đường viền của Card:
    // Khi fullRebindTrigger thay đổi, viền card sẽ chớp màu đỏ rực cảnh báo ViewHolder bị vẽ lại toàn bộ
    val flashBorderColor by animateColorAsState(
        targetValue = if (device.fullRebindTrigger % 2 == 1) Color(0xFFEF4444) else Color(0xFF334155),
        animationSpec = tween(durationMillis = 350),
        label = "flashBorder"
    )

    // Hoạt ảnh màu của con số công suất điện (kW):
    // Khi partialPayloadTrigger thay đổi, chỉ có màu số điện chớp xanh mượt mà, khung card đứng yên
    val numberColor by animateColorAsState(
        targetValue = if (device.partialPayloadTrigger % 2 == 1) Color(0xFF34D399) else Color(0xFF38BDF8),
        animationSpec = tween(durationMillis = 250),
        label = "numberColor"
    )

    // Khung Card đại diện cho 1 Item View
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, flashBorderColor, RoundedCornerShape(10.dp)), // Viền nhấp nháy khi full rebind
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột bên trái: Tên thiết bị và trạng thái Online (Các trường không bị đổi khi cập nhật số điện)
            Column {
                Text(
                    text = device.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = if (device.isOnline) "🟢 Trực tuyến" else "⚪ Ngoại tuyến",
                    color = if (device.isOnline) Color(0xFF10B981) else Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
            }

            // Cột bên phải: Chỉ số công suất điện (Trường duy nhất thay đổi)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f kW", device.powerKw),
                    color = numberColor, // Đổi màu xanh nhẹ nhàng khi áp dụng partial bind
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                // Hiển thị bộ đếm số lần Full Rebind và Partial Rebind để lập trình viên theo dõi
                Text(
                    text = "Full: ${device.fullRebindTrigger} | Payload: ${device.partialPayloadTrigger}",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
