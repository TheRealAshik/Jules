package dev.therealashik.jules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.therealashik.jules.KeyValueStore
import dev.therealashik.jules.sdk.JulesApiClient
import dev.therealashik.jules.sdk.models.*
import dev.therealashik.jules.PROXY_URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

enum class ThemePreference { SYSTEM, LIGHT, DARK }

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
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val pageSize: Int = 30,
    val filterStates: Set<SessionState> = emptySet(),
    val filterRepo: String? = null
) {
    val filteredSessions: List<Session>
        get() = sessions.filter { session ->
            val matchState = filterStates.isEmpty() || filterStates.contains(session.state)
            val matchRepo = filterRepo == null || session.sourceContext?.source == filterRepo
            matchState && matchRepo
        }
}

private fun String.normalizeSessionId() = substringAfter("sessions/").takeIf { it.isNotBlank() } ?: this

class JulesViewModel(
    private var apiClient: JulesApiClient,
    initialApiKey: String = "",
    private val store: KeyValueStore? = null,
    private val promptGalleryRepository: PromptGalleryRepository? = null
) : ViewModel() {

    private val initialTheme = store?.getString("theme_preference")
        ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
        ?: ThemePreference.SYSTEM
    private val initialPageSize = store?.getString("page_size")
        ?.toIntOrNull()?.coerceIn(10, 100) ?: 30

    private val _state = MutableStateFlow(
        UiState(
            apiKey = initialApiKey,
            screen = if (initialApiKey.isBlank()) Screen.Settings else Screen.SessionList,
            themePreference = initialTheme,
            pageSize = initialPageSize
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var activityWatchJob: Job? = null
    private var pollingJob: Job? = null

    fun saveApiKey(key: String) {
        store?.putString("api_key", key)
        apiClient.close()
        apiClient = JulesApiClient(key, PROXY_URL)
        _state.update { it.copy(apiKey = key) }
        navigate(Screen.SessionList)
    }

    fun saveThemePreference(theme: ThemePreference) {
        store?.putString("theme_preference", theme.name)
        _state.update { it.copy(themePreference = theme) }
    }

    fun savePageSize(size: Int) {
        store?.putString("page_size", size.toString())
        _state.update { it.copy(pageSize = size) }
    }

    fun navigate(screen: Screen) {
        if (screen !is Screen.CreateSession) {
            _state.update { it.copy(selectedGalleryPrompts = emptyList()) }
        }
        _state.update { it.copy(screen = screen, error = null) }

        activityWatchJob?.cancel()
        activityWatchJob = null
        stopPolling()

        when (screen) {
            is Screen.SessionList -> loadSessions()
            is Screen.SessionDetail -> {
                loadActivities(screen.sessionId)
                startWatchingActivities(screen.sessionId)
                startPolling(screen.sessionId)
            }
            is Screen.PromptGallery -> loadPrompts()
            Screen.CreateSession -> {
                loadPrompts()
                loadSources()
            }
            Screen.Settings -> Unit
        }
    }

    private fun startWatchingActivities(sessionId: String) {
        activityWatchJob = viewModelScope.launch {
            try {
                apiClient.watchActivities(sessionId.normalizeSessionId()).collect { activity ->
                    _state.update { state ->
                        val currentIds = state.activities.map { it.id }.toSet()
                        if (activity.id !in currentIds) {
                            state.copy(activities = state.activities + activity)
                        } else {
                            state
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently handle WS errors, might want to retry or show a toast
            }
        }
    }

    private fun startPolling(sessionId: String) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                try {
                    val session = apiClient.getSession(sessionId.normalizeSessionId(), forceRefresh = true)
                    _state.update { state ->
                        state.copy(
                            sessionsById = state.sessionsById + (sessionId to session)
                        )
                    }
                    loadActivities(sessionId, forceRefresh = true)

                    if (session.state == SessionState.COMPLETED ||
                        session.state == SessionState.FAILED ||
                        session.state == SessionState.STATE_UNSPECIFIED
                    ) {
                        break
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun toggleStateFilter(state: SessionState) {
        _state.update { current ->
            val newStates = if (current.filterStates.contains(state)) {
                current.filterStates - state
            } else {
                current.filterStates + state
            }
            current.copy(filterStates = newStates)
        }
    }

    fun setRepoFilter(repo: String?) {
        _state.update { it.copy(filterRepo = repo) }
    }

    fun loadSources() {
        viewModelScope.launch {
            try {
                val response = apiClient.listSources()
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

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.deleteSession(sessionId)
                loadSessions()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to delete session") }
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiClient.listSessions(pageSize = _state.value.pageSize)
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

    fun loadActivities(sessionId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiClient.listActivities(sessionId.normalizeSessionId(), pageSize = _state.value.pageSize, forceRefresh = forceRefresh)
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
                // Activities will be updated via WebSocket
                _state.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send message") }
            }
        }
    }

    fun approvePlan(sessionId: String, plan: dev.therealashik.jules.sdk.models.Plan? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.approvePlan(sessionId.normalizeSessionId(), plan)
                // Activities will be updated via WebSocket
                _state.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to approve plan") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activityWatchJob?.cancel()
        apiClient.close()
    }
}
