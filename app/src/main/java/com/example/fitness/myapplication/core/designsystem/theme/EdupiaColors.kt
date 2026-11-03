package com.example.fitness.myapplication.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

/**
 * Bảng màu Design System của app, port 1-1 từ:
 * `hybrid_app/packages/app_core/lib/design_system/color/app_colors.dart` (AppColors)
 *
 * Dùng trực tiếp giống hệt Flutter:
 * - [AppColors.primary500], [AppColors.white], [AppColors.brandingPink], [AppColors.semanticRedError]...
 */
object AppColors {

    // region Brand
    // Primary chính của app & theme là primary500 (#0876B1). Màu cam cũ (#F96C49) không dùng làm primary.
    val primary: Color get() = primary500
    val legacyPrimaryOrange = Color(0xFFF96C49)

    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)
    val transparent = Color(0x00000000)

    val brandingLightBlue = Color(0xFF2A9D8F)
    val brandingGreen = Color(0xFF75A366)
    val brandingYellow = Color(0xFFE9C46A)
    val brandingOrange = Color(0xFFF4A261)
    val brandingPink = Color(0xFF00B2B2)
    val brandingDat = Color(0xFFBD550F)
    val brandingRed = Color(0xFF972A20)
    val brandingDoDam = Color(0xFF5C231E)
    val branding300 = Color(0xFF518E0D)

    val brandBlue = Color(0xFF005DB2)
    val brandLightBlue = Color(0xFF36C0C8)

    val brandingColor50 = Color(0xFF3FC2CA)
    val brandingColor100 = Color(0xFF0A5C8F)
    val brandingColor300 = Color(0xFFFFB703)
    val brandingColor400 = Color(0xFFFB8500)
    val brandingColorSupport3200 = Color(0xFFFAE1DD)
    val brandingColorSupport3300 = Color(0xFFF8EDEB)
    val brandingColorSupport3400 = Color(0xFFE8E8E4)
    val brandingColorSupport3500 = Color(0xFFD8E2DC)
    val brandingColorSupport3700 = Color(0xFFFFE5D9)
    val brandingColorSupport3900 = Color(0xFFFEC89A)
    val brandingColorPro300 = Color(0xFFD74E09)
    val brandingColorVip11200 = Color(0xFF005A7D)
    val brandingColorEdupiaColor50 = Color(0xFF3FC2CA)
    // endregion

    // region Primary scale (xanh)
    val primary800 = Color(0xFF08416A)
    val primary700 = Color(0xFF094E7F)
    val primary600 = Color(0xFF0C6496)
    val primary550 = Color(0xFF0073B1)
    val primary500 = Color(0xFF0876B1)
    val primary400 = Color(0xFF1890C7)
    val primary300 = Color(0xFF3CB4E7)
    val primary200 = Color(0xFFCBDFE7)
    val primary150 = Color(0xFF91CAFF)
    val primary100 = Color(0xFFC6EDFB)
    val primary75 = Color(0xFFE6F4FF)
    val primary50 = Color(0xFFECFBFF)
    val primary40 = Color(0xFFE6F9F8)
    val Primary2000F58A8 = Color(0xFF81D6FA)
    // endregion

    // region Success
    val success50 = Color(0xFFF3FDF0)
    val success75 = Color(0xFFDBF7D4)
    val success100 = Color(0xFFDEFCDC)
    val sucess100 = success100

    val success150 = Color(0xFFB3F5D9)
    val success200 = Color(0xFFC0F7BB)
    val success300 = Color(0xFF8FEF86)
    val success400 = Color(0xFF56DE4A)
    val success500 = Color(0xFF40C522)
    val sucess500 = success500

    val success600 = Color(0xFF26A316)
    val success700 = Color(0xFF1E8E5A)
    val success800 = Color(0xFF2C6516)
    val success900 = Color(0xFF205314)
    // endregion

    // region Semantic
    val semanticGreenSuccess = Color(0xFF3AB284)
    val sematicColor = semanticGreenSuccess

    val semanticYellowWaring = Color(0xFFFFA425)
    val semanticYellowWarning = semanticYellowWaring

    val semanticRedError = Color(0xFFEF444A)
    val semanticOrange = Color(0xFFFFA237)
    val infoBlue50 = Color(0xFFEFF6FF)
    // endregion

    // region Warning
    val warning25 = Color(0xFFFFF8E1)
    val warning50 = Color(0xFFFFFCEB)
    val warning100 = Color(0xFFFEF2C7)
    val warning200 = Color(0xFFFDDB8A)
    val warning300 = Color(0xFFFCC74D)
    val warning400 = Color(0xFFFBBE24)
    val warning500 = Color(0xFFF5B30B)
    val warning500Alt = Color(0xFFEAB308)
    val warning600 = Color(0xFFD99606)
    val warning700 = Color(0xFFB47209)
    val warning800 = Color(0xFF92540E)
    val warning900 = Color(0xFF783D0F)
    val warningOrange500 = Color(0xFFFA8C16)
    val warningOrangeAlt = Color(0xFFEA580C)
    // endregion

    // region Destructive
    val destructiveColor25 = Color(0xFFFFF0F0)
    val destructiveColor50 = Color(0xFFFEF2F4)
    val destructiveColor100 = Color(0xFFFEE2E2)
    val destructiveColor200 = Color(0xFFFECACE)
    val destructiveColor250 = Color(0xFFFFB3B3)
    val destructiveColor300 = Color(0xFFFCA5A6)
    val destructiveColor400 = Color(0xFFF87171)
    val destructiveColor500 = Color(0xFFEF444A)
    val destructiveColor550 = Color(0xFFE05353)
    val destructiveColor600 = Color(0xFFDC2632)
    val destructiveColor700 = Color(0xFFB91C1F)
    val destructiceColor800 = Color(0xFF991B1F)
    // endregion

    // region Neutral
    val neutral950 = Color(0xFF1F2230)
    val neutral900 = Color(0xFF111827)
    val neutral800 = Color(0xFF1F2937)
    val neutral700 = Color(0xFF374151)
    val neutral600 = Color(0xFF4B5563)
    val neutral500 = Color(0xFF6B7280)
    val neutral400 = Color(0xFF9CA3AF)
    val neutral300 = Color(0xFFD1D5DB)
    val neutral200 = Color(0xFFE5E7EB)
    val neutral100 = Color(0xFFF3F4F6)
    val neutral75 = Color(0xFFF1F4F8)
    val neutral50 = Color(0xFFF9FAFB)
    val neutral0 = Color(0xFFFFFFFF)
    val neutralSurface = Color(0xFFF6F8FA)

    val neutralN900 = Color(0xFF222222)
    val neutralN800 = Color(0xFF51535F)
    val neutralN700 = Color(0xFF7D808E)
    val neutralN600 = Color(0xFFA3A6B1)
    val neutralN500 = Color(0xFFCDCED6)
    val neutralN400 = Color(0xFFE3E4EC)
    val neutralN200 = Color(0xFFF3F4F5)
    val neutrals4 = Color(0xFF777E90)

    val neutralText800 = Color(0xFF51545F)
    val neutralDark60 = Color(0xFF7A7A7A)
    // endregion

    // region Secondary / accents
    val secondaryColor = Color(0xFFF9D742)
    val secondaryY400 = Color(0xFFF9D742)
    val greenPrePrimary = Color(0xFF86C71C)
    val buttonSecondaryFg = Color(0xFF414651)

    val shadowTextFeild = Color(0xFFE1E1FE)
    val teal500 = Color(0xFF5AD4CC)
    val teal300 = Color(0xFF9FF2E7)
    val mintAccent = Color(0xFF93EDC7)
    val badgeBorderAccent = Color(0xFF7AEEF5)
    val ItemColorSuperRare1 = Color(0xFFFF99FA)

    val sliderTrackDark = Color(0xFF5A4A4D)
    val sliderLabelMuted = Color(0xFF9E8F91)
    // endregion

    // region Background & surfaces
    val backgroundWhite = Color(0xFFFFFFFF)
    val backgroundGray = Color(0xFFF5F5F5)
    val backgroundDesRecord = Color(0xFF01CBCB)
    val stroke = Color(0xFFEEEEEE)
    val bgColorExam = Color(0xFFF3F3F3)
    val filterLesson = Color(0xFFF0F1F3)
    val bgTrainingRoom = Color(0xFFEDEDED)
    val bgCountWhiteBoard = Color(0xFFE0E0E0)
    val bgTitleWhiteBoard = Color(0xFFF95F64)
    // endregion

    // region Feature: Monthly mission
    val monthlyMissionShadowPrimary = Color(0x1A101828)
    val monthlyMissionShadowSecondary = Color(0x0D101828)
    val monthlyMissionTheoryIconBackground = Color(0xFF94D2BD)
    val monthlyMissionDecorLavender = Color(0xFFE5CCFD)
    val monthlyMissionDecorPink = Color(0xFFFECACE)
    val monthlyMissionDecorGreen = Color(0xFFC0F7BB)
    val monthlyMissionMonthBadgeBackground = Color(0xFFFEE6FC)
    val monthlyMissionButtonShadow = Color(0x0A101828)
    // endregion

    // region Feature: NoName exercise
    val noNameExerciseBanner = Color(0xFF00203D)
    val noNameIconListen = Color(0xFF1E5936)
    val noNameIconSpeak = Color(0xFF66352D)
    val noNameIconRead = Color(0xFF52521D)
    val noNameIconWrite = Color(0xFF6C3B32)
    val noNameBackGroundLoadingAISpeak = Color(0xFF00203D)
    // endregion

    // region Feature: Ambassador
    val ambassadorCyanLight = Color(0xFF81D6FA)
    val ambassadorNeutralText = Color(0xFFF9F9F9)
    val ambassadorRewardText = Color(0xFFFDFBF2)
    val ambassadorGoldOverlay = Color(0x4DFFB236)
    val ambassadorTrackGreen = Color(0xFFD2DDC3)
    val ambassadorTrackBlue = Color(0xFFC3D5DD)
    val ambassadorTrackGold = Color(0xFFEEDDC5)
    val ambassadorFooterTextBlue = Color(0xFF334155)
    val ambassadorLockedChipText = Color(0xFF7F1D2A)
    // endregion

    // region Gradients
    private const val CardBeginX = -1.118f
    private const val CardBeginY = 1.267f
    private const val CardEndX = 1.051f
    private const val CardEndY = -1.192f

    val ambassadorCardGreenGradient = alignedLinearGradient(
        listOf(Color(0xFF2C6516), Color(0xFF62B95A), Color(0xFFD1EBCF)),
        listOf(0.1838f, 0.6437f, 0.9839f),
        CardBeginX, CardBeginY, CardEndX, CardEndY,
    )

    val ambassadorCardBlueGradient = alignedLinearGradient(
        listOf(Color(0xFF1685C0), Color(0xFF44ADDD), Color(0xFF81D6FA)),
        listOf(0.2093f, 0.6768f, 0.9839f),
        CardBeginX, CardBeginY, CardEndX, CardEndY,
    )

    val ambassadorCardGoldGradient = alignedLinearGradient(
        listOf(Color(0xFFAC7E3A), Color(0xFFD2AF7F), Color(0xFFEEB975)),
        listOf(0.2093f, 0.6768f, 0.9839f),
        CardBeginX, CardBeginY, CardEndX, CardEndY,
    )

    val ambassadorCardLockedGradient = alignedLinearGradient(
        listOf(Color(0xFF8C1A1A), Color(0xFFCA4D4D), Color(0xFFD99494)),
        listOf(0.1838f, 0.6437f, 0.9839f),
        CardBeginX, CardBeginY, CardEndX, CardEndY,
    )

    val ambassadorBorderGreenGradient = alignedLinearGradient(
        listOf(Color(0xFF2D571C), Color(0xFF62B95A), Color(0xFFE9FFE8)),
        listOf(0.0f, 0.6437f, 0.9839f),
        -1.188f, 1.670f, 0.965f, -1.356f,
    )

    val ambassadorBorderBlueGradient = alignedLinearGradient(
        listOf(Color(0xFF30A4E3), Color(0xFF44ADDD), Color(0xFF8DDDFF)),
        listOf(0.2093f, 0.5308f, 0.8954f),
        -0.992f, -0.217f, 1.138f, 0.249f,
    )

    val ambassadorBorderGoldGradient = alignedLinearGradient(
        listOf(Color(0xFFFAD6A6), Color(0xFFD2AF7F), Color(0xFFD3A664)),
        listOf(0.0161f, 0.3232f, 0.7907f),
        CardBeginX, CardBeginY, CardEndX, CardEndY,
    )

    val ambassadorBorderLockedGradient = alignedLinearGradient(
        listOf(Color(0xFF9E1E1E), Color(0xFFEE6E6E), Color(0xFFFFF2F2)),
        listOf(0.0f, 0.6437f, 0.9839f),
        -1.188f, 1.670f, 0.965f, -1.356f,
    )

    val ambassadorChevronCircleGradient = Brush.verticalGradient(
        listOf(Color(0xCCFFFFFF), Color(0x66FFFFFF))
    )

    val ambassadorFooterGradient = alignedLinearGradient(
        listOf(Color(0xB3FFFFFF), Color(0xC9FFFFFF), Color(0xFFFFFFFF)),
        listOf(0.016f, 0.483f, 1.0f),
        -0.995f, -0.697f, 1.118f, 0.784f,
    )

    val ambassadorRateChipGradient = alignedLinearGradient(
        listOf(Color(0xFFE6A953), Color(0xFFBF7C1A), Color(0xFFDE8A2B)),
        listOf(0.103f, 0.580f, 0.951f),
        -0.806f, -0.944f, 0.878f, 1.028f,
    )

    val ambassadorRestoreChipGradient = alignedLinearGradient(
        listOf(Color(0xFFB2B2B2), Color(0xFF858585), Color(0xFF999999), Color(0xFFB2B2B2)),
        listOf(0.120f, 0.361f, 0.705f, 0.971f),
        -0.353f, -3.747f, 0.315f, 3.348f,
    )

    val ambassadorLockedChipGradient = alignedLinearGradient(
        listOf(Color(0xFFCA231D), Color(0xFFFE7E82), Color(0xFFFEF2F4)),
        listOf(0.029f, 0.476f, 0.897f),
        0.075f, 1.871f, -0.049f, -1.231f,
    )

    val ambassadorLockedButtonBorderGradient = alignedLinearGradient(
        listOf(Color(0xFFCA1212), Color(0xFFDD4444), Color(0xFFFA8181)),
        listOf(0.209f, 0.531f, 0.895f),
        -0.010f, 1.314f, 0.017f, -2.359f,
    )

    val gradientMesh14 = Brush.horizontalGradient(listOf(Color(0xFFD70F4D), Color(0xFFFCB6A1)))
    val gradientSpeakAI = Brush.verticalGradient(listOf(Color(0xFFC2FBFF), Color(0xFFFFFFFF)))
    val gradientAISpeakVideoCall = Brush.verticalGradient(
        listOf(
            noNameExerciseBanner.copy(alpha = 0f),
            noNameExerciseBanner.copy(alpha = 0.6f),
            noNameExerciseBanner,
        )
    )
    val gradientPracticeScreen = Brush.verticalGradient(
        listOf(Color(0xFFEAFAF9), Color(0xFFF8F8F8), Color(0xFFF8F8F8))
    )
    val listLinearGradient: List<Brush> = listOf(
        Brush.linearGradient(listOf(Color(0xFFEAFF35), Color(0xFFEB43FF))),
        Brush.linearGradient(listOf(Color(0xFF7BC5F5), Color(0xFF787FF6))),
        Brush.linearGradient(listOf(Color(0xFF35FF83), Color(0xFF0C80ED))),
        Brush.linearGradient(listOf(Color(0xFFB2FF6F), Color(0xFF09A74D))),
        Brush.linearGradient(listOf(Color(0xFFFF8635), Color(0xFF6F43FF))),
    )

    val gradientEndLesson = Brush.verticalGradient(
        listOf(Color(0xFFECFBFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF))
    )
    val gradientEndLession = gradientEndLesson

    val gradient1 = Brush.verticalGradient(listOf(Color(0xFFFFBB7C), Color(0xFFED7331)))
    val gradient2 = Brush.horizontalGradient(listOf(Color(0xFF4AEBE1), Color(0xFF009BA3)))
    val gradient3 = Brush.horizontalGradient(listOf(Color(0xFFFFD661), Color(0xFFFBBC0A)))
    val gradient4 = Brush.horizontalGradient(listOf(Color(0xFF39AFFD), Color(0xFF477FFF)))
    val gradientExercise = Brush.verticalGradient(
        listOf(Color(0xFFFBEDDE), Color(0xFFF8F8F8), Color(0xFFF8F8F8))
    )
    val gradientClassroom = Brush.verticalGradient(listOf(Color(0xFFFEE8D1), Color(0xFFF5F6F8)))
    val gradientAITopic = Brush.verticalGradient(
        listOf(Color(0xFFEAFAF9), Color(0xFFF8F8F8), Color(0xFFF8F8F8))
    )
    val gradientContest = Brush.linearGradient(listOf(Color(0xFF35FF83), Color(0xFF0C80ED)))
    val radialGradient = Brush.verticalGradient(listOf(Color(0xFF040404), Color(0xFF212020)))
    val gradientHomeClub = Brush.verticalGradient(listOf(primary100, Color(0xFFE7FEFF), white))
    val aiTopicGradient = Brush.verticalGradient(
        listOf(Color(0xFF00192B).copy(alpha = 0f), Color(0xFF01304C))
    )

    val buttonPrimaryGradient = Brush.horizontalGradient(listOf(primary400, primary500))
    // endregion
}

