package dev.therealashik.jules.gallery

import kotlinx.serialization.Serializable

@Serializable
data class PromptItem(
    val id: String,
    val title: String,
    val prompt: String
)
