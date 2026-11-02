package com.example.fitness.myapplication.core.designsystem.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography

/**
 * Tiện ích load và render ảnh từ thư mục assets/ (PNG, WebP, JPG) trong Jetpack Compose.
 */
@Composable
fun rememberAssetPainter(assetPath: String): Painter? {
    val context = LocalContext.current
    return remember(assetPath) {
        try {
            val inputStream = context.assets.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap?.let { BitmapPainter(it.asImageBitmap()) }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val painter = rememberAssetPainter(assetPath)
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .background(EdupiaColors.Neutral200)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = assetPath.substringAfterLast('/'),
                style = EdupiaTypography.labelSmall,
                color = EdupiaColors.Neutral600
            )
        }
    }
}

@Preview(showBackground = true, name = "AssetImage Preview")
@Composable
fun AssetImagePreview() {
    EdupiaTheme {
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AssetImage(
                assetPath = "icons/sample_icon.png",
                contentDescription = "Sample"
            )
        }
    }
}