/**
 * Đối tượng màu Compose (PascalCase), giữ tương thích 100% với toàn bộ code Android hiện tại.
 */
object EdupiaColors {

    // region Brand
    val Primary: Color get() = AppColors.primary500
    val LegacyPrimaryOrange: Color get() = AppColors.legacyPrimaryOrange
    val White: Color get() = AppColors.white
    val Black: Color get() = AppColors.black
    val Transparent: Color get() = AppColors.transparent

    val BrandingLightBlue: Color get() = AppColors.brandingLightBlue
    val BrandingGreen: Color get() = AppColors.brandingGreen
    val BrandingYellow: Color get() = AppColors.brandingYellow
    val BrandingOrange: Color get() = AppColors.brandingOrange
    val BrandingPink: Color get() = AppColors.brandingPink
    val BrandingDat: Color get() = AppColors.brandingDat
    val BrandingRed: Color get() = AppColors.brandingRed
    val BrandingDoDam: Color get() = AppColors.brandingDoDam
    val Branding300: Color get() = AppColors.branding300

    val BrandBlue: Color get() = AppColors.brandBlue
    val BrandLightBlue: Color get() = AppColors.brandLightBlue

    val BrandingColor50: Color get() = AppColors.brandingColor50
    val BrandingColor100: Color get() = AppColors.brandingColor100
    val BrandingColor300: Color get() = AppColors.brandingColor300
    val BrandingColor400: Color get() = AppColors.brandingColor400
    val BrandingColorSupport3200: Color get() = AppColors.brandingColorSupport3200
    val BrandingColorSupport3300: Color get() = AppColors.brandingColorSupport3300
    val BrandingColorSupport3400: Color get() = AppColors.brandingColorSupport3400
    val BrandingColorSupport3500: Color get() = AppColors.brandingColorSupport3500
    val BrandingColorSupport3700: Color get() = AppColors.brandingColorSupport3700
    val BrandingColorSupport3900: Color get() = AppColors.brandingColorSupport3900
    val BrandingColorPro300: Color get() = AppColors.brandingColorPro300
    val BrandingColorVip11200: Color get() = AppColors.brandingColorVip11200
    val BrandingColorEdupiaColor50: Color get() = AppColors.brandingColorEdupiaColor50
    // endregion

