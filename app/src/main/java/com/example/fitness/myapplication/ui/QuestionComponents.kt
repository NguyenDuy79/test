package com.example.fitness.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.fitness.myapplication.data.LabType
import com.example.fitness.myapplication.data.QuestionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBrowserScreen(
    questions: List<QuestionItem>,
    onOpenLab: (LabType) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedQuestion by remember { mutableStateOf<QuestionItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = remember {
        listOf(
            "Tất cả",
            "Custom View & Vòng đời",
            "View Hierarchy & Render Pipeline",
            "Layouts, XML & Theming",
            "RecyclerView Chuyên Sâu",
            "Fragment, ViewModel & Back Navigation"
        )
    }

    val filteredQuestions = remember(searchQuery, selectedCategory, questions) {
        questions.filter { q ->
            val matchesCategory = (selectedCategory == "Tất cả" || q.category == selectedCategory)
            val matchesQuery = searchQuery.isBlank() ||
                q.question.contains(searchQuery, ignoreCase = true) ||
                q.num.contains(searchQuery, ignoreCase = true) ||
                q.answer.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // THANH TÌM KIẾM
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm kiếm trong 40 câu hỏi...", color = Color(0xFF64748B), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // THANH FILTER CHỦ ĐỀ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Text(
            text = "Hiển thị ${filteredQuestions.size} / ${questions.size} câu hỏi (chạm vào để xem chi tiết & code mẫu):",
            color = Color(0xFF94A3B8),
            fontSize = 11.5.sp
        )

        // DANH SÁCH CÂU HỎI
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredQuestions, key = { it.id }) { item ->
                QuestionSummaryCard(
                    item = item,
                    onClick = { selectedQuestion = item },
                    onOpenLab = { lab -> onOpenLab(lab) }
                )
            }
        }
    }

    // BOTTOM SHEET CHI TIẾT CÂU HỎI
    selectedQuestion?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedQuestion = null },
            sheetState = sheetState,
            containerColor = Color(0xFF0F172A)
        ) {
            QuestionDetailContent(
                item = item,
                onOpenLab = { lab ->
                    selectedQuestion = null
                    onOpenLab(lab)
                }
            )
        }
    }
}

@Composable
fun QuestionSummaryCard(
    item: QuestionItem,
    onClick: () -> Unit,
    onOpenLab: (LabType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.num,
                        color = Color(0xFFFDE047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                when (item.level.lowercase()) {
                                    "junior" -> Color(0xFF065F46)
                                    "middle" -> Color(0xFF0369A1)
                                    else -> Color(0xFF7C2D12)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.level, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item.relatedLab?.let { lab ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF312E81), RoundedCornerShape(4.dp))
                            .clickable { onOpenLab(lab) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🧪 Có Lab", color = Color(0xFFA5B4FC), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Text(
                text = item.question,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = item.answer.take(120).replace("\n", " ") + "...",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
fun QuestionDetailContent(
    item: QuestionItem,
    onOpenLab: (LabType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.num} • ${item.category}",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            item.relatedLab?.let { lab ->
                Button(
                    onClick = { onOpenLab(lab) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("🧪 Mở Demo Thực Hành", fontSize = 11.5.sp)
                }
            }
        }

        Text(
            text = item.question,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        // PHẦN GIẢI THÍCH CHI TIẾT
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📖 Phân Tích & Nguyên Lý Hoạt Động:",
                    color = Color(0xFFFDE047),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = item.answer,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // PHẦN CODE MINH HOẠ NẾU CÓ
        if (item.code.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF021024)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "💻 Code Mẫu Minh Hoạ:",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = item.code,
                        color = Color(0xFFE0F2FE),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // PHẦN BẪY & TIPS NẾU CÓ
        if (item.tips.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, Color(0xFFD97706), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "⚠️ Bẫy Phỏng Vấn & Lưu Ý:",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = item.tips,
                        color = Color(0xFFFEF3C7),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // PHẦN LIÊN HỆ DỰ ÁN NẾU CÓ
        if (item.projectRef.isNotBlank()) {
            Text(
                text = "📁 Áp dụng trong project: ${item.projectRef}",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
