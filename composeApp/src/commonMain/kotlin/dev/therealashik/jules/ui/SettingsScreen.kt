package dev.therealashik.jules.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JulesViewModel, state: UiState) {
    var apiKey by remember { mutableStateOf(state.apiKey) }
    var showKey by remember { mutableStateOf(false) }
    var pageSizeText by remember { mutableStateOf(state.pageSize.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    if (state.apiKey.isNotBlank()) {
                        IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Jules API Key", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                placeholder = { Text("Enter your Jules API key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKey) "Hide key" else "Show key"
                        )
                    }
                }
            )
            FilledTonalButton(
                onClick = { viewModel.saveApiKey(apiKey.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Features", style = MaterialTheme.typography.titleMedium)

            ListItem(
                headlineContent = { Text("Prompt Gallery") },
                supportingContent = { Text("Manage saved prompts for quick use") },
                modifier = Modifier.fillMaxWidth(),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            FilledTonalButton(
                onClick = { viewModel.navigate(Screen.PromptGallery) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Prompt Gallery")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Customization", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = pageSizeText,
                onValueChange = { pageSizeText = it.filter { char -> char.isDigit() } },
                label = { Text("Page Size") },
                placeholder = { Text("Enter page size (e.g. 30)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                onClick = { viewModel.savePageSize(pageSizeText.toIntOrNull() ?: 30) },
                modifier = Modifier.fillMaxWidth(),
                enabled = pageSizeText.isNotBlank() && (pageSizeText.toIntOrNull() ?: 0) > 0
            ) {
                Text("Save Page Size")
            }
        }
    }
}