    // region Primary scale (xanh)
    val Primary800: Color get() = AppColors.primary800
    val Primary700: Color get() = AppColors.primary700
    val Primary600: Color get() = AppColors.primary600
    val Primary550: Color get() = AppColors.primary550
    val Primary500: Color get() = AppColors.primary500
    val Primary400: Color get() = AppColors.primary400
    val Primary300: Color get() = AppColors.primary300
    val Primary200: Color get() = AppColors.primary200
    val Primary150: Color get() = AppColors.primary150
    val Primary100: Color get() = AppColors.primary100
    val Primary75: Color get() = AppColors.primary75
    val Primary50: Color get() = AppColors.primary50
    val Primary40: Color get() = AppColors.primary40
    val Primary2000F58A8: Color get() = AppColors.Primary2000F58A8
    // endregion

    // region Success
    val Success50: Color get() = AppColors.success50
    val Success75: Color get() = AppColors.success75
    val Success100: Color get() = AppColors.success100
    val Success150: Color get() = AppColors.success150
    val Success200: Color get() = AppColors.success200
    val Success300: Color get() = AppColors.success300
    val Success400: Color get() = AppColors.success400
    val Success500: Color get() = AppColors.success500
    val Success600: Color get() = AppColors.success600
    val Success700: Color get() = AppColors.success700
    val Success800: Color get() = AppColors.success800
    val Success900: Color get() = AppColors.success900
    // endregion

