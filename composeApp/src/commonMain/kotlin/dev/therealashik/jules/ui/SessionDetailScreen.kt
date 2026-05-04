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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.therealashik.jules.sdk.models.Activity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
        AuroraBackground()
        
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(screen.title) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
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
            ) {
                if (state.activities.isEmpty()) {
                    EmptyState(title = screen.title)
                } else {
                    val reversedActivities = remember(state.activities) { state.activities.reversed() }
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reversedActivities, key = { it.id.ifEmpty { it.name } }) { activity ->
                            ChatBubble(activity = activity)
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
fun AuroraBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Top solid color
        drawRect(
            color = Color(0xFF0A0A0A),
            size = size
        )

        // Bottom aurora particles
        val colors = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0xFFFF4081))
        
        // We use a fixed seed to keep dots in place but let them pulsate
        val random = Random(42)
        val numDots = 400
        val bottomCenter = Offset(width / 2f, height + 100f)
        val maxRadius = height * 0.7f

        for (i in 0 until numDots) {
            val angle = random.nextFloat() * Math.PI.toFloat()
            val r = random.nextFloat() * maxRadius
            
            val x = bottomCenter.x + cos(angle) * r
            val y = bottomCenter.y - sin(angle) * r
            
            // Only draw if within bounds and bottom half mostly
            if (y < height && y > height * 0.3f) {
                val color = colors[i % colors.size]
                val baseAlpha = 0.1f + random.nextFloat() * 0.5f
                val pulse = sin(offset * Math.PI.toFloat() * 2 + i) * 0.2f
                val alpha = (baseAlpha + pulse).coerceIn(0f, 1f)
                val dotSize = 2f + random.nextFloat() * 4f + pulse * 2f

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = dotSize,
                    center = Offset(x, y)
                )
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
            
            val starColor = when (colorPhase.toInt() % 4) {
                0 -> Color(0xFF4285F4) // Blue
                1 -> Color(0xFFEA4335) // Red
                2 -> Color(0xFFFBBC05) // Yellow
                else -> Color(0xFF34A853) // Green
            }

            Text(
                text = "✦",
                fontSize = 64.sp,
                color = starColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
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
                        color = Color(0xFF1A73E8),
                        shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                        modifier = Modifier.padding(start = 32.dp)
                    ) {
                        Text(
                            text = userMessaged.userMessage,
                            modifier = Modifier.padding(12.dp),
                            color = Color.White
                        )
                    }
                }
            } else if (agentMessaged != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier.padding(end = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(8.dp)
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                        )
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                        ) {
                            Text(
                                text = agentMessaged.agentMessage,
                                modifier = Modifier.padding(12.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            } else if (planGenerated != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📋 Plan", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            planGenerated.steps.forEach { step ->
                                Text(
                                    "${step.index + 1}. ${step.title}",
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                if (step.description.isNotBlank()) {
                                    Text(
                                        step.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = activity.description.ifBlank { "System event" },
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
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
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Glass background layer (blurred)
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
        )
        // Pill surface
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF1A1A2E).copy(alpha = 0.75f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                // + button
                IconButton(onClick = onAttachClick) {
                    Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Add")
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                    maxLines = 4,
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) {
                                Text("Ask Jules", color = Color.White.copy(0.45f), fontSize = 16.sp)
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
                            Box(Modifier.size(36.dp).background(Color(0xFF1A73E8), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        "stop" -> IconButton(onClick = {}) {
                            Box(Modifier.size(36.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        else -> IconButton(onClick = {}) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
