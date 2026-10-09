package dev.therealashik.jules.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.Activity
import dev.therealashik.jules.sdk.models.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMainScreen(
    viewModel: JulesViewModel,
    state: UiState,
    currentSession: Session? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToPromptGallery: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedSessionId = currentSession?.let {
        it.name.substringAfter("sessions/").takeIf { id -> id.isNotBlank() } ?: it.id
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerContent(
                sessions = state.sessions,
                selectedSessionId = selectedSessionId,
                onSessionSelect = { session ->
                    coroutineScope.launch { drawerState.close() }
                    viewModel.navigate(Screen.SessionDetail(
                        sessionId = session.name.substringAfter("sessions/").takeIf { id -> id.isNotBlank() } ?: session.id,
                        title = session.title.ifBlank { Strings.UNTITLED_SESSION }
                    ))
                },
                onNewChatClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.navigate(Screen.CreateSession)
                },
                onSettingsClick = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onPromptGalleryClick = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToPromptGallery()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentSession?.title?.ifBlank { Strings.JULES } ?: Strings.JULES,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = Strings.NAVIGATION_DRAWER,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigate(Screen.CreateSession) }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = Strings.NEW_CHAT,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Chat activity list or central empty canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (currentSession == null && state.activities.isEmpty()) {
                            // ChatGPT Empty / Starter Screen Canvas
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = Dimens.spacingL),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Dimens.spacingM),
                                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingM)
                                ) {
                                    ContextualActionItem(
                                        icon = Icons.Default.Image,
                                        title = Strings.CREATE_AN_IMAGE,
                                        onClick = { promptText = Strings.CREATE_AN_IMAGE }
                                    )
                                    ContextualActionItem(
                                        icon = Icons.Default.Edit,
                                        title = Strings.WRITE_OR_EDIT,
                                        onClick = { promptText = Strings.WRITE_OR_EDIT }
                                    )
                                    ContextualActionItem(
                                        icon = Icons.Default.Language,
                                        title = Strings.SEARCH_THE_WEB,
                                        onClick = { promptText = Strings.SEARCH_THE_WEB }
                                    )
                                }
                            }
                        } else {
                            // Conversation Activity List
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = Dimens.spacingM),
                                verticalArrangement = Arrangement.spacedBy(Dimens.spacingM),
                                contentPadding = PaddingValues(vertical = Dimens.spacingM)
                            ) {
                                items(state.activities) { activity ->
                                    ActivityBubbleItem(activity = activity)
                                }
                            }
                        }
                    }

                    // Floating Bottom Composer Card (ChatGPT Mobile layout)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.spacingM, vertical = Dimens.spacingS),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.spacingM, vertical = Dimens.spacingS)
                        ) {
                            BasicTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Dimens.spacingXs, bottom = Dimens.spacingS),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (promptText.isEmpty()) {
                                        Text(
                                            text = Strings.ASK_CHATGPT,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Attachment Plus (+) Icon Button
                                IconButton(
                                    onClick = { showAttachSheet = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = Strings.ADD_ATTACHMENT,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mic/Audio Action Icon Button
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = Strings.VOICE_INPUT,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Send Action Button (Active Blue when prompt entered)
                                    val isPromptActive = promptText.isNotBlank()
                                    Surface(
                                        onClick = {
                                            if (isPromptActive) {
                                                val textToSend = promptText
                                                promptText = ""
                                                if (currentSession != null) {
                                                    val sessionId = currentSession.name.substringAfter("sessions/").takeIf { it.isNotBlank() } ?: currentSession.id
                                                    viewModel.sendMessage(sessionId, textToSend)
                                                } else {
                                                    viewModel.createSession(title = "New Session", prompt = textToSend)
                                                }
                                            }
                                        },
                                        shape = CircleShape,
                                        color = if (isPromptActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = Strings.SEND,
                                                tint = if (isPromptActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachSheet = false }
        )
    }
}

@Composable
private fun ContextualActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spacingS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActivityBubbleItem(activity: Activity) {
    val isUser = activity.originator.equals("USER", ignoreCase = true) || activity.userMessaged != null
    val contentText = activity.description.ifBlank {
        activity.userMessaged?.userMessage
            ?: activity.agentMessaged?.agentMessage
            ?: activity.originator
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Box(modifier = Modifier.padding(Dimens.spacingM)) {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingM)
        ) {
            Text(
                text = Strings.ATTACHMENT_OPTIONS,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = Dimens.spacingS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(Strings.IMAGES, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = Dimens.spacingS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(Strings.CANVAS, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = Dimens.spacingS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(Strings.MORE_UPLOADS, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(Dimens.spacingL))
        }
    }
}
