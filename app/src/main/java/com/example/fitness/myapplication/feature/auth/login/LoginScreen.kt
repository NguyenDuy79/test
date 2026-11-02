package com.example.fitness.myapplication.feature.auth.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.component.EdupiaButton
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                is LoginEffect.NavigateToSignUp -> onNavigateToSignUp()
                is LoginEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                is LoginEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LoginContent(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
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
            Spacer(modifier = Modifier.height(24.dp))

            // Banner hình ảnh thương hiệu Edupia
            Image(
                painter = painterResource(id = R.drawable.image_login),
                contentDescription = "Edupia Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(EdupiaShapes.ShapeLarge),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tiêu đề & Lời chào
            Text(
                text = "Đăng Nhập",
                style = EdupiaTypography.headlineMedium,
                color = EdupiaColors.Neutral900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cùng Edupia AI khám phá hành trình học tiếng Anh thông minh!",
                style = EdupiaTypography.bodyMedium,
                color = EdupiaColors.Neutral600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thông báo lỗi nếu có
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

            // Trường nhập Số điện thoại
            OutlinedTextField(
                value = state.phone,
                onValueChange = { onIntent(LoginIntent.PhoneChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại hoặc Tài khoản") },
                placeholder = { Text("Nhập số điện thoại đã đăng ký") },
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
                    focusedLabelColor = EdupiaColors.Primary,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trường nhập Mật khẩu
            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mật khẩu") },
                placeholder = { Text("Nhập mật khẩu của bạn") },
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
                        if (state.canSubmit) onIntent(LoginIntent.SubmitLogin)
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { onIntent(LoginIntent.TogglePasswordVisibility) }) {
                        Text(
                            text = if (state.isPasswordVisible) "🙈" else "👁",
                            fontSize = 16.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdupiaColors.Primary,
                    unfocusedBorderColor = EdupiaColors.Neutral300,
                    focusedLabelColor = EdupiaColors.Primary,
                )
            )

            // Quên mật khẩu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    style = EdupiaTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = EdupiaColors.Primary,
                    modifier = Modifier.clickable { onIntent(LoginIntent.NavigateToForgotPassword) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập chính
            EdupiaButton(
                text = "ĐĂNG NHẬP",
                onClick = {
                    focusManager.clearFocus()
                    onIntent(LoginIntent.SubmitLogin)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Đường phân cách đăng nhập MXH
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = EdupiaColors.Neutral200
                )
                Text(
                    text = "  Hoặc tiếp tục với  ",
                    style = EdupiaTypography.bodySmall,
                    color = EdupiaColors.Neutral500
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = EdupiaColors.Neutral200
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialLoginItem(
                    iconRes = R.drawable.google,
                    name = "Google",
                    onClick = { onIntent(LoginIntent.SocialLogin("Google")) }
                )

                Spacer(modifier = Modifier.width(20.dp))

                SocialLoginItem(
                    iconRes = R.drawable.facebook,
                    name = "Facebook",
                    onClick = { onIntent(LoginIntent.SocialLogin("Facebook")) }
                )

                Spacer(modifier = Modifier.width(20.dp))

                SocialLoginItem(
                    iconRes = R.drawable.zalo,
                    name = "Zalo",
                    onClick = { onIntent(LoginIntent.SocialLogin("Zalo")) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer chuyển sang Đăng ký
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Chưa có tài khoản Edupia? ",
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
                Text(
                    text = "Đăng ký ngay",
                    style = EdupiaTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = EdupiaColors.Primary,
                    modifier = Modifier.clickable { onIntent(LoginIntent.NavigateToSignUp) }
                )
            }
        }
    }
}

@Composable
private fun SocialLoginItem(
    iconRes: Int,
    name: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .border(1.dp, EdupiaColors.Neutral200, CircleShape)
            .background(EdupiaColors.BackgroundWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = name,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    EdupiaTheme {
        LoginContent(state = LoginState()) { }
    }
}

@Preview(showBackground = true, name = "Login Screen - Error")
@Composable
fun LoginScreenErrorPreview() {
    EdupiaTheme {
        LoginContent(
            state = LoginState(
                phone = "0987654321",
                password = "wrongpassword",
                errorMessage = "Số điện thoại hoặc mật khẩu không chính xác"
            )
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowLoginScreen() {
    LoginScreenPreview()
}
