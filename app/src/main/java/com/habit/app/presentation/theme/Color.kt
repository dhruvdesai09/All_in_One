package com.habit.app.presentation.theme

import androidx.compose.ui.graphics.Color

// Nexora Brand
val NexoraGold     = Color(0xFFC9A84C)
val NexoraGoldDim  = Color(0xFF7A5F28)
val NexoraMint     = Color(0xFF4ECDC4)
val NexoraRose     = Color(0xFFE07B6C)

// Surfaces (Dark)
val SurfaceDark    = Color(0xFF0D1117)
val SurfaceCard    = Color(0xFF161C26)
val SurfaceElevated= Color(0xFF1E2736)
val SurfaceBorder  = Color(0x12FFFFFF) // ~0.07 alpha

// Text
val TextPrimary    = Color(0xFFEEF0F4)
val TextMuted      = Color(0xFF6B7A96)
val TextSecondary  = TextMuted

// Fallbacks / Legacy mapped
val ForestGreen    = NexoraMint
val AccentEmerald  = NexoraMint
val DangerRed      = NexoraRose
val DangerRedDim   = Color(0xFF4A1010)

// Heatmap cells (Nexora style)
val HeatEmpty      = SurfaceElevated
val HeatNone       = SurfaceBorder
val HeatLow        = NexoraGold.copy(alpha = 0.2f)
val HeatMid        = NexoraGold.copy(alpha = 0.4f)
val HeatMidHigh    = NexoraGold.copy(alpha = 0.65f)
val HeatHigh       = NexoraGold

// Budget category accents
val BudgetBlue     = Color(0xFF1E88E5)
val BudgetAmber    = NexoraGold
val BudgetTeal     = NexoraMint
val BudgetPurple   = Color(0xFF8E24AA)
val BudgetRose     = NexoraRose
val BudgetOrange   = Color(0xFFFF6D00)
val BudgetIndigo   = Color(0xFF3949AB)
val BudgetCyan     = Color(0xFF00ACC1)
