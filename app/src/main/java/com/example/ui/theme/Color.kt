package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Dark Theme Palette for SEARX AI
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF111726)
val DarkSurfaceVariant = Color(0xFF192237)
val DarkSurfaceElevated = Color(0xFF212C45)

// Brand Accents
val NeonCyan = Color(0xFF00E5FF)
val ElectricViolet = Color(0xFF8B5CF6)
val DeepViolet = Color(0xFF6D28D9)
val BrightBlue = Color(0xFF3B82F6)
val VibrantPink = Color(0xFFEC4899)

// Functional Status Colors
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF38BDF8)

// Text & Content Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Border & Divider
val BorderGlow = Color(0x3300E5FF)
val BorderSubtle = Color(0x1F94A3B8)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ElectricViolet, NeonCyan)
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x338B5CF6),
        Color(0x1100E5FF),
        Color(0x05111726)
    )
)

val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(DeepViolet, BrightBlue, NeonCyan)
)

val AudioGradient = Brush.horizontalGradient(
    colors = listOf(VibrantPink, ElectricViolet)
)
