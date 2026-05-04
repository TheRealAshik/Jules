package dev.therealashik.jules.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.sdk.models.GitHubBranch
import dev.therealashik.jules.sdk.models.Source

@OptIn(ExperimentalMaterial3Api::class)
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
                title = { Text("New Session") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            AnimatedVisibility(visible = true) {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prompt", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showGalleryDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add from Gallery", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add from Gallery")
                        }
                    }

                    if (state.selectedGalleryPrompts.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.selectedGalleryPrompts.forEach { item ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.toggleGalleryPrompt(item) },
                                    label = { Text(item.title) },
                                    trailingIcon = {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("Instructions") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        minLines = 5,
                        maxLines = 10,
                        placeholder = { Text("What would you like me to do?") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = sourceDropdownExpanded,
                        onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSource?.let { it.githubRepo?.repo ?: it.name } ?: "No repository",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Repository") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No repository") },
                                onClick = {
                                    selectedSource = null
                                    selectedBranch = null
                                    sourceDropdownExpanded = false
                                }
                            )
                            state.sources.forEach { source ->
                                DropdownMenuItem(
                                    text = { Text(source.githubRepo?.repo ?: source.name) },
                                    onClick = {
                                        selectedSource = source
                                        selectedBranch = source.githubRepo?.defaultBranch ?: source.githubRepo?.branches?.firstOrNull()
                                        sourceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedSource != null && selectedSource?.githubRepo?.branches?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = branchDropdownExpanded,
                            onExpandedChange = { branchDropdownExpanded = !branchDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedBranch?.displayName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Branch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = branchDropdownExpanded,
                                onDismissRequest = { branchDropdownExpanded = false }
                            ) {
                                selectedSource?.githubRepo?.branches?.forEach { branch ->
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(
                        onClick = {
                            viewModel.createSession(
                                prompt = prompt,
                                title = title,
                                source = selectedSource,
                                branch = selectedBranch
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = (prompt.isNotBlank() || state.selectedGalleryPrompts.isNotEmpty()) && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (showGalleryDialog) {
        AlertDialog(
            onDismissRequest = { showGalleryDialog = false },
            title = { Text("Select Prompts") },
            text = {
                if (state.promptItems.isEmpty()) {
                    Text("No saved prompts. Add some in the Prompt Gallery from Settings.")
                } else {
                    LazyColumn {
                        items(state.promptItems) { item ->
                            val isSelected = state.selectedGalleryPrompts.contains(item)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleGalleryPrompt(item) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(item.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        item.prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGalleryDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
