package dev.therealashik.jules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.therealashik.jules.sdk.JulesApiClient
import dev.therealashik.jules.sdk.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object SessionList : Screen
    data object CreateSession : Screen
    data class SessionDetail(val sessionId: String, val title: String) : Screen
}

data class UiState(
    val sessions: List<Session> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val screen: Screen = Screen.SessionList
)

class JulesViewModel(private val apiClient: JulesApiClient) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun navigate(screen: Screen) {
        _state.update { it.copy(screen = screen, error = null) }
        when (screen) {
            is Screen.SessionList -> loadSessions()
            is Screen.SessionDetail -> loadActivities(screen.sessionId)
            Screen.CreateSession -> {
                // Just navigation
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                apiClient.listSessions()
            }.onSuccess { response ->
                _state.update { it.copy(isLoading = false, sessions = response.sessions) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sessions") }
            }
        }
    }

    fun createSession(prompt: String, title: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                apiClient.createSession(
                    CreateSessionRequest(
                        prompt = prompt,
                        title = title.takeIf { it.isNotBlank() }
                    )
                )
            }.onSuccess { session ->
                _state.update { it.copy(isLoading = false) }
                navigate(Screen.SessionDetail(session.name.substringAfter("sessions/").takeIf { it.isNotBlank() } ?: session.id, session.title))
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to create session") }
            }
        }
    }

    fun loadActivities(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val normalizedId = sessionId.substringAfter("sessions/")
                apiClient.listActivities(normalizedId)
            }.onSuccess { response ->
                _state.update { it.copy(isLoading = false, activities = response.activities) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load activities") }
            }
        }
    }

    fun sendMessage(sessionId: String, prompt: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val normalizedId = sessionId.substringAfter("sessions/")
                apiClient.sendMessage(normalizedId, SendMessageRequest(prompt = prompt))
            }.onSuccess {
                loadActivities(sessionId)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send message") }
            }
        }
    }

    fun approvePlan(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val normalizedId = sessionId.substringAfter("sessions/")
                apiClient.approvePlan(normalizedId)
            }.onSuccess {
                loadActivities(sessionId)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to approve plan") }
            }
        }
    }
}
