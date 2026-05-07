package dev.therealashik.jules.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
                title = { Text(Strings.SETTINGS) },
                navigationIcon = {
                    if (state.apiKey.isNotBlank()) {
                        IconButton(onClick = { viewModel.navigate(Screen.SessionList) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
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
                .padding(horizontal = Dimens.spacingL, vertical = Dimens.spacingXl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingL)
        ) {
            // — API Key section —
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.spacingL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingL)
                ) {
                    Text(Strings.JULES_API_KEY, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(Strings.API_KEY) },
                        placeholder = { Text(Strings.ENTER_YOUR_JULES_API_KEY) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showKey) Strings.HIDE_KEY else Strings.SHOW_KEY
                                )
                            }
                        }
                    )
                    FilledTonalButton(
                        onClick = { viewModel.saveApiKey(apiKey.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = apiKey.isNotBlank()
                    ) {
                        Text(Strings.SAVE)
                    }
                }
            }

            // — Features section —
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.spacingL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingL)
                ) {
                    Text(Strings.FEATURES, style = MaterialTheme.typography.titleMedium)

                    ListItem(
                        headlineContent = { Text(Strings.PROMPT_GALLERY) },
                        supportingContent = { Text(Strings.MANAGE_SAVED_PROMPTS_FOR_QUICK_USE) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.navigate(Screen.PromptGallery) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // — Customization section —
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.spacingL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingL)
                ) {
                    Text(Strings.CUSTOMIZATION, style = MaterialTheme.typography.titleMedium)

                    // Theme selector
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingS)) {
                        Text(Strings.THEME, style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingS)
                        ) {
                            ThemePreference.entries.forEach { pref ->
                                FilterChip(
                                    selected = state.themePreference == pref,
                                    onClick = { viewModel.saveThemePreference(pref) },
                                    label = { Text(pref.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }

                    // Compact session list toggle
                    ListItem(
                        headlineContent = { Text(Strings.COMPACT_SESSION_LIST) },
                        trailingContent = {
                            Switch(
                                checked = state.sessionListCompact,
                                onCheckedChange = { viewModel.saveSessionListCompact(it) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Page size slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(Strings.PAGE_SIZE.trimEnd(':', ' '), style = MaterialTheme.typography.labelLarge)
                            Text(state.pageSize.toString(), style = MaterialTheme.typography.labelLarge)
                        }
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
        }
    }
}
