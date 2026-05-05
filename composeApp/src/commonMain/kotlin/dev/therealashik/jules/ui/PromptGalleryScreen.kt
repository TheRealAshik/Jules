package dev.therealashik.jules.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.therealashik.jules.gallery.PromptItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptGalleryScreen(viewModel: JulesViewModel, state: UiState) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.PROMPT_GALLERY) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigate(Screen.Settings) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = Strings.ADD_PROMPT_LOWER) },
                text = { Text(Strings.ADD_PROMPT) }
            )
        }
    ) { paddingValues ->
        if (state.promptItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(Strings.NO_PROMPTS_SAVED_YET, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.spacingS),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)
            ) {
                items(state.promptItems) { item ->
                    PromptCard(
                        item = item,
                        onDelete = { viewModel.deletePrompt(item.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddPromptDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, prompt ->
                    viewModel.savePrompt(title, prompt)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun PromptCard(item: PromptItem, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingL).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(Dimens.spacingXl).padding(start = Dimens.spacingS)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = Strings.DELETE,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.iconSizeMedium)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacingS))
            Text(
                text = item.prompt,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AddPromptDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.ADD_PROMPT) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Strings.TITLE) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text(Strings.PROMPT) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, prompt) },
                enabled = title.isNotBlank() && prompt.isNotBlank()
            ) {
                Text(Strings.SAVE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.CANCEL)
            }
        }
    )
}
