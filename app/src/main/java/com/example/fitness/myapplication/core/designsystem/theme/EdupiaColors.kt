package com.example.fitness.myapplication.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Bảng màu Design System chuẩn Edupia AI port trực tiếp từ AppColors trong app_core.
 */
object EdupiaColors {

    // Brand Primary & Accent
    val Primary = Color(0xFFF96C49) // Edupia Cam đặc trưng
    val PrimaryHover = Color(0xFFE25533)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00000000)

    // Branding Hues
    val BrandingLightBlue = Color(0xFF2A9D8F)
    val BrandingGreen = Color(0xFF75A366)
    val BrandingYellow = Color(0xFFE9C46A)
    val BrandingOrange = Color(0xFFF4A261)
    val BrandingPink = Color(0xFF00B2B2)
    val BrandingRed = Color(0xFF972A20)
    val BrandBlue = Color(0xFF005DB2)

    // Primary Shades
    val Primary800 = Color(0xFF08416A)
    val Primary700 = Color(0xFF094E7F)
    val Primary600 = Color(0xFF0C6496)
    val Primary500 = Color(0xFF0876B1)
    val Primary400 = Color(0xFF1890C7)
    val Primary300 = Color(0xFF3CB4E7)
    val Primary200 = Color(0xFFCBDFE7)
    val Primary100 = Color(0xFFC6EDFB)
    val Primary50 = Color(0xFFECFBFF)

    // Semantic Status Colors
    val SemanticSuccess = Color(0xFF3AB284)
    val SemanticWarning = Color(0xFFFFA425)
    val SemanticError = Color(0xFFEF444A)
    val SemanticInfo = Color(0xFF0073B1)

    // Neutral Grayscale
    val Neutral900 = Color(0xFF111827)
    val Neutral800 = Color(0xFF1F2937)
    val Neutral700 = Color(0xFF374151)
    val Neutral600 = Color(0xFF4B5563)
    val Neutral500 = Color(0xFF6B7280)
    val Neutral400 = Color(0xFF9CA3AF)
    val Neutral300 = Color(0xFFD1D5DB)
    val Neutral200 = Color(0xFFE5E7EB)
    val Neutral100 = Color(0xFFF3F4F6)
    val Neutral50 = Color(0xFFF9FAFB)

    // Background & Surfaces
    val BackgroundWhite = Color(0xFFFFFFFF)
    val BackgroundGray = Color(0xFFF5F5F5)
    val SurfaceCard = Color(0xFFFFFFFF)
    val StrokeDivider = Color(0xFFEEEEEE)

    // Gradients
    val BrandGradient = Brush.verticalGradient(
        colors = listOf(Primary100, Color(0xFFE7FEFF), White)
    )

    val AiTopicGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x0000192B),
            Color(0xFF01304C)
        )
    )

    val ButtonPrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF7A59), Primary)
    )
}

val LocalEdupiaColors = staticCompositionLocalOf { EdupiaColors }