    // region Semantic
    val SemanticSuccess: Color get() = AppColors.semanticGreenSuccess
    val SemanticWarning: Color get() = AppColors.semanticYellowWaring
    val SemanticError: Color get() = AppColors.semanticRedError
    val SemanticOrange: Color get() = AppColors.semanticOrange
    val SemanticInfo: Color get() = AppColors.primary550
    val InfoBlue50: Color get() = AppColors.infoBlue50
    // endregion

    // region Warning
    val Warning25: Color get() = AppColors.warning25
    val Warning50: Color get() = AppColors.warning50
    val Warning100: Color get() = AppColors.warning100
    val Warning200: Color get() = AppColors.warning200
    val Warning300: Color get() = AppColors.warning300
    val Warning400: Color get() = AppColors.warning400
    val Warning500: Color get() = AppColors.warning500
    val Warning500Alt: Color get() = AppColors.warning500Alt
    val Warning600: Color get() = AppColors.warning600
    val Warning700: Color get() = AppColors.warning700
    val Warning800: Color get() = AppColors.warning800
    val Warning900: Color get() = AppColors.warning900
    val WarningOrange500: Color get() = AppColors.warningOrange500
    val WarningOrangeAlt: Color get() = AppColors.warningOrangeAlt
    // endregion

    // region Destructive
    val Destructive25: Color get() = AppColors.destructiveColor25
    val Destructive50: Color get() = AppColors.destructiveColor50
    val Destructive100: Color get() = AppColors.destructiveColor100
    val Destructive200: Color get() = AppColors.destructiveColor200
    val Destructive250: Color get() = AppColors.destructiveColor250
    val Destructive300: Color get() = AppColors.destructiveColor300
    val Destructive400: Color get() = AppColors.destructiveColor400
    val Destructive500: Color get() = AppColors.destructiveColor500
    val Destructive550: Color get() = AppColors.destructiveColor550
    val Destructive600: Color get() = AppColors.destructiveColor600
    val Destructive700: Color get() = AppColors.destructiveColor700
    val Destructive800: Color get() = AppColors.destructiceColor800
    // endregion

