package com.example.fitness.myapplication.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bo góc tokens theo Design System Edupia AI.
 */
object EdupiaShapes {
    val RadiusSmall = 6.dp
    val RadiusMedium = 12.dp
    val RadiusLarge = 16.dp
    val RadiusExtraLarge = 24.dp
    val RadiusPill = 999.dp

    val ShapeSmall = RoundedCornerShape(RadiusSmall)
    val ShapeMedium = RoundedCornerShape(RadiusMedium)
    val ShapeLarge = RoundedCornerShape(RadiusLarge)
    val ShapeExtraLarge = RoundedCornerShape(RadiusExtraLarge)
    val ShapeCircle = CircleShape

    val MaterialShapes = Shapes(
        small = ShapeSmall,
        medium = ShapeMedium,
        large = ShapeLarge,
        extraLarge = ShapeExtraLarge
    )
}
