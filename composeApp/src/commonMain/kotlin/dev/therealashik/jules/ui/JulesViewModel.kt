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

import dev.therealashik.jules.gallery.PromptGalleryRepository
import dev.therealashik.jules.gallery.PromptItem

sealed interface Screen {
    data object SessionList : Screen
    data object CreateSession : Screen
    data class SessionDetail(val sessionId: String, val title: String, val prompt: String = "") : Screen
    data object Settings : Screen
    data object PromptGallery : Screen
}

data class UiState(
    val sessions: List<Session> = emptyList(),
    val sessionsById: Map<String, Session> = emptyMap(),
    val activities: List<Activity> = emptyList(),
    val promptItems: List<PromptItem> = emptyList(),
    val selectedGalleryPrompts: List<PromptItem> = emptyList(),
    val sources: List<Source> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val screen: Screen = Screen.SessionList,
    val apiKey: String = "",
    val pageSize: Int = 30
)

private fun String.normalizeSessionId() = substringAfter("sessions/").takeIf { it.isNotBlank() } ?: this

class JulesViewModel(
    private var apiClient: JulesApiClient,
    initialApiKey: String = "",
    private val store: KeyValueStore? = null,
    private val promptGalleryRepository: PromptGalleryRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            apiKey = initialApiKey,
            screen = if (initialApiKey.isBlank()) Screen.Settings else Screen.SessionList,
            pageSize = store?.getString("page_size", "30")?.toIntOrNull() ?: 30
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
        if (screen !is Screen.CreateSession) {
            _state.update { it.copy(selectedGalleryPrompts = emptyList()) }
        }
        _state.update { it.copy(screen = screen, error = null) }
        when (screen) {
            is Screen.SessionList -> loadSessions()
            is Screen.SessionDetail -> loadActivities(screen.sessionId)
            is Screen.PromptGallery -> loadPrompts()
            Screen.CreateSession -> {
                loadPrompts()
                loadSources()
            }
            Screen.Settings -> Unit
        }
    }

    fun savePageSize(pageSize: Int) {
        store?.putString("page_size", pageSize.toString())
        _state.update { it.copy(pageSize = pageSize) }
        navigate(state.value.screen)
    }

    fun loadSources() {
        viewModelScope.launch {
            try {
                val response = apiClient.listSources(pageSize = state.value.pageSize)
                _state.update { it.copy(sources = response.sources) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ignore errors for sources
            }
        }
    }

    fun toggleGalleryPrompt(item: PromptItem) {
        _state.update { state ->
            val current = state.selectedGalleryPrompts.toMutableList()
            if (current.contains(item)) {
                current.remove(item)
            } else {
                current.add(item)
            }
            state.copy(selectedGalleryPrompts = current)
        }
    }

    fun loadPrompts() {
        val prompts = promptGalleryRepository?.getAll() ?: emptyList()
        _state.update { it.copy(promptItems = prompts) }
    }

    fun savePrompt(title: String, prompt: String) {
        val id = title.hashCode().toString() + "_" + prompt.hashCode().toString()
        promptGalleryRepository?.save(PromptItem(id, title, prompt))
        loadPrompts()
    }

    fun deletePrompt(id: String) {
        promptGalleryRepository?.delete(id)
        loadPrompts()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiClient.listSessions(pageSize = state.value.pageSize)
                val sessionsById = response.sessions.associateBy { it.id } +
                    response.sessions.associateBy { it.name.normalizeSessionId() }
                _state.update { it.copy(isLoading = false, sessions = response.sessions, sessionsById = sessionsById) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sessions") }
            }
        }
    }

    fun createSession(prompt: String, title: String, sourceContext: SourceContext? = null) {
        val selectedPromptsText = state.value.selectedGalleryPrompts.joinToString("\n\n") { it.prompt }
        val finalPrompt = if (selectedPromptsText.isNotBlank()) {
            if (prompt.isNotBlank()) "$selectedPromptsText\n\n$prompt" else selectedPromptsText
        } else {
            prompt
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val session = apiClient.createSession(
                    CreateSessionRequest(
                        prompt = finalPrompt,
                        title = title.takeIf { it.isNotBlank() },
                        sourceContext = sourceContext
                    )
                )
                _state.update { it.copy(isLoading = false) }
                navigate(Screen.SessionDetail(session.name.normalizeSessionId(), session.title, finalPrompt))
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
                val response = apiClient.listActivities(sessionId.normalizeSessionId(), pageSize = state.value.pageSize)
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