    // region Neutral
    val Neutral950: Color get() = AppColors.neutral950
    val Neutral900: Color get() = AppColors.neutral900
    val Neutral800: Color get() = AppColors.neutral800
    val Neutral700: Color get() = AppColors.neutral700
    val Neutral600: Color get() = AppColors.neutral600
    val Neutral500: Color get() = AppColors.neutral500
    val Neutral400: Color get() = AppColors.neutral400
    val Neutral300: Color get() = AppColors.neutral300
    val Neutral200: Color get() = AppColors.neutral200
    val Neutral100: Color get() = AppColors.neutral100
    val Neutral75: Color get() = AppColors.neutral75
    val Neutral50: Color get() = AppColors.neutral50
    val Neutral0: Color get() = AppColors.neutral0
    val NeutralSurface: Color get() = AppColors.neutralSurface

    val NeutralN900: Color get() = AppColors.neutralN900
    val NeutralN800: Color get() = AppColors.neutralN800
    val NeutralN700: Color get() = AppColors.neutralN700
    val NeutralN600: Color get() = AppColors.neutralN600
    val NeutralN500: Color get() = AppColors.neutralN500
    val NeutralN400: Color get() = AppColors.neutralN400
    val NeutralN200: Color get() = AppColors.neutralN200
    val Neutrals4: Color get() = AppColors.neutrals4

