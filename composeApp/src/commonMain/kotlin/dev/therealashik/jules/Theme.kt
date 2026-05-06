package dev.therealashik.jules

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun getAppColorScheme(darkTheme: Boolean): ColorScheme
