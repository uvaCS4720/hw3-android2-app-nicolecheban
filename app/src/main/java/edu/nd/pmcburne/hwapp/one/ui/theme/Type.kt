package edu.nd.pmcburne.hwapp.one.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = FontFamily.Serif),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = FontFamily.Serif),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = FontFamily.Serif),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Other default text styles to override
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = FontFamily.Serif),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = FontFamily.Serif),
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = FontFamily.Serif),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = FontFamily.Serif),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

