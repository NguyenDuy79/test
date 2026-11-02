package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography

@Composable
fun EdupiaLoading(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = EdupiaColors.Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = EdupiaTypography.bodyMedium,
                    color = EdupiaColors.Neutral600
                )
            }
        }
    }
}

@Composable
fun EdupiaErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚠️",
                style = EdupiaTypography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = EdupiaTypography.bodyMedium,
                color = EdupiaColors.Neutral700,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            EdupiaButton(
                text = "Thử lại",
                onClick = onRetry
            )
        }
    }
}

@Composable
fun EdupiaCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = EdupiaColors.SurfaceCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = EdupiaShapes.ShapeMedium)
            .clip(EdupiaShapes.ShapeMedium)
            .background(backgroundColor)
            .border(width = 1.dp, color = EdupiaColors.StrokeDivider, shape = EdupiaShapes.ShapeMedium)
            .then(clickModifier)
            .padding(16.dp)
    ) {
        content()
    }
}

@Preview(showBackground = true, name = "EdupiaLoading")
@Composable
fun EdupiaLoadingPreview() {
    EdupiaTheme {
        Box(modifier = Modifier.size(300.dp)) {
            EdupiaLoading(message = "Đang tải dữ liệu...")
        }
    }
}

@Preview(showBackground = true, name = "EdupiaErrorView")
@Composable
fun EdupiaErrorViewPreview() {
    EdupiaTheme {
        Box(modifier = Modifier.size(300.dp)) {
            EdupiaErrorView(
                message = "Đã xảy ra lỗi kết nối. Vui lòng kiểm tra lại mạng.",
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "EdupiaCard")
@Composable
fun EdupiaCardPreview() {
    EdupiaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EdupiaCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Thẻ thông tin Edupia",
                        style = EdupiaTypography.titleMedium,
                        color = EdupiaColors.Neutral900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nội dung chi tiết bên trong EdupiaCard với bo góc và viền chuẩn thiết kế.",
                        style = EdupiaTypography.bodyMedium,
                        color = EdupiaColors.Neutral600
                    )
                }
            }
        }
    }
}
