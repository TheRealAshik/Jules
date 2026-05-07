package dev.therealashik.jules.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeReviewScreen(viewModel: JulesViewModel, state: UiState, screen: Screen.CodeReview) {
    val clipboardManager = LocalClipboardManager.current

    val filePatches = remember(state.activities) {
        val allPatches = state.activities.flatMap { activity ->
            activity.artifacts.mapNotNull { it.changeSet?.gitPatch?.unidiffPatch }
        }.flatMap { parseUnifiedDiff(it) }

        allPatches.groupBy { it.filePath }.map { (filePath, patches) ->
            FilePatch(
                filePath = filePath,
                patch = patches.joinToString("\n") { it.patch },
                additions = patches.sumOf { it.additions }
            )
        }
    }

    var expandedStates by remember { mutableStateOf(filePatches.associate { it.filePath to false }) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(Strings.CODE_REVIEW, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigate(Screen.SessionDetail(screen.sessionId, screen.title)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        expandedStates = expandedStates.mapValues { false }
                    }) {
                        Text(Strings.COLLAPSE_ALL)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.spacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingM)
        ) {
            items(filePatches, key = { it.filePath }) { filePatch ->
                val isExpanded = expandedStates[filePatch.filePath] == true

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.spacingS)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedStates = expandedStates.toMutableMap().apply {
                                        this[filePatch.filePath] = !isExpanded
                                    }
                                }
                                .padding(Dimens.spacingM),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingM)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "M",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Text(
                                text = filePatch.filePath,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "+${filePatch.additions} ${Strings.ADDITIONS}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                            ) {
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = Strings.DOWNLOAD_PATCH,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(filePatch.patch)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = Strings.COPY_PATCH,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Box(modifier = Modifier.padding(Dimens.spacingM)) {
                                UnifiedDiffViewer(patch = filePatch.patch)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FilePatch(val filePath: String, val patch: String, val additions: Int)

fun parseUnifiedDiff(diff: String): List<FilePatch> {
    val filePatches = mutableListOf<FilePatch>()
    val lines = diff.lines()
    var currentFilePath = ""
    var currentPatchLines = mutableListOf<String>()
    var currentAdditions = 0

    for (line in lines) {
        if (line.startsWith("diff --git")) {
            if (currentFilePath.isNotEmpty()) {
                filePatches.add(FilePatch(currentFilePath, currentPatchLines.joinToString("\n"), currentAdditions))
            }

            val parts = line.split(" ")
            if (parts.size >= 4) {
                currentFilePath = parts[3].removePrefix("b/")
            }
            currentPatchLines = mutableListOf(line)
            currentAdditions = 0
        } else {
            currentPatchLines.add(line)
            if (line.startsWith("+") && !line.startsWith("+++")) {
                currentAdditions++
            }
        }
    }

    if (currentFilePath.isNotEmpty()) {
        filePatches.add(FilePatch(currentFilePath, currentPatchLines.joinToString("\n"), currentAdditions))
    }

    return filePatches
}
