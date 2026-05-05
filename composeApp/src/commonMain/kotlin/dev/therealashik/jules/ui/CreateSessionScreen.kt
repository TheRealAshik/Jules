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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSessionScreen(viewModel: JulesViewModel, state: UiState) {
    var title by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var showGalleryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Session") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                            Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add from Gallery")
                        }
                    }

                    if (state.selectedGalleryPrompts.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.selectedGalleryPrompts.forEach { item ->
                                InputChip(
                                    selected = false,
                                    onClick = { viewModel.toggleGalleryPrompt(item) },
                                    label = { Text(item.title) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
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
                        placeholder = { Text("What would you like me to do?") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(
                        onClick = { viewModel.createSession(prompt, title) },
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

        if (showGalleryDialog) {
            AlertDialog(
                onDismissRequest = { showGalleryDialog = false },
                title = { Text("Select Prompts") },
                text = {
                    if (state.promptItems.isEmpty()) {
                        Text("No prompts in gallery.")
                    } else {
                        LazyColumn {
                            items(state.promptItems) { item ->
                                val isSelected = state.selectedGalleryPrompts.contains(item)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleGalleryPrompt(item) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.title)
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
}
