package dev.therealashik.jules

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ChatGPTBlue = Color(0xFF0066FF)
val ChatGPTBlueDark = Color(0xFF3B82F6)

val LightColorScheme = lightColorScheme(
    primary = ChatGPTBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF001F60),
    secondary = Color(0xFF5D5D5D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4F4F4),
    onSecondaryContainer = Color(0xFF0D0D0D),
    tertiary = Color(0xFF386568),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2F1F1),
    onTertiaryContainer = Color(0xFF002022),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0D0D0D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D0D0D),
    surfaceVariant = Color(0xFFF4F4F4),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE3E3E3),
    outlineVariant = Color(0xFFF0F0F0)
)

val DarkColorScheme = darkColorScheme(
    primary = ChatGPTBlueDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF172554),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = Color(0xFFB4B4B4),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0xFF2F2F2F),
    onSecondaryContainer = Color(0xFFECECEC),
    tertiary = Color(0xFFA0CFD2),
    onTertiary = Color(0xFF003639),
    tertiaryContainer = Color(0xFF1E4D50),
    onTertiaryContainer = Color(0xFFBCEBEF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF171717),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF212121),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = Color(0xFF2F2F2F),
    onSurfaceVariant = Color(0xFFB4B4B4),
    outline = Color(0xFF383838),
    outlineVariant = Color(0xFF2A2A2A)
)

fun getDefaultColorScheme(darkTheme: Boolean): ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

@Composable
expect fun getAppColorScheme(darkTheme: Boolean): ColorScheme
