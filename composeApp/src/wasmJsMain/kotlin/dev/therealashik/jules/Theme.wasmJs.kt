package dev.therealashik.jules

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getAppColorScheme(darkTheme: Boolean): ColorScheme = getDefaultColorScheme(darkTheme)
