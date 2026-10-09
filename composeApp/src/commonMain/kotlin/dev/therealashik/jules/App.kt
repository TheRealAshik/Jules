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
import dev.therealashik.jules.ui.ApiKeySetupScreen
import dev.therealashik.jules.ui.CreateSessionScreen
import dev.therealashik.jules.ui.JulesViewModel
import dev.therealashik.jules.ui.Screen
import dev.therealashik.jules.ui.SessionDetailScreen
import dev.therealashik.jules.ui.SessionListScreen
import dev.therealashik.jules.ui.SettingsScreen
import dev.therealashik.jules.ui.WelcomeScreen
import dev.therealashik.jules.ui.PromptGalleryScreen
import dev.therealashik.jules.gallery.PromptGalleryRepository
import dev.therealashik.jules.ui.ThemePreference
import dev.therealashik.jules.ui.CrashDialog

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF325A1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC3EBA2),
    onPrimaryContainer = Color(0xFF0F2006),
    secondary = Color(0xFF53634E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8CE),
    onSecondaryContainer = Color(0xFF111F0F),
    tertiary = Color(0xFF386568),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBEF),
    onTertiaryContainer = Color(0xFF002022),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FAF5),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFF8FAF5),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFE1E4DA),
    onSurfaceVariant = Color(0xFF44483F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8D48A),
    onPrimary = Color(0xFF133804),
    primaryContainer = Color(0xFF284F17),
    onPrimaryContainer = Color(0xFFC3EBA2),
    secondary = Color(0xFFBACCB3),
    onSecondary = Color(0xFF263422),
    secondaryContainer = Color(0xFF3C4B37),
    onSecondaryContainer = Color(0xFFD6E8CE),
    tertiary = Color(0xFFA0CFD2),
    onTertiary = Color(0xFF003639),
    tertiaryContainer = Color(0xFF1E4D50),
    onTertiaryContainer = Color(0xFFBCEBEF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE2E3DD),
    surface = Color(0xFF121411),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF44483F),
    onSurfaceVariant = Color(0xFFC5C8BA)
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
    val apiClient = remember { JulesApiClient(savedKey) }
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
                is Screen.Welcome -> WelcomeScreen(
                    onGetStartedClick = { viewModel.navigate(Screen.ApiKeySetup) }
                )
                is Screen.ApiKeySetup -> ApiKeySetupScreen(
                    viewModel = viewModel,
                    state = state,
                    onBackClick = { viewModel.navigate(Screen.Welcome) }
                )
                is Screen.SessionList -> SessionListScreen(viewModel, state)
                is Screen.CreateSession -> CreateSessionScreen(viewModel, state)
                is Screen.SessionDetail -> SessionDetailScreen(viewModel, state, screen)
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
