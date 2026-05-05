package dev.therealashik.jules.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JulesViewModel, state: UiState) {
    var apiKey by remember { mutableStateOf(state.apiKey) }
    var showKey by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // — API Key section —
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

            // — Features section —
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

            // — Customization section —
            Text("Customization", style = MaterialTheme.typography.titleMedium)

            // Theme selector
            Text("Theme", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePreference.entries.forEach { pref ->
                    FilterChip(
                        selected = state.themePreference == pref,
                        onClick = { viewModel.saveThemePreference(pref) },
                        label = { Text(pref.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Page size slider
            Text(
                "Page size: ${state.pageSize}",
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = state.pageSize.toFloat(),
                onValueChange = { viewModel.savePageSize(it.roundToInt()) },
                valueRange = 10f..100f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
