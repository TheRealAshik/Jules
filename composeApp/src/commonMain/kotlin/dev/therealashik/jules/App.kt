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

