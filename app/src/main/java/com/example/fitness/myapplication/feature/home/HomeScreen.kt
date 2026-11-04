package com.example.fitness.myapplication.feature.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.appstate.LocalCommonStore
import com.example.fitness.myapplication.core.base.CollectEffect
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.component.EdupiaCard
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is HomeEffect.ShowToast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
        }
    }

    HomeContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    Scaffold(
        containerColor = EdupiaColors.BackgroundWhite,
        bottomBar = {
            EdupiaBottomBar(
                selectedTab = state.selectedTab,
                onTabSelected = { tab -> onIntent(HomeIntent.SelectTab(tab)) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "HomeTabTransition"
            ) { tab ->
                when (tab) {
                    HomeTab.HOME -> HomeTabContent(state = state, onIntent = onIntent)
                    HomeTab.CLASS -> ClassTabContent()
                    HomeTab.PRACTICE -> PracticeTabContent()
                    HomeTab.AI_TALK -> AITalkTabContent()
                    HomeTab.PROFILE -> ProfileTabContent(state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun EdupiaBottomBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    NavigationBar(
        containerColor = EdupiaColors.BackgroundWhite,
        tonalElevation = 8.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val iconRes = when (tab) {
                HomeTab.HOME -> if (isSelected) R.drawable.ichomestateopening else R.drawable.ichomestateclosed
                HomeTab.CLASS -> if (isSelected) R.drawable.icaiclassnavselected else R.drawable.icaiclassnav
                HomeTab.PRACTICE -> if (isSelected) R.drawable.icaipracticenavselected else R.drawable.icaipracticenav
                HomeTab.AI_TALK -> if (isSelected) R.drawable.icaitalknavselected else R.drawable.icaitalknav
                HomeTab.PROFILE -> if (isSelected) R.drawable.icchoseprofilenavselected else R.drawable.icchoseprofilenav
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = EdupiaTypography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) EdupiaColors.Primary else EdupiaColors.Neutral500
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EdupiaColors.Primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// ================= Tab 1: Trang chủ =================
@Composable
private fun HomeTabContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner chào mừng
        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Xin chào, ${state.profileName.ifEmpty { "Học viên Edupia" }}! 👋",
                    style = EdupiaTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chào mừng bạn quay trở lại với Edupia.",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
            }
        }

        // Thông tin gói học
        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Gói học của bạn",
                    style = EdupiaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.isLoadingLicenses) "Đang tải dữ liệu gói học..."
                    else "Gói học đang hoạt động: ${state.activeLicenseCount}",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral700
                )
                Spacer(modifier = Modifier.height(12.dp))
                EdupiaButton(
                    text = "Tải lại thông tin",
                    onClick = { onIntent(HomeIntent.Refresh) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AppVersionLabel()
    }
}

// ================= Tab 2: Lớp học =================
@Composable
private fun ClassTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Lớp học",
            style = EdupiaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = EdupiaColors.Neutral900
        )

        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "📚 Danh sách lớp học trực tuyến",
                    style = EdupiaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tham gia các buổi học tương tác cùng giáo viên bản ngữ và các bạn học viên.",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
            }
        }
    }
}

// ================= Tab 3: Luyện tập =================
@Composable
private fun PracticeTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Luyện tập",
            style = EdupiaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = EdupiaColors.Neutral900
        )

        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "🎯 Phòng luyện tập kỹ năng",
                    style = EdupiaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Luyện Nghe, Nói, Đọc, Viết với hàng ngàn bài tập tương tác hấp dẫn.",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
            }
        }
    }
}

// ================= Tab 4: AI Talk =================
@Composable
private fun AITalkTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Talk",
            style = EdupiaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = EdupiaColors.Neutral900
        )

        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "🤖 Trò chuyện Tiếng Anh cùng AI",
                    style = EdupiaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tự tin giao tiếp và phản xạ Tiếng Anh tự nhiên cùng gia sư AI thông minh.",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
            }
        }
    }
}

// ================= Tab 5: Tài khoản =================
@Composable
private fun ProfileTabContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tài khoản",
            style = EdupiaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = EdupiaColors.Neutral900
        )

        EdupiaCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Thông tin cá nhân",
                    style = EdupiaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Neutral800
                )
                Text(
                    text = "Họ và tên: ${state.profileName.ifEmpty { "Chưa cập nhật" }}",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral700
                )
                Text(
                    text = "Số điện thoại: ${state.phone.ifEmpty { "Chưa cập nhật" }}",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral700
                )
                Text(
                    text = "Số lượng hồ sơ học viên: ${state.profileCount}",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral700
                )
            }
        }

        EdupiaButton(
            text = "Đăng xuất",
            onClick = { onIntent(HomeIntent.SignOut) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AppVersionLabel() {
    if (LocalInspectionMode.current) {
        Text("Phiên bản 1.0.0", style = MaterialTheme.typography.bodySmall)
    } else {
        val common by LocalCommonStore.current.state.collectAsStateWithLifecycle()
        Text("Phiên bản ${common.currentVersion}", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true, name = "Home Screen - Bottom Nav")
@Composable
fun HomeScreenPreview() {
    EdupiaTheme {
        HomeContent(
            state = HomeState(
                selectedTab = HomeTab.HOME,
                profileName = "Nguyễn Văn A",
                phone = "0987654321",
                profileCount = 2,
                activeLicenseCount = 1,
                isLoadingLicenses = false
            ),
            onIntent = {}
        )
    }
}
