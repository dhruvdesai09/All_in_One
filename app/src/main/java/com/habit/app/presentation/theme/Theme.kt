package com.habit.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary            = ForestGreenLight,
    onPrimary          = Color(0xFF051005),
    primaryContainer   = ForestGreenDim,
    onPrimaryContainer = Color(0xFFC8F5C9),
    secondary          = AccentEmerald,
    onSecondary        = Color(0xFF002108),
    surface            = SurfaceDark,
    surfaceVariant     = SurfaceCard,
    onSurface          = TextPrimary,
    onSurfaceVariant   = TextSecondary,
    surfaceContainerHigh = SurfaceElevated,
    outline            = SurfaceBorder,
    outlineVariant     = Color(0xFF1E2A3A),
    error              = DangerRed,
    onError            = Color(0xFFFFFFFF),
    errorContainer     = DangerRedDim,
    onErrorContainer   = Color(0xFFFFB4A9),
    background         = SurfaceDark,
    onBackground       = TextPrimary,
)

private val LightColors = lightColorScheme(
    primary            = ForestGreen,
    onPrimary          = Color(0xFFFFFFFF),
    surface            = Color(0xFFF4FAF4),
    onSurface          = Color(0xFF1A1C1B),
    surfaceVariant     = Color(0xFFDCE5DC),
    onSurfaceVariant   = Color(0xFF40504A),
    outline            = Color(0xFF7A8C80),
    background         = Color(0xFFF4FAF4),
    onBackground       = Color(0xFF1A1C1B),
)

private val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun HabitTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) DarkColors else LightColors
    MaterialTheme(
        colorScheme = scheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content,
    )
}
