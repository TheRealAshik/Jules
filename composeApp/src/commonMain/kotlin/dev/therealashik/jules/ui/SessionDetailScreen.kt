package dev.therealashik.jules.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.Activity
import dev.therealashik.jules.sdk.models.PullRequest
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(viewModel: JulesViewModel, state: UiState, screen: Screen.SessionDetail) {
    var showAttachSheet by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.activities.size) {
        if (state.activities.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(screen.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .imePadding()
            ) {
                if (state.activities.isEmpty() && screen.prompt.isBlank()) {
                    EmptyState(title = screen.title)
                } else {
                    val reversedActivities = remember(state.activities) { state.activities.reversed() }
                    val pullRequests = remember(state.sessionsById, screen.sessionId) {
                        state.sessionsById[screen.sessionId]
                            ?.outputs?.mapNotNull { it.pullRequest }?.filter { it.url.isNotBlank() }
                            ?: emptyList()
                    }
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pullRequests, key = { "pr_${it.url}" }) { pr ->
                            PullRequestCard(pr)
                        }
                        items(reversedActivities, key = { it.id.ifEmpty { it.name } }) { activity ->
                            ChatBubble(activity = activity)
                        }
                        if (screen.prompt.isNotBlank()) {
                            item(key = "initial_prompt") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                                        modifier = Modifier.padding(start = 32.dp)
                                    ) {
                                        Text(
                                            text = screen.prompt,
                                            modifier = Modifier.padding(12.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Pill
                InputPill(
                    text = text,
                    onTextChange = { text = it },
                    state = state,
                    screen = screen,
                    viewModel = viewModel,
                    onAttachClick = { showAttachSheet = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photos")
                        }
                        Text("Photos", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                        Text("Camera", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                val items = listOf(
                    Triple(Icons.Default.Image, "Images", "Create and edit"),
                    Triple(Icons.Default.Movie, "Videos", "Bring ideas to life"),
                    Triple(Icons.Default.MusicNote, "Music", "Make audio tracks"),
                    Triple(Icons.Default.Brush, "Canvas", "Code, write, or make slides"),
                    Triple(Icons.Default.Search, "Deep research", "Get detailed reports"),
                    Triple(Icons.AutoMirrored.Filled.MenuBook, "Guided learning", "Get step-by-step help"),
                    Triple(Icons.Default.MoreHoriz, "More uploads", "Files, Notebooks, and more")
                )

                items.forEach { (icon, title, subtitle) ->
                    ListItem(
                        headlineContent = { Text(title) },
                        supportingContent = { Text(subtitle) },
                        leadingContent = { Icon(icon, contentDescription = title) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition = rememberInfiniteTransition()
            val colorPhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            val colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.secondary
            )
            val starColor = colors[colorPhase.toInt() % 4]

            Text(
                text = "✦",
                style = MaterialTheme.typography.displayLarge,
                color = starColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBubble(activity: Activity) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it / 3 } + fadeIn(tween(250))
    ) {
        val agentMessaged = activity.agentMessaged
        val userMessaged = activity.userMessaged
        val planGenerated = activity.planGenerated?.plan

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            if (userMessaged != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                        modifier = Modifier.padding(start = 32.dp)
                    ) {
                        Text(
                            text = userMessaged.userMessage,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (agentMessaged != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                        modifier = Modifier.padding(end = 32.dp)
                    ) {
                        Markdown(
                            content = agentMessaged.agentMessage,
                            modifier = Modifier.padding(12.dp),
                            colors = markdownColor(
                                text = MaterialTheme.colorScheme.onSurfaceVariant,
                                codeBackground = MaterialTheme.colorScheme.surface,
                            ),
                            typography = markdownTypography(
                                text = MaterialTheme.typography.bodyMedium,
                                code = MaterialTheme.typography.bodySmall,
                            )
                        )
                    }
                }
            } else if (planGenerated != null) {
                var expanded by remember { mutableStateOf(true) }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📋 Plan", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    "${planGenerated.steps.size} steps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    planGenerated.steps.forEach { step ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "${step.index + 1}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(step.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                                if (step.description.isNotBlank()) {
                                                    Text(
                                                        step.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            } else if (activity.progressUpdated != null) {
                val progress = activity.progressUpdated!!
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = progress.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (progress.description.isNotBlank()) {
                        Text(
                            text = progress.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (activity.sessionCompleted != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(activity.description.ifBlank { "Completed" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else if (activity.sessionFailed != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        activity.sessionFailed!!.reason.ifBlank { activity.description },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (activity.planApproved != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    Text(activity.description.ifBlank { "Plan approved" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                val label = activity.description.ifBlank { null }
                if (label != null) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PullRequestCard(pr: PullRequest) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MergeType,
                contentDescription = "Pull Request",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Pull Request", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pr.title.ifBlank { pr.url }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (pr.description.isNotBlank()) {
                    Text(pr.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InputPill(
    text: String,
    onTextChange: (String) -> Unit,
    state: UiState,
    screen: Screen.SessionDetail,
    viewModel: JulesViewModel,
    onAttachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Pill surface
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                // + button
                IconButton(onClick = onAttachClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    maxLines = 4,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) {
                                Text("Ask Jules", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                            }
                            inner()
                        }
                    }
                )
                // Right button
                AnimatedContent(
                    targetState = when {
                        state.isLoading -> "stop"
                        text.isNotBlank() -> "send"
                        else -> "mic"
                    },
                    transitionSpec = {
                        (scaleIn(tween(150)) + fadeIn()).togetherWith(scaleOut(tween(150)) + fadeOut())
                    },
                    label = "InputButton"
                ) { mode ->
                    when (mode) {
                        "send" -> IconButton(onClick = { viewModel.sendMessage(screen.sessionId, text); onTextChange("") }) {
                            Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        "stop" -> IconButton(onClick = {}) {
                            Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                        else -> IconButton(onClick = {}) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic")
                        }
                    }
                }
            }
        }
    }
}
