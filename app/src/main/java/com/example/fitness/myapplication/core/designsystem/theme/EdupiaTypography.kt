package com.example.fitness.myapplication.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fitness.myapplication.R

/**
 * Cấu hình FontFamily & Typography của Edupia AI.
 */
object EdupiaFonts {
    val Poppins = FontFamily(
        Font(R.font.fzpoppins_regular, FontWeight.Normal),
        Font(R.font.fzpoppins_medium, FontWeight.Medium),
        Font(R.font.fzpoppins_semibold, FontWeight.SemiBold),
        Font(R.font.fzpoppins_bold, FontWeight.Bold)
    )

    val FredokaOne = FontFamily(
        Font(R.font.fredokaone_regular, FontWeight.Normal)
    )

    val Quicksand = FontFamily(
        Font(R.font.quicksand, FontWeight.Normal)
    )
}

val EdupiaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = EdupiaColors.Neutral900
    ),
    headlineMedium = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = EdupiaColors.Neutral900
    ),
    headlineSmall = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = EdupiaColors.Neutral900
    ),
    titleLarge = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = EdupiaColors.Neutral900
    ),
    titleMedium = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = EdupiaColors.Neutral900
    ),
    titleSmall = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = EdupiaColors.Neutral800
    ),
    bodyLarge = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = EdupiaColors.Neutral800
    ),
    bodyMedium = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = EdupiaColors.Neutral700
    ),
    bodySmall = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = EdupiaColors.Neutral600
    ),
    labelLarge = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = EdupiaColors.White
    ),
    labelMedium = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = EdupiaColors.Neutral600
    ),
    labelSmall = TextStyle(
        fontFamily = EdupiaFonts.Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = EdupiaColors.Neutral500
    )
)
