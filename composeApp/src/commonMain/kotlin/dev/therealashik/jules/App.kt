package dev.therealashik.jules

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.therealashik.jules.sdk.JulesApiClient
import dev.therealashik.jules.ui.CreateSessionScreen
import dev.therealashik.jules.ui.JulesViewModel
import dev.therealashik.jules.ui.Screen
import dev.therealashik.jules.ui.SessionDetailScreen
import dev.therealashik.jules.ui.SessionListScreen
import dev.therealashik.jules.ui.SettingsScreen
import dev.therealashik.jules.ui.PromptGalleryScreen
import dev.therealashik.jules.ui.CodeReviewScreen
import dev.therealashik.jules.gallery.PromptGalleryRepository
import dev.therealashik.jules.ui.ThemePreference
import dev.therealashik.jules.ui.CrashDialog

// A purple seed color fallback
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun App() {
    val store = remember { KeyValueStore() }
    var crashLog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val lastCrash = store.getString("last_crash_log", "")
        if (lastCrash.isNotEmpty()) {
            crashLog = lastCrash
            store.putString("last_crash_log", "")
        }

        setCrashHandler { throwable ->
            val log = throwable.stackTraceToString()
            crashLog = log
            try {
                store.putString("last_crash_log", log)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val promptGalleryRepository = remember { PromptGalleryRepository(store) }
    val savedKey = remember { store.getString("api_key") }
    val apiClient = remember { JulesApiClient(savedKey, PROXY_URL) }
    val viewModel = viewModel { JulesViewModel(apiClient, savedKey, store, promptGalleryRepository) }
    val state by viewModel.state.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (state.themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemDark
    }
    val colorScheme = getAppColorScheme(darkTheme)

    MaterialTheme(colorScheme = colorScheme, typography = AppTypography()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            when (val screen = state.screen) {
                is Screen.SessionList -> SessionListScreen(viewModel, state)
                is Screen.CreateSession -> CreateSessionScreen(viewModel, state)
                is Screen.SessionDetail -> SessionDetailScreen(viewModel, state, screen)
                is Screen.CodeReview -> CodeReviewScreen(viewModel, state, screen)
                is Screen.Settings -> SettingsScreen(viewModel, state)
                is Screen.PromptGallery -> PromptGalleryScreen(viewModel, state)
            }

            crashLog?.let { log ->
                CrashDialog(
                    crashLog = log,
                    onDismiss = { crashLog = null }
                )
            }
        }
    }
}

fun getDefaultColorScheme(darkTheme: Boolean) = if (darkTheme) DarkColorScheme else LightColorScheme
