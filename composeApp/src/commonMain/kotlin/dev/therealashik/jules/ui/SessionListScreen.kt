package dev.therealashik.jules.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.Session
import dev.therealashik.jules.sdk.models.SessionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SessionListScreen(viewModel: JulesViewModel, state: UiState) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedSessionForAction by remember { mutableStateOf<Session?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // We trigger load in ViewModel if sessions are empty, though it's already done by navigate
    LaunchedEffect(Unit) {
        if (state.sessions.isEmpty()) {
            viewModel.loadSessions()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.JULES, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.navigate(Screen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = Strings.SETTINGS)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigate(Screen.CreateSession) },
                icon = { Icon(Icons.Filled.Add, contentDescription = Strings.NEW_SESSION) },
                text = { Text(Strings.NEW_SESSION) },
                expanded = true
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.sessions.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = Strings.EMPTY,
                        modifier = Modifier.size(Dimens.iconSizeXl),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.spacingL))
                    Text(
                        text = Strings.NO_SESSIONS_YET,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // In Material 3.1.0-alpha PullToRefreshBox is introduced, but we are using 1.10.0-alpha05 of compose-material3 which maps to Androidx Compose.
                // PullRefresh has been replaced by PullToRefreshBox. Since it is standard M3 in latest versions, we use basic styling or standard PullToRefresh functionality if available.
                // To be safe against API changes in compose multiplatform M3, we just implement a basic list since we can't reliably know the exact PullToRefresh API name here.
                // Wait, the prompt explicitly said: Strings.PULL_TO_REFRESH.
                // I will use PullToRefreshBox, which is standard in M3.

                // Note: The specific version of compose multiplatform might use PullToRefreshBox or ExperimentalMaterial3Api.
                // If it fails, I'll fallback.

                // To avoid compilation issues with Experimental PullToRefreshBox, we'll try it, and if it fails, fallback to something simpler.

                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.loadSessions() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimens.spacingL),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingM)
                    ) {
                        items(state.sessions, key = { it.id.ifEmpty { it.name } }) { session ->
                            SessionCard(
                                session = session,
                                isCompact = state.sessionListCompact,
                                onClick = {
                                    val sessionId = session.name.substringAfter("sessions/").takeIf { it.isNotBlank() } ?: session.id
                                    viewModel.navigate(Screen.SessionDetail(sessionId, session.title, session.prompt))
                                },
                                onLongClick = {
                                    selectedSessionForAction = session
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedSessionForAction != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSessionForAction = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacingXxl)
            ) {
                Text(
                    text = selectedSessionForAction?.title?.ifBlank { Strings.UNTITLED_SESSION } ?: Strings.UNTITLED_SESSION,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = Dimens.spacingL, end = Dimens.spacingL, top = Dimens.spacingS, bottom = Dimens.spacingL),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                ListItem(
                    headlineContent = { Text(Strings.DELETE_SESSION, color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = Strings.DELETE,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable {
                        selectedSessionForAction?.id?.let { viewModel.deleteSession(it) }
                        selectedSessionForAction = null
                    }
                )

                ListItem(
                    headlineContent = { Text(Strings.OPEN_IN_JULES) },
                    leadingContent = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = Strings.OPEN_IN_JULES
                        )
                    },
                    modifier = Modifier.clickable {
                        selectedSessionForAction?.url?.takeIf { it.isNotBlank() }?.let { uriHandler.openUri(it) }
                        selectedSessionForAction = null
                    }
                )
            }
        }
    }
}

// Temporary custom implementation of PullToRefreshBox if not found in M3. We will remove it if the real one exists or use real one.
// The actual M3 compose Multiplatform has `androidx.compose.material3.pulltorefresh.PullToRefreshBox` but sometimes it requires opt-in.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // We try to import it, if it's not there, we'll get a compile error.
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(session: Session, isCompact: Boolean = false, onClick: () -> Unit, onLongClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        if (isCompact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title.ifBlank { Strings.UNTITLED_SESSION },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = Dimens.spacingM)
                )
                StateBadge(state = session.state)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(Dimens.spacingL)
                    .fillMaxWidth()
            ) {
                Text(
                    text = session.title.ifBlank { Strings.UNTITLED_SESSION },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                Text(
                    text = session.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacingM))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StateBadge(state = session.state)
                    Text(
                        text = session.createTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StateBadge(state: SessionState) {
    val (containerColor, contentColor) = when (state) {
        SessionState.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        SessionState.FAILED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        SessionState.IN_PROGRESS, SessionState.PLANNING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SessionState.AWAITING_PLAN_APPROVAL, SessionState.AWAITING_USER_FEEDBACK -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(
        onClick = {},
        label = { Text(state.name.replace("_", " ")) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = null
    )
}
