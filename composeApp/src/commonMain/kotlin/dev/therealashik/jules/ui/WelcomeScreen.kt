package dev.therealashik.jules.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val AgentSteps = listOf(
    "Cloning repository...",
    "Analyzing codebase...",
    "Reading AGENTS.md...",
    "Indexing project files...",
    "Scanning dependency tree...",
    "Parsing build configurations...",
    "Understanding system architecture...",
    "Checking git branch & history...",
    "Identifying task requirements...",
    "Searching relevant symbols...",
    "Constructing execution plan...",
    "Verifying Kotlin types & signatures...",
    "Running static code analysis...",
    "Inspecting API contracts...",
    "Generating code solution...",
    "Applying atomic file changes...",
    "Running unit test suite...",
    "Verifying edge cases...",
    "Formatting code style...",
    "Plan generated & ready for review!"
)

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Color definitions aligned with the design vision
    val circleBg = if (isDark) Color(0xFF284820) else Color(0xFFC3EBA2)
    val sparkleColor = if (isDark) Color(0xFFD0F0C0) else Color(0xFF193B11)
    val buttonBg = if (isDark) Color(0xFF386A20) else Color(0xFF325A1E)
    val buttonContent = Color.White

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingL)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimens.spacingXl))

                // Top Mint Sparkle Badge
                Surface(
                    shape = CircleShape,
                    color = circleBg,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = Strings.SPARKLE,
                            style = MaterialTheme.typography.displayMedium,
                            color = sparkleColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingXl))

                Text(
                    text = Strings.MEET_JULES,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.spacingM))

                Text(
                    text = Strings.WELCOME_DESCRIPTION,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimens.spacingM)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingXxl))

                // Visual Animated Code Card
                WelcomeVisualElement(isDark = isDark)

                Spacer(modifier = Modifier.height(Dimens.spacingXxl))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacingM),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Strings.CODING_JOURNEY_STARTS_HERE,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.spacingM))

                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonContent
                    )
                ) {
                    Text(
                        text = Strings.GET_STARTED,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeVisualElement(isDark: Boolean) {
    var stepIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2200L)
            stepIndex = (stepIndex + 1) % AgentSteps.size
        }
    }

    val cardBg = if (isDark) Color(0xFF232523) else Color(0xFFF1F2F0)
    val codeAreaBg = if (isDark) Color(0xFF1B1D1B) else Color(0xFFE6E8E4)
    val badgeBg = if (isDark) Color(0xFF2E4D26) else Color(0xFFD4E9C8)
    val badgeText = if (isDark) Color(0xFFD2F3C3) else Color(0xFF224219)
    val activeText = if (isDark) Color(0xFF8FD87B) else Color(0xFF2D5722)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacingXs),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Window Control Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE56A5D))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5C05D))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5DE57A))
                    )
                }

                // jules-agent Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.spacingS, vertical = Dimens.spacingXxs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = badgeText
                        )
                        Text(
                            text = "jules-agent",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingM))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = codeAreaBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.spacingM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)
                ) {
                    // Animated Step Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Strings.SPARKLE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = activeText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingXs))
                        AnimatedContent(
                            targetState = AgentSteps[stepIndex],
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "AgentStepAnimation"
                        ) { currentStep ->
                            Text(
                                text = currentStep,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = activeText
                            )
                        }
                    }

                    Text(
                        text = "val session = jules.createSession(\"Fix bug\")",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "✓ Plan generated & ready for review",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
