package dev.therealashik.jules.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.gallery.PromptItem
import dev.therealashik.jules.sdk.models.Source
import dev.therealashik.jules.sdk.models.GitHubBranch
import dev.therealashik.jules.sdk.models.SourceContext
import dev.therealashik.jules.sdk.models.GitHubRepoContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSessionScreen(viewModel: JulesViewModel, state: UiState) {
    var title by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var showGalleryDialog by remember { mutableStateOf(false) }

    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var selectedBranch by remember { mutableStateOf<GitHubBranch?>(null) }

    var sourceDropdownExpanded by remember { mutableStateOf(false) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.NEW_SESSION) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = Strings.BACK)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacingL)
        ) {
            AnimatedVisibility(visible = true) {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(Strings.TITLE_OPTIONAL) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingL))

                    if (state.sources.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = sourceDropdownExpanded,
                            onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSource?.name ?: Strings.SELECT_REPOSITORY_OPTIONAL,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = sourceDropdownExpanded,
                                onDismissRequest = { sourceDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(Strings.NONE) },
                                    onClick = {
                                        selectedSource = null
                                        selectedBranch = null
                                        sourceDropdownExpanded = false
                                    }
                                )
                                state.sources.forEach { source ->
                                    DropdownMenuItem(
                                        text = { Text(source.name) },
                                        onClick = {
                                            selectedSource = source
                                            selectedBranch = source.githubRepo?.defaultBranch
                                            sourceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacingL))

                        val branches = selectedSource?.githubRepo?.branches
                        if (!branches.isNullOrEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = branchDropdownExpanded,
                                onExpandedChange = { branchDropdownExpanded = !branchDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedBranch?.displayName ?: Strings.SELECT_BRANCH,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = branchDropdownExpanded,
                                    onDismissRequest = { branchDropdownExpanded = false }
                                ) {
                                    branches.forEach { branch ->
                                        DropdownMenuItem(
                                            text = { Text(branch.displayName) },
                                            onClick = {
                                                selectedBranch = branch
                                                branchDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.spacingL))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Strings.PROMPT, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showGalleryDialog = true }) {
                            Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(Dimens.bubbleCornerRadius))
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text(Strings.ADD_FROM_GALLERY)
                        }
                    }

                    if (state.selectedGalleryPrompts.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.spacingS),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS)
                        ) {
                            state.selectedGalleryPrompts.forEach { item ->
                                InputChip(
                                    selected = false,
                                    onClick = { viewModel.toggleGalleryPrompt(item) },
                                    label = { Text(item.title) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = Strings.REMOVE,
                                            modifier = Modifier.size(Dimens.spacingL)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        minLines = 5,
                        maxLines = 10,
                        placeholder = { Text(Strings.WHAT_WOULD_YOU_LIKE_ME_TO_DO) }
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingXl))
                    FilledTonalButton(
                        onClick = {
                            val sourceContext = selectedSource?.let { src ->
                                SourceContext(
                                    source = src.name,
                                    githubRepoContext = selectedBranch?.let { branch ->
                                        GitHubRepoContext(startingBranch = branch.displayName)
                                    }
                                )
                            }
                            viewModel.createSession(prompt, title, sourceContext)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.createButtonHeight),
                        enabled = (prompt.isNotBlank() || state.selectedGalleryPrompts.isNotEmpty()) && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.spacingXl),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                strokeWidth = Dimens.spacingXxs
                            )
                        } else {
                            Text(Strings.CREATE)
                        }
                    }
                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(Dimens.spacingS))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (showGalleryDialog) {
            AlertDialog(
                onDismissRequest = { showGalleryDialog = false },
                title = { Text(Strings.SELECT_PROMPTS) },
                text = {
                    if (state.promptItems.isEmpty()) {
                        Text(Strings.NO_PROMPTS_IN_GALLERY)
                    } else {
                        LazyColumn {
                            items(state.promptItems) { item ->
                                val isSelected = state.selectedGalleryPrompts.contains(item)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spacingXs),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleGalleryPrompt(item) }
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.spacingS))
                                    Text(item.title)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGalleryDialog = false }) {
                        Text(Strings.DONE)
                    }
                }
            )
        }
    }
}
