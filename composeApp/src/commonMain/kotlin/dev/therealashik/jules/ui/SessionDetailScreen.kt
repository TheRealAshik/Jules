package dev.therealashik.jules.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(viewModel: JulesViewModel, state: UiState, screen: Screen.SessionDetail) {
    val infiniteTransition = rememberInfiniteTransition()

    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0A0A2E),
        targetValue = Color(0xFF1A0A4E),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1A0A4E),
        targetValue = Color(0xFF0D1B4E),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, delayMillis = 1666, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0D1B4E),
        targetValue = Color(0xFF1B0A3E),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, delayMillis = 3333, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Animated aurora background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(color1, color2, color3),
                        radius = 1200f
                    )
                )
        )

        // Layer 2: Scaffold
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(screen.title) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                var text by remember { mutableStateOf("") }
                val listState = rememberLazyListState()

                val reversedActivities = remember(state.activities) { state.activities.reversed() }

                LaunchedEffect(reversedActivities.size) {
                    if (reversedActivities.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
                }

                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp + 16.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reversedActivities, key = { it.id.ifEmpty { it.name } }) { activity ->
                        ActivityCard(activity = activity)
                    }
                }

                // Input Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .blur(16.dp)
                    ) {
                        Box(modifier = Modifier.height(56.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                            IconButton(onClick = {}) { Icon(Icons.Default.Add, tint=Color.White, contentDescription = "Add") }
                            BasicTextField(
                                value = text, onValueChange = { text = it },
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                decorationBox = { inner ->
                                    if (text.isEmpty()) Text("Message", color=Color.White.copy(alpha=0.5f))
                                    inner()
                                }
                            )
                            AnimatedContent(
                                targetState = text.isNotBlank(),
                                transitionSpec = {
                                    (scaleIn(tween(200)) + fadeIn()).togetherWith(scaleOut(tween(200)) + fadeOut())
                                },
                                label = "SendButtonAnimation"
                            ) { hasText ->
                                if (hasText) {
                                    IconButton(
                                        onClick = {
                                            viewModel.sendMessage(screen.sessionId, text)
                                            text = ""
                                        },
                                        enabled = !state.isLoading
                                    ) {
                                        Box(Modifier.size(36.dp).background(Color(0xFF4A90E2), CircleShape), contentAlignment=Alignment.Center) {
                                            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color=Color.White, strokeWidth=2.dp)
                                            else Icon(Icons.Default.KeyboardArrowUp, tint=Color.White, contentDescription = "Send")
                                        }
                                    }
                                } else {
                                    IconButton(onClick = {}) { Icon(Icons.Default.Mic, tint=Color.White, contentDescription = "Mic") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: Activity) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it / 2 } + fadeIn(tween(300))
    ) {
        val agentMessaged = activity.agentMessaged
        val userMessaged = activity.userMessaged
        val planGenerated = activity.planGenerated?.plan

        when {
            userMessaged != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                    ) {
                        Text(
                            text = userMessaged.userMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
            agentMessaged != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                        modifier = Modifier.blur(0.dp) // glass look
                    ) {
                        Text(
                            text = agentMessaged.agentMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
            planGenerated != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📋 Plan", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                planGenerated.steps.forEach { step ->
                                    Text("${step.index + 1}. ${step.title}", fontWeight = FontWeight.Medium)
                                    if (step.description.isNotBlank()) {
                                        Text(
                                            step.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = activity.description.ifBlank { "System event" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
