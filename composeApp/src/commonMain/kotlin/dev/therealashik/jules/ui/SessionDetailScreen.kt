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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.Activity
import dev.therealashik.jules.sdk.models.PullRequest
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import dev.therealashik.jules.sdk.models.GitPatch
import dev.therealashik.jules.sdk.models.BashOutput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalUriHandler

private val ATTACHMENT_ITEMS = listOf(
    Triple(Icons.Default.Image, Strings.IMAGES, Strings.CREATE_AND_EDIT),
    Triple(Icons.Default.Movie, Strings.VIDEOS, Strings.BRING_IDEAS_TO_LIFE),
    Triple(Icons.Default.MusicNote, Strings.MUSIC, Strings.MAKE_AUDIO_TRACKS),
    Triple(Icons.Default.Brush, Strings.CANVAS, Strings.CODE_WRITE_OR_MAKE_SLIDES),
    Triple(Icons.Default.Search, Strings.DEEP_RESEARCH, Strings.GET_DETAILED_REPORTS),
    Triple(Icons.AutoMirrored.Filled.MenuBook, Strings.GUIDED_LEARNING, Strings.GET_STEP_BY_STEP_HELP),
    Triple(Icons.Default.MoreHoriz, Strings.MORE_UPLOADS, Strings.FILES_NOTEBOOKS_AND_MORE)
)

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = Strings.MORE)
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
                        contentPadding = PaddingValues(bottom = Dimens.spacing80, top = Dimens.spacingS, start = Dimens.spacingL, end = Dimens.spacingL),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)
                    ) {
                        items(pullRequests, key = { "pr_${it.url}" }) { pr ->
                            PullRequestCard(pr)
                        }
                        items(reversedActivities, key = { it.id.ifEmpty { it.name } }) { activity ->
                            ChatBubble(activity = activity, session = state.sessionsById[screen.sessionId], viewModel = viewModel)
                        }
                        if (screen.prompt.isNotBlank()) {
                            item(key = "initial_prompt") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(Dimens.bubbleCornerRadius, Dimens.bubbleCornerRadius, Dimens.spacingXs, Dimens.bubbleCornerRadius),
                                        modifier = Modifier.padding(start = Dimens.spacingXxl)
                                    ) {
                                        Markdown(
                                            content = screen.prompt,
                                            modifier = Modifier.padding(Dimens.spacingM),
                                            colors = markdownColor(
                                                text = MaterialTheme.colorScheme.onPrimaryContainer,
                                                codeBackground = MaterialTheme.colorScheme.primaryContainer,
                                            ),
                                            typography = markdownTypography(
                                                text = MaterialTheme.typography.bodyMedium,
                                                code = MaterialTheme.typography.bodySmall,
                                            )
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
                    .padding(bottom = Dimens.spacingXxl)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingS),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = Strings.PHOTOS)
                        }
                        Text(Strings.PHOTOS, style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.CameraAlt, contentDescription = Strings.CAMERA)
                        }
                        Text(Strings.CAMERA, style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.spacingS))

                ATTACHMENT_ITEMS.forEach { (icon, title, subtitle) ->
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
fun BashOutputCard(bashOutput: BashOutput) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.spacingL)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = Strings.BASH,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Dimens.spacingS))
                Column(modifier = Modifier.weight(1f)) {
                    Text(Strings.BASH, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    if (bashOutput.command.isNotBlank()) {
                        Text(
                            bashOutput.command,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(Dimens.spacingS))
                Surface(
                    color = if (bashOutput.exitCode == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(Dimens.spacingXs)
                ) {
                    Text(
                        bashOutput.exitCode.toString(),
                        modifier = Modifier.padding(horizontal = Dimens.spacing6, vertical = Dimens.spacingXxs),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bashOutput.exitCode == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.width(Dimens.spacingXs))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) Strings.COLLAPSE else Strings.EXPAND,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = Dimens.spacingL, end = Dimens.spacingL, bottom = Dimens.spacingM)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.spacingS))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = Dimens.codeBlockMaxHeight)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.spacingS))
                            .border(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.spacingS))
                            .verticalScroll(rememberScrollState())
                            .padding(Dimens.spacingS)
                    ) {
                        Text(
                            text = bashOutput.output,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                text = Strings.SPARKLE,
                style = MaterialTheme.typography.displayLarge,
                color = starColor,
                modifier = Modifier.padding(bottom = Dimens.spacingL)
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
fun ChatBubble(activity: Activity, session: dev.therealashik.jules.sdk.models.Session?, viewModel: JulesViewModel) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it / 3 } + fadeIn(tween(250))
    ) {
        val agentMessaged = activity.agentMessaged
        val userMessaged = activity.userMessaged
        val planGenerated = activity.planGenerated?.plan

        val isAwaitingApproval = session?.state == dev.therealashik.jules.sdk.models.SessionState.AWAITING_PLAN_APPROVAL
        var editableSteps by remember(planGenerated) {
            mutableStateOf(planGenerated?.steps ?: emptyList())
        }

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spacingXs)) {
            if (userMessaged != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(Dimens.bubbleCornerRadius, Dimens.bubbleCornerRadius, Dimens.spacingXs, Dimens.bubbleCornerRadius),
                        modifier = Modifier.padding(start = Dimens.spacingXxl)
                    ) {
                        Markdown(
                            content = userMessaged.userMessage,
                            modifier = Modifier.padding(Dimens.spacingM),
                            colors = markdownColor(
                                text = MaterialTheme.colorScheme.onPrimaryContainer,
                                codeBackground = MaterialTheme.colorScheme.primaryContainer,
                            ),
                            typography = markdownTypography(
                                text = MaterialTheme.typography.bodyMedium,
                                code = MaterialTheme.typography.bodySmall,
                            )
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
                        shape = RoundedCornerShape(Dimens.bubbleCornerRadius, Dimens.bubbleCornerRadius, Dimens.bubbleCornerRadius, Dimens.spacingXs),
                        modifier = Modifier.padding(end = Dimens.spacingXxl)
                    ) {
                        Markdown(
                            content = agentMessaged.agentMessage,
                            modifier = Modifier.padding(Dimens.spacingM),
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
                    OutlinedCard(modifier = Modifier.padding(horizontal = Dimens.spacingL)) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingM),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(Strings.PLAN, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    "${planGenerated.steps.size} ${Strings.STEPS}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(Dimens.spacingXs))
                                Icon(
                                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) Strings.COLLAPSE else Strings.EXPAND,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(start = Dimens.spacingL, end = Dimens.spacingL, bottom = Dimens.spacingM)) {
                                    HorizontalDivider()
                                    Spacer(Modifier.height(Dimens.spacingS))
                                    val displaySteps = if (isAwaitingApproval) editableSteps else planGenerated.steps
                                    displaySteps.forEachIndexed { idx, step ->
                                        Row(
                                            modifier = Modifier.padding(vertical = Dimens.spacingXs),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(Dimens.planStepIndicatorSize)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "${idx + 1}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(Dimens.spacingS))
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (isAwaitingApproval) {
                                                    androidx.compose.foundation.text.BasicTextField(
                                                        value = step.title,
                                                        onValueChange = { newTitle ->
                                                            editableSteps = editableSteps.toMutableList().apply {
                                                                set(idx, step.copy(title = newTitle))
                                                            }
                                                        },
                                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        ),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                                    )
                                                    Spacer(Modifier.height(Dimens.spacingXxs))
                                                    androidx.compose.foundation.text.BasicTextField(
                                                        value = step.description,
                                                        onValueChange = { newDesc ->
                                                            editableSteps = editableSteps.toMutableList().apply {
                                                                set(idx, step.copy(description = newDesc))
                                                            }
                                                        },
                                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                                    )
                                                } else {
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
                                            if (isAwaitingApproval) {
                                                Column(modifier = Modifier.width(Dimens.spacingXl)) {
                                                    IconButton(
                                                        onClick = {
                                                            if (idx > 0) {
                                                                editableSteps = editableSteps.toMutableList().apply {
                                                                    val temp = this[idx]
                                                                    this[idx] = this[idx - 1]
                                                                    this[idx - 1] = temp
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.size(Dimens.iconSizeMedium)
                                                    ) {
                                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = Strings.UP, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            if (idx < editableSteps.size - 1) {
                                                                editableSteps = editableSteps.toMutableList().apply {
                                                                    val temp = this[idx]
                                                                    this[idx] = this[idx + 1]
                                                                    this[idx + 1] = temp
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.size(Dimens.iconSizeMedium)
                                                    ) {
                                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = Strings.DOWN, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            editableSteps = editableSteps.toMutableList().apply {
                                                                removeAt(idx)
                                                            }
                                                        },
                                                        modifier = Modifier.size(Dimens.iconSizeMedium)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = Strings.REMOVE_STEP, tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (isAwaitingApproval) {
                                        TextButton(
                                            onClick = {
                                                editableSteps = editableSteps + dev.therealashik.jules.sdk.models.PlanStep(
                                                    title = "New step",
                                                    description = ""
                                                )
                                            },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(Dimens.spacingL))
                                            Spacer(Modifier.width(Dimens.spacingXs))
                                            Text(Strings.ADD_STEP)
                                        }
                                    }
                                }
                            }
                            if (isAwaitingApproval) {
                                HorizontalDivider()
                                Box(modifier = Modifier.fillMaxWidth().padding(Dimens.spacingM), contentAlignment = Alignment.Center) {
                                    Button(onClick = {
                                        viewModel.approvePlan(
                                            sessionId = session?.id ?: "",
                                            plan = planGenerated.copy(steps = editableSteps.mapIndexed { i, step -> step.copy(index = i) })
                                        )
                                    }) {
                                        Text(Strings.APPROVE)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activity.progressUpdated != null) {
                val progress = activity.progressUpdated!!
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spacingXxs),
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
                    Spacer(Modifier.width(Dimens.spacingXs))
                    Text(activity.description.ifBlank { Strings.COMPLETED }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else if (activity.sessionFailed != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(Dimens.spacingXs))
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
                    Spacer(Modifier.width(Dimens.spacingXs))
                    Text(activity.description.ifBlank { Strings.PLAN_APPROVED }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            } else if (activity.artifacts.isEmpty()) {
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

            if (activity.artifacts.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingS),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activity.artifacts.forEach { artifact ->
                        artifact.changeSet?.gitPatch?.let { gitPatch ->
                            GitPatchCard(gitPatch)
                        }
                        artifact.bashOutput?.let { bashOutput ->
                            BashOutputCard(bashOutput)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GitPatchCard(gitPatch: GitPatch) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.spacingL)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = Strings.GIT_PATCH,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Dimens.spacingS))
                Column(modifier = Modifier.weight(1f)) {
                    Text(Strings.GIT_PATCH, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    if (gitPatch.suggestedCommitMessage.isNotBlank()) {
                        Text(
                            gitPatch.suggestedCommitMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(Dimens.spacingXs))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) Strings.COLLAPSE else Strings.EXPAND,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = Dimens.spacingL, end = Dimens.spacingL, bottom = Dimens.spacingM)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.spacingS))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.spacingS))
                            .padding(Dimens.borderWidth)
                    ) {
                        UnifiedDiffViewer(
                            patch = gitPatch.unidiffPatch,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PullRequestCard(pr: PullRequest) {
    val uriHandler = LocalUriHandler.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { uriHandler.openUri(pr.url) }
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spacingM),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.MergeType,
                contentDescription = Strings.PULL_REQUEST,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeMedium)
            )
            Spacer(Modifier.width(Dimens.spacingS))
            Column(modifier = Modifier.weight(1f)) {
                Text(Strings.PULL_REQUEST, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pr.title.ifBlank { pr.url }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (pr.description.isNotBlank()) {
                    Text(pr.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.OpenInNew, contentDescription = Strings.OPEN, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingM)
    ) {
        // Pill surface
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.spacingXs, vertical = Dimens.spacingXs)
            ) {
                // + button
                IconButton(onClick = onAttachClick) {
                    Icon(Icons.Default.Add, contentDescription = Strings.ADD)
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
                                Text(Strings.ASK_JULES, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
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
                    label = Strings.INPUT_BUTTON
                ) { mode ->
                    when (mode) {
                        "send" -> IconButton(onClick = { viewModel.sendMessage(screen.sessionId, text); onTextChange("") }) {
                            Box(Modifier.size(Dimens.inputButtonSize).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = Strings.SEND, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(Dimens.iconSizeMedium))
                            }
                        }
                        "stop" -> IconButton(onClick = {}) {
                            Box(Modifier.size(Dimens.inputButtonSize).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Stop, contentDescription = Strings.STOP, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(Dimens.iconSizeMedium))
                            }
                        }
                        else -> IconButton(onClick = {}) {
                            Icon(Icons.Default.Mic, contentDescription = Strings.MIC)
                        }
                    }
                }
            }
        }
    }
}
