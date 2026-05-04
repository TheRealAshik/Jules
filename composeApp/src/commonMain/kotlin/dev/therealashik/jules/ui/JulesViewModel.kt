package dev.therealashik.jules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.therealashik.jules.KeyValueStore
import dev.therealashik.jules.sdk.JulesApiClient
import dev.therealashik.jules.sdk.models.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object SessionList : Screen
    data object CreateSession : Screen
    data class SessionDetail(val sessionId: String, val title: String) : Screen
    data object Settings : Screen
}

data class UiState(
    val sessions: List<Session> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val screen: Screen = Screen.SessionList,
    val apiKey: String = ""
)

private fun String.normalizeSessionId() = substringAfter("sessions/").takeIf { it.isNotBlank() } ?: this

class JulesViewModel(
    private var apiClient: JulesApiClient,
    initialApiKey: String = "",
    private val store: KeyValueStore? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            apiKey = initialApiKey,
            screen = if (initialApiKey.isBlank()) Screen.Settings else Screen.SessionList
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun saveApiKey(key: String) {
        store?.putString("api_key", key)
        apiClient.close()
        apiClient = JulesApiClient(key)
        _state.update { it.copy(apiKey = key) }
        navigate(Screen.SessionList)
    }

    fun navigate(screen: Screen) {
        _state.update { it.copy(screen = screen, error = null) }
        when (screen) {
            is Screen.SessionList -> loadSessions()
            is Screen.SessionDetail -> loadActivities(screen.sessionId)
            Screen.CreateSession, Screen.Settings -> Unit
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiClient.listSessions()
                _state.update { it.copy(isLoading = false, sessions = response.sessions) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sessions") }
            }
        }
    }

    fun createSession(prompt: String, title: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val session = apiClient.createSession(
                    CreateSessionRequest(prompt = prompt, title = title.takeIf { it.isNotBlank() })
                )
                _state.update { it.copy(isLoading = false) }
                navigate(Screen.SessionDetail(session.name.normalizeSessionId(), session.title))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to create session") }
            }
        }
    }

    fun loadActivities(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiClient.listActivities(sessionId.normalizeSessionId())
                _state.update { it.copy(isLoading = false, activities = response.activities) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load activities") }
            }
        }
    }

    fun sendMessage(sessionId: String, prompt: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.sendMessage(sessionId.normalizeSessionId(), SendMessageRequest(prompt = prompt))
                loadActivities(sessionId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send message") }
            }
        }
    }

    fun approvePlan(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.approvePlan(sessionId.normalizeSessionId())
                loadActivities(sessionId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to approve plan") }
            }
        }
    }
}