    val NeutralText800: Color get() = AppColors.neutralText800
    val NeutralDark60: Color get() = AppColors.neutralDark60
    // endregion

    // region Secondary / accents
    val SecondaryColor: Color get() = AppColors.secondaryColor
    val SecondaryY400: Color get() = AppColors.secondaryY400
    val GreenPrePrimary: Color get() = AppColors.greenPrePrimary
    val ButtonSecondaryFg: Color get() = AppColors.buttonSecondaryFg
    val ShadowTextField: Color get() = AppColors.shadowTextFeild
    val Teal500: Color get() = AppColors.teal500
    val Teal300: Color get() = AppColors.teal300
    val MintAccent: Color get() = AppColors.mintAccent
    val BadgeBorderAccent: Color get() = AppColors.badgeBorderAccent
    val ItemColorSuperRare1: Color get() = AppColors.ItemColorSuperRare1

    val SliderTrackDark: Color get() = AppColors.sliderTrackDark
    val SliderLabelMuted: Color get() = AppColors.sliderLabelMuted
    // endregion

    // region Background & surfaces
    val BackgroundWhite: Color get() = AppColors.backgroundWhite
    val BackgroundGray: Color get() = AppColors.backgroundGray
    val BackgroundDesRecord: Color get() = AppColors.backgroundDesRecord
    val Stroke: Color get() = AppColors.stroke
    val BgColorExam: Color get() = AppColors.bgColorExam
    val FilterLesson: Color get() = AppColors.filterLesson
    val BgTrainingRoom: Color get() = AppColors.bgTrainingRoom
    val BgCountWhiteBoard: Color get() = AppColors.bgCountWhiteBoard
    val BgTitleWhiteBoard: Color get() = AppColors.bgTitleWhiteBoard

