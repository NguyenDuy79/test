package com.example.fitness.myapplication.feature.auth.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.component.EdupiaTopBar
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SignUpEffect.NavigateToLogin -> onNavigateToLogin()
                is SignUpEffect.NavigateToHome -> onNavigateToHome()
                is SignUpEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SignUpContent(
        state = state,
        onIntent = viewModel::sendIntent,
        onBack = onNavigateToLogin
    )
}

@Composable
fun SignUpContent(
    state: SignUpState,
    onIntent: (SignUpIntent) -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            EdupiaTopBar(
                title = "Tạo Tài Khoản",
                onBackClick = onBack
            )
        },
        containerColor = EdupiaColors.BackgroundWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Đăng Ký Tài Khoản Edupia",
                style = EdupiaTypography.headlineSmall,
                color = EdupiaColors.Neutral900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Điền thông tin để bắt đầu học tiếng Anh chuẩn quốc tế",
                style = EdupiaTypography.bodyMedium,
                color = EdupiaColors.Neutral600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EdupiaShapes.ShapeMedium)
                        .background(EdupiaColors.SemanticError.copy(alpha = 0.1f))
                        .border(1.dp, EdupiaColors.SemanticError.copy(alpha = 0.3f), EdupiaShapes.ShapeMedium)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = state.errorMessage,
                        style = EdupiaTypography.bodySmall,
                        color = EdupiaColors.SemanticError
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = state.phone,
                onValueChange = { onIntent(SignUpIntent.PhoneChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại") },
                placeholder = { Text("09xxxxxxxx") },
                singleLine = true,
                shape = EdupiaShapes.ShapeMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdupiaColors.Primary,
                    unfocusedBorderColor = EdupiaColors.Neutral300,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.fullName,
                onValueChange = { onIntent(SignUpIntent.FullNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Họ và tên của bé hoặc Phụ huynh") },
                placeholder = { Text("Nguyễn Văn A") },
                singleLine = true,
                shape = EdupiaShapes.ShapeMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdupiaColors.Primary,
                    unfocusedBorderColor = EdupiaColors.Neutral300,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(SignUpIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mật khẩu") },
                placeholder = { Text("Tối thiểu 6 ký tự") },
                singleLine = true,
                shape = EdupiaShapes.ShapeMedium,
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { onIntent(SignUpIntent.TogglePasswordVisibility) }) {
                        Text(
                            text = if (state.isPasswordVisible) "🙈" else "👁",
                            fontSize = 16.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdupiaColors.Primary,
                    unfocusedBorderColor = EdupiaColors.Neutral300,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(SignUpIntent.ConfirmPasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Xác nhận mật khẩu") },
                placeholder = { Text("Nhập lại mật khẩu") },
                singleLine = true,
                shape = EdupiaShapes.ShapeMedium,
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (state.canSubmit) onIntent(SignUpIntent.SubmitSignUp)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdupiaColors.Primary,
                    unfocusedBorderColor = EdupiaColors.Neutral300,
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            EdupiaButton(
                text = "TẠO TÀI KHOẢN",
                onClick = {
                    focusManager.clearFocus()
                    onIntent(SignUpIntent.SubmitSignUp)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Đã có tài khoản Edupia? ",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
                Text(
                    text = "Đăng nhập ngay",
                    style = EdupiaTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Primary,
                    modifier = Modifier.clickable { onIntent(SignUpIntent.NavigateToLogin) }
                )
            }
        }
    }
}
