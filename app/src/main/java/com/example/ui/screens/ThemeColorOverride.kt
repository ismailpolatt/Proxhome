package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.isAppInDarkTheme

/**
 * Custom package-level Color override to support dynamic light/dark theme transition
 * on legacy hardcoded color values.
 */
fun Color(colorValue: Long): Color {
    if (isAppInDarkTheme) {
        return Color(colorValue)
    }
    
    val mappedValue = when (colorValue) {
        // Main backgrounds (deep dark -> slate light)
        0xFF020617L -> 0xFFF8FAFC // slate 50
        0xFF030712L -> 0xFFF8FAFC
        0xFF0B132BL -> 0xFFF1F5F9 // slate 100
        0xFF0F172AL -> 0xFFF1F5F9 // slate 100
        
        // Cards / Surfaces (slate 800/900 -> pure white)
        0xFF1E293BL -> 0xFFFFFFFF // white
        0xFF111827L -> 0xFFFFFFFF // white
        0xFF1F2937L -> 0xFFFFFFFF // white
        
        // Borders (slate 700 -> slate 300)
        0xFF334155L -> 0xFFCBD5E1
        0xFF475569L -> 0xFFE2E8F0
        
        // Texts / Icons (white/gray -> dark slate)
        0xFFE2E8F0L -> 0xFF0F172A
        0xFF94A3B8L -> 0xFF334155
        0xFF64748BL -> 0xFF475569
        0xFFFFFFFFL -> 0xFF0F172A // Note: we carefully shadow white only for specific container overrides if needed, but general white text mapping is done locally
        
        else -> colorValue
    }
    
    return Color(mappedValue)
}

fun Color(colorValue: Int): Color {
    if (isAppInDarkTheme) {
        return Color(colorValue)
    }
    return Color(colorValue.toLong())
}
