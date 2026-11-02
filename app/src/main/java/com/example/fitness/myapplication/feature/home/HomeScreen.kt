package com.example.fitness.myapplication.feature.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness.myapplication.core.appstate.LocalCommonStore
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HomeContent(state = state, onIntent = viewModel::sendIntent)
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    Scaffold(containerColor = EdupiaColors.BackgroundWhite) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Xin chào, ${state.profileName}", style = MaterialTheme.typography.headlineSmall)
            Text("SĐT: ${state.phone}")
            Text("Số hồ sơ: ${state.profileCount}")
            Text(
                if (state.isLoadingLicenses) "Đang tải gói học…"
                else "Gói học đang hoạt động: ${state.activeLicenseCount}"
            )
            AppVersionLabel()

            EdupiaButton(
                text = "Tải lại thông tin",
                onClick = { onIntent(HomeIntent.Refresh) },
                modifier = Modifier.fillMaxWidth(),
            )
            EdupiaButton(
                text = "Đăng xuất",
                onClick = { onIntent(HomeIntent.SignOut) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Ví dụ widget nhỏ đọc thẳng state app-wide qua CompositionLocal
 * (≈ `BlocBuilder<CommonBloc, CommonState>` ngay trong widget), không cần ViewModel.
 */
@Composable
private fun AppVersionLabel() {
    val common by LocalCommonStore.current.state.collectAsStateWithLifecycle()
    Text("Phiên bản ${common.currentVersion}", style = MaterialTheme.typography.bodySmall)
}
