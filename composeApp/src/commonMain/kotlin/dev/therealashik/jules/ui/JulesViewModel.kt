package dev.therealashik.jules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.therealashik.jules.KeyValueStore
import dev.therealashik.jules.notifications.AppNotificationManager
import dev.therealashik.jules.notifications.NotificationEvent
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
    data object Welcome : Screen
    data object ApiKeySetup : Screen
    data object SessionList : Screen
    data object CreateSession : Screen
    data class SessionDetail(val sessionId: String, val title: String, val prompt: String = "") : Screen
    data object Settings : Screen
    data object PromptGallery : Screen
}

enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class SessionFilter { ALL, ACTIVE, ARCHIVED }

data class UiState(
    val sessions: List<Session> = emptyList(),
    val sessionsById: Map<String, Session> = emptyMap(),
    val activities: List<Activity> = emptyList(),
    val activitiesNextPageToken: String? = null,
    val hasMoreActivities: Boolean = false,
    val isLoadingMoreActivities: Boolean = false,
    val activeSessionId: String? = null,
    val promptItems: List<PromptItem> = emptyList(),
    val selectedGalleryPrompts: List<PromptItem> = emptyList(),
    val sources: List<Source> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val screen: Screen = Screen.Welcome,
    val apiKey: String = "",
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val pageSize: Int = 30,
    val sessionFilter: SessionFilter = SessionFilter.ACTIVE
)

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

    private val notifiedSessionStates = mutableMapOf<String, SessionState>()
    private val notificationManager = AppNotificationManager()

    private val _state = MutableStateFlow(
        UiState(
            apiKey = initialApiKey,
            screen = if (initialApiKey.isBlank()) Screen.Welcome else Screen.SessionList,
            themePreference = initialTheme,
            pageSize = initialPageSize
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun startNewChat() {
        _state.update {
            it.copy(
                activeSessionId = null,
                activities = emptyList(),
                activitiesNextPageToken = null,
                hasMoreActivities = false,
                selectedGalleryPrompts = emptyList(),
                screen = Screen.SessionList
            )
        }
    }

    fun saveApiKey(key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isBlank()) {
            _state.update { it.copy(error = Strings.INVALID_API_KEY) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val testClient = JulesApiClient(trimmedKey)
            try {
                testClient.listSessions(pageSize = 1)
                store?.putString("api_key", trimmedKey)
                apiClient.close()
                apiClient = testClient
                _state.update { it.copy(isLoading = false, apiKey = trimmedKey, error = null) }
                navigate(Screen.SessionList)
            } catch (e: CancellationException) {
                testClient.close()
                throw e
            } catch (e: Exception) {
                testClient.close()
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message?.takeIf { msg -> msg.isNotBlank() } ?: Strings.INVALID_API_KEY
                    )
                }
            }
        }
    }

    fun saveThemePreference(theme: ThemePreference) {
        store?.putString("theme_preference", theme.name)
        _state.update { it.copy(themePreference = theme) }
    }

    fun savePageSize(size: Int) {
        store?.putString("page_size", size.toString())
        _state.update { it.copy(pageSize = size) }
    }

    fun setSessionFilter(filter: SessionFilter) {
        _state.update { it.copy(sessionFilter = filter) }
        loadSessions()
    }

    fun navigate(screen: Screen) {
        if (screen !is Screen.CreateSession) {
            _state.update { it.copy(selectedGalleryPrompts = emptyList()) }
        }
        _state.update { it.copy(screen = screen, error = null) }
        when (screen) {
            is Screen.SessionList -> {
                if (state.value.activeSessionId == null) {
                    _state.update { it.copy(activities = emptyList(), activitiesNextPageToken = null, hasMoreActivities = false) }
                }
                loadSessions()
            }
            is Screen.SessionDetail -> {
                _state.update { it.copy(activeSessionId = screen.sessionId) }
                loadActivities(screen.sessionId, initialPromptFallback = screen.prompt)
            }
            is Screen.PromptGallery -> loadPrompts()
            Screen.CreateSession -> {
                loadPrompts()
                loadSources()
            }
            Screen.Welcome, Screen.ApiKeySetup, Screen.Settings -> Unit
        }
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

    fun archiveSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.archiveSession(sessionId.normalizeSessionId())
                loadSessions()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to archive session") }
            }
        }
    }

    fun unarchiveSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.unarchiveSession(sessionId.normalizeSessionId())
                loadSessions()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to unarchive session") }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.deleteSession(sessionId.normalizeSessionId())
                if (_state.value.activeSessionId == sessionId.normalizeSessionId()) {
                    startNewChat()
                } else {
                    loadSessions()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to delete session") }
            }
        }
    }

    private fun checkSessionNotifications(sessions: List<Session>) {
        sessions.forEach { session ->
            val sessionId = session.name.substringAfter("sessions/").takeIf { it.isNotBlank() } ?: session.id
            val currentState = session.state
            val previousState = notifiedSessionStates[sessionId]

            if (previousState != currentState && currentState in listOf(
                    SessionState.AWAITING_PLAN_APPROVAL,
                    SessionState.AWAITING_USER_FEEDBACK,
                    SessionState.COMPLETED,
                    SessionState.FAILED
                )
            ) {
                notifiedSessionStates[sessionId] = currentState
                notificationManager.notifySessionEvent(
                    NotificationEvent(
                        sessionId = sessionId,
                        sessionTitle = session.title,
                        state = currentState
                    )
                )
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val filterParam = when (_state.value.sessionFilter) {
                    SessionFilter.ARCHIVED -> "archived = true"
                    SessionFilter.ACTIVE -> "archived = false"
                    SessionFilter.ALL -> null
                }
                val includeArchivedParam = if (_state.value.sessionFilter == SessionFilter.ALL) true else null

                val response = apiClient.listSessions(
                    pageSize = _state.value.pageSize,
                    filter = filterParam,
                    includeArchived = includeArchivedParam
                )
                val sessionsById = response.sessions.associateBy { it.id } +
                    response.sessions.associateBy { it.name.normalizeSessionId() }
                checkSessionNotifications(response.sessions)
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
                val newSessionId = session.name.normalizeSessionId()
                _state.update {
                    it.copy(
                        isLoading = false,
                        activeSessionId = newSessionId,
                        selectedGalleryPrompts = emptyList()
                    )
                }
                loadSessions()
                navigate(Screen.SessionDetail(newSessionId, session.title.ifBlank { title }, finalPrompt))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to create session") }
            }
        }
    }

    fun loadActivities(sessionId: String, initialPromptFallback: String = "") {
        val normalizedId = sessionId.normalizeSessionId()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, activeSessionId = normalizedId) }
            try {
                val response = apiClient.listActivities(normalizedId, pageSize = _state.value.pageSize)
                val fetchedActivities = response.activities

                // Retrieve session metadata prompt if available
                val sessionPrompt = _state.value.sessionsById[normalizedId]?.prompt
                    .takeIf { !it.isNullOrBlank() }
                    ?: initialPromptFallback

                val finalActivities = processActivitiesWithInitialPrompt(
                    normalizedId = normalizedId,
                    sessionPrompt = sessionPrompt,
                    activities = fetchedActivities
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        activities = finalActivities,
                        activitiesNextPageToken = response.nextPageToken,
                        hasMoreActivities = !response.nextPageToken.isNullOrBlank()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load activities") }
            }
        }
    }

    fun loadMoreActivities() {
        val activeId = _state.value.activeSessionId ?: return
        val pageToken = _state.value.activitiesNextPageToken ?: return
        if (_state.value.isLoadingMoreActivities) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreActivities = true) }
            try {
                val response = apiClient.listActivities(activeId, pageSize = _state.value.pageSize, pageToken = pageToken)
                val existingList = _state.value.activities
                val existingIds = existingList.map { it.id.ifEmpty { it.name } }.toSet()
                val newUnique = response.activities.filterNot { existingIds.contains(it.id.ifEmpty { it.name }) }

                val sessionPrompt = _state.value.sessionsById[activeId]?.prompt ?: ""
                val combined = processActivitiesWithInitialPrompt(
                    normalizedId = activeId,
                    sessionPrompt = sessionPrompt,
                    activities = existingList + newUnique
                )

                _state.update {
                    it.copy(
                        isLoadingMoreActivities = false,
                        activities = combined,
                        activitiesNextPageToken = response.nextPageToken,
                        hasMoreActivities = !response.nextPageToken.isNullOrBlank()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMoreActivities = false, error = e.message ?: "Failed to load more activities") }
            }
        }
    }

    private fun processActivitiesWithInitialPrompt(
        normalizedId: String,
        sessionPrompt: String,
        activities: List<Activity>
    ): List<Activity> {
        val hasUserPromptActivity = activities.any {
            it.userMessaged != null || it.originator.equals("USER", ignoreCase = true)
        }

        val listWithPrompt = if (!hasUserPromptActivity && sessionPrompt.isNotBlank()) {
            val initialUserActivity = Activity(
                id = "initial_prompt_$normalizedId",
                name = "activities/initial_prompt_$normalizedId",
                originator = "USER",
                description = sessionPrompt,
                userMessaged = UserMessaged(userMessage = sessionPrompt)
            )
            listOf(initialUserActivity) + activities
        } else {
            activities
        }

        // Deduplicate activities by non-empty ID/name while retaining exact chronological sequence
        val seenIds = mutableSetOf<String>()
        return listWithPrompt.filter { activity ->
            val key = activity.id.ifEmpty { activity.name }
            if (key.isBlank()) true else seenIds.add(key)
        }
    }

    fun sendMessage(sessionId: String, prompt: String) {
        val normalizedId = sessionId.normalizeSessionId()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.sendMessage(normalizedId, SendMessageRequest(prompt = prompt))
                loadActivities(normalizedId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send message") }
            }
        }
    }

    fun approvePlan(sessionId: String) {
        val normalizedId = sessionId.normalizeSessionId()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                apiClient.approvePlan(normalizedId)
                loadActivities(normalizedId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to approve plan") }
            }
        }
    }
}