    val SurfaceCard: Color get() = AppColors.backgroundWhite
    val StrokeDivider: Color get() = AppColors.stroke
    // endregion

    // region Feature: Monthly mission
    val MonthlyMissionShadowPrimary: Color get() = AppColors.monthlyMissionShadowPrimary
    val MonthlyMissionShadowSecondary: Color get() = AppColors.monthlyMissionShadowSecondary
    val MonthlyMissionTheoryIconBackground: Color get() = AppColors.monthlyMissionTheoryIconBackground
    val MonthlyMissionDecorLavender: Color get() = AppColors.monthlyMissionDecorLavender
    val MonthlyMissionDecorPink: Color get() = AppColors.monthlyMissionDecorPink
    val MonthlyMissionDecorGreen: Color get() = AppColors.monthlyMissionDecorGreen
    val MonthlyMissionMonthBadgeBackground: Color get() = AppColors.monthlyMissionMonthBadgeBackground
    val MonthlyMissionButtonShadow: Color get() = AppColors.monthlyMissionButtonShadow
    // endregion

    // region Feature: NoName exercise
    val NoNameExerciseBanner: Color get() = AppColors.noNameExerciseBanner
    val NoNameIconListen: Color get() = AppColors.noNameIconListen
    val NoNameIconSpeak: Color get() = AppColors.noNameIconSpeak
    val NoNameIconRead: Color get() = AppColors.noNameIconRead
    val NoNameIconWrite: Color get() = AppColors.noNameIconWrite
    val NoNameBackGroundLoadingAISpeak: Color get() = AppColors.noNameBackGroundLoadingAISpeak
    // endregion

    // region Feature: Ambassador
    val AmbassadorCyanLight: Color get() = AppColors.ambassadorCyanLight
    val AmbassadorNeutralText: Color get() = AppColors.ambassadorNeutralText
    val AmbassadorRewardText: Color get() = AppColors.ambassadorRewardText
    val AmbassadorGoldOverlay: Color get() = AppColors.ambassadorGoldOverlay
    val AmbassadorTrackGreen: Color get() = AppColors.ambassadorTrackGreen
    val AmbassadorTrackBlue: Color get() = AppColors.ambassadorTrackBlue
    val AmbassadorTrackGold: Color get() = AppColors.ambassadorTrackGold
    val AmbassadorFooterTextBlue: Color get() = AppColors.ambassadorFooterTextBlue
    val AmbassadorLockedChipText: Color get() = AppColors.ambassadorLockedChipText
    // endregion

