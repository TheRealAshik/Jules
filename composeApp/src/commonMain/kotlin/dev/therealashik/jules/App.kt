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
import dev.therealashik.jules.ui.ChatMainScreen
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
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF71717A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4F4F5),
    onSecondaryContainer = Color(0xFF18181B),
    tertiary = Color(0xFF0EA5E9),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0369A1),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFF9F9FB),
    onBackground = Color(0xFF18181B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF71717A),
    surfaceContainerHigh = Color(0xFFF4F4F5),
    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFF4F4F5)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color(0xFFF4F4F5),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF082F49),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFFE0F2FE),
    error = Color(0xFFA82323),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF121214),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFA1A1AA),
    surfaceContainerHigh = Color(0xFF27272A),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF27272A)
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
                is Screen.SessionList -> ChatMainScreen(
                    viewModel = viewModel,
                    state = state,
                    currentSession = null,
                    onNavigateToSettings = { viewModel.navigate(Screen.Settings) },
                    onNavigateToPromptGallery = { viewModel.navigate(Screen.PromptGallery) }
                )
                is Screen.CreateSession -> CreateSessionScreen(viewModel, state)
                is Screen.SessionDetail -> {
                    val currentSession = state.sessions.find {
                        (it.name.substringAfter("sessions/").takeIf { id -> id.isNotBlank() } ?: it.id) == screen.sessionId
                    }
                    ChatMainScreen(
                        viewModel = viewModel,
                        state = state,
                        currentSession = currentSession,
                        onNavigateToSettings = { viewModel.navigate(Screen.Settings) },
                        onNavigateToPromptGallery = { viewModel.navigate(Screen.PromptGallery) }
                    )
                }
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
