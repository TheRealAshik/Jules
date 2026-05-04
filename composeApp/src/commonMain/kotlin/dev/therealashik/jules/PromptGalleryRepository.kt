package dev.therealashik.jules

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PromptItem(
    val id: String,
    val title: String,
    val prompt: String
)

class PromptGalleryRepository(private val store: KeyValueStore) {
    private val key = "prompt_gallery"

    fun getAll(): List<PromptItem> {
        val json = store.getString(key, "")
        if (json.isBlank()) return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(item: PromptItem) {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
        } else {
            current.add(item)
        }
        store.putString(key, Json.encodeToString(current))
    }

    fun delete(id: String) {
        val current = getAll().filter { it.id != id }
        store.putString(key, Json.encodeToString(current))
    }
}