    // region Gradients
    val AmbassadorCardGreenGradient: Brush get() = AppColors.ambassadorCardGreenGradient
    val AmbassadorCardBlueGradient: Brush get() = AppColors.ambassadorCardBlueGradient
    val AmbassadorCardGoldGradient: Brush get() = AppColors.ambassadorCardGoldGradient
    val AmbassadorCardLockedGradient: Brush get() = AppColors.ambassadorCardLockedGradient
    val AmbassadorBorderGreenGradient: Brush get() = AppColors.ambassadorBorderGreenGradient
    val AmbassadorBorderBlueGradient: Brush get() = AppColors.ambassadorBorderBlueGradient
    val AmbassadorBorderGoldGradient: Brush get() = AppColors.ambassadorBorderGoldGradient
    val AmbassadorBorderLockedGradient: Brush get() = AppColors.ambassadorBorderLockedGradient
    val AmbassadorChevronCircleGradient: Brush get() = AppColors.ambassadorChevronCircleGradient
    val AmbassadorFooterGradient: Brush get() = AppColors.ambassadorFooterGradient
    val AmbassadorRateChipGradient: Brush get() = AppColors.ambassadorRateChipGradient
    val AmbassadorRestoreChipGradient: Brush get() = AppColors.ambassadorRestoreChipGradient
    val AmbassadorLockedChipGradient: Brush get() = AppColors.ambassadorLockedChipGradient
    val AmbassadorLockedButtonBorderGradient: Brush get() = AppColors.ambassadorLockedButtonBorderGradient

    val GradientMesh14: Brush get() = AppColors.gradientMesh14
    val GradientSpeakAI: Brush get() = AppColors.gradientSpeakAI
    val GradientAISpeakVideoCall: Brush get() = AppColors.gradientAISpeakVideoCall
    val GradientPracticeScreen: Brush get() = AppColors.gradientPracticeScreen
    val ListLinearGradient: List<Brush> get() = AppColors.listLinearGradient

    val GradientEndLesson: Brush get() = AppColors.gradientEndLesson
    val Gradient1: Brush get() = AppColors.gradient1
    val Gradient2: Brush get() = AppColors.gradient2
    val Gradient3: Brush get() = AppColors.gradient3
    val Gradient4: Brush get() = AppColors.gradient4
    val GradientExercise: Brush get() = AppColors.gradientExercise
    val GradientClassroom: Brush get() = AppColors.gradientClassroom
    val GradientAITopic: Brush get() = AppColors.gradientAITopic
    val GradientContest: Brush get() = AppColors.gradientContest
    val RadialGradient: Brush get() = AppColors.radialGradient
    val GradientHomeClub: Brush get() = AppColors.gradientHomeClub
    val AiTopicGradient: Brush get() = AppColors.aiTopicGradient

    val BrandGradient: Brush get() = AppColors.gradientHomeClub
    val ButtonPrimaryGradient: Brush get() = AppColors.buttonPrimaryGradient
    // endregion
}

/**
 * Tương đương Flutter `LinearGradient(begin: Alignment(bx, by), end: Alignment(ex, ey))`:
 * toạ độ Alignment (-1..1, có thể vượt) được quy đổi theo kích thước vùng vẽ.
 */
private fun alignedLinearGradient(
    colors: List<Color>,
    stops: List<Float>,
    beginX: Float,
    beginY: Float,
    endX: Float,
    endY: Float,
): Brush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        fun point(x: Float, y: Float) =
            Offset((x + 1f) / 2f * size.width, (y + 1f) / 2f * size.height)
        return LinearGradientShader(
            from = point(beginX, beginY),
            to = point(endX, endY),
            colors = colors,
            colorStops = stops,
        )
    }
}

val LocalAppColors = staticCompositionLocalOf { AppColors }
val LocalEdupiaColors = staticCompositionLocalOf { EdupiaColors }
