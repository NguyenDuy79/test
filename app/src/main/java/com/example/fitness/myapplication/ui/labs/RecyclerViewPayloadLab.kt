package com.example.fitness.myapplication.ui.labs

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
 * - Câu #035, #036: DiffUtil, Card nháy khi cập nhật dữ liệu, và cách dùng Payload partial-bind
 * - Câu #030: clipToPadding
 */
@Composable
fun RecyclerViewPayloadLab() {
    var usePayloadMode by remember { mutableStateOf(true) }
    var clipToPaddingEnabled by remember { mutableStateOf(false) }

    val devices = remember {
        mutableStateListOf(
            DeviceItem("1", "Điều hoà phòng khách", 1.85f, true),
            DeviceItem("2", "Bình nóng lạnh", 2.40f, true),
            DeviceItem("3", "Tủ lạnh Inverter", 0.18f, true),
            DeviceItem("4", "Máy giặt sấy", 0.05f, false)
        )
    }

    var logMessage by remember {
        mutableStateOf("Bấm nút 'Cập nhật chỉ số điện' để quan sát sự khác biệt giữa Full Rebind và Payload.")
    }

    fun updateRandomDevice() {
        val targetIndex = Random.nextInt(devices.size)
        val old = devices[targetIndex]
        val newPower = (old.powerKw + (Random.nextFloat() - 0.45f) * 0.35f).coerceAtLeast(0.05f)

        if (usePayloadMode) {
            // CƠ CHẾ PAYLOAD: Chỉ cập nhật số điện, KHÔNG gọi onBind toàn bộ card
            devices[targetIndex] = old.copy(
                powerKw = newPower,
                partialPayloadTrigger = old.partialPayloadTrigger + 1
            )
            logMessage = "⚡ [PAYLOAD PARTIAL BIND] Thiết bị '${old.name}': Chỉ cập nhật TextView số điện. Card KHÔNG bị nháy!"
        } else {
            // CƠ CHẾ FULL REBIND: Giả lập notifyItemChanged(position) không payload
            devices[targetIndex] = old.copy(
                powerKw = newPower,
                fullRebindTrigger = old.fullRebindTrigger + 1
            )
            logMessage = "🚨 [FULL REBIND GÂY NHÁY] Gọi lại onBindViewHolder toàn bộ: Tái tạo ảnh, nền, layout -> Nhấp nháy toàn card!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "⚡ Lab #036 & #030: DiffUtil Payload & clipToPadding",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        // BẢNG ĐIỀU KHIỂN
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

                Button(
                    onClick = { updateRandomDevice() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (usePayloadMode) Color(0xFF0284C7) else Color(0xFFE11D48)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔄 Gửi dữ liệu cảm biến mới (Random)")
                }
            }
        }

        // DÒNG THÔNG BÁO TRẠNG THÁI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF021024), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = logMessage,
                color = if (usePayloadMode) Color(0xFFFDE047) else Color(0xFFFCA5A5),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // DANH SÁCH SIMULATION RECYCLERVIEW
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827), RoundedCornerShape(10.dp))
                .padding(if (clipToPaddingEnabled) 8.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices, key = { it.id }) { device ->
                DeviceCardSimulated(device = device)
            }
        }
    }
}

@Composable
private fun DeviceCardSimulated(device: DeviceItem) {
    // Nếu full rebind -> Card viền đỏ nhấp nháy cảnh báo re-layout toàn bộ item
    val flashBorderColor by animateColorAsState(
        targetValue = if (device.fullRebindTrigger % 2 == 1) Color(0xFFEF4444) else Color(0xFF334155),
        animationSpec = tween(durationMillis = 350),
        label = "flashBorder"
    )

    // Nếu partial payload -> Chỉ số điện nhảy nháy xanh mượt mà
    val numberColor by animateColorAsState(
        targetValue = if (device.partialPayloadTrigger % 2 == 1) Color(0xFF34D399) else Color(0xFF38BDF8),
        animationSpec = tween(durationMillis = 250),
        label = "numberColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, flashBorderColor, RoundedCornerShape(10.dp)),
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

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f kW", device.powerKw),
                    color = numberColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Full: ${device.fullRebindTrigger} | Payload: ${device.partialPayloadTrigger}",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
