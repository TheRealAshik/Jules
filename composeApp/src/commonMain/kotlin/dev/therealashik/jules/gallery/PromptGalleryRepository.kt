package dev.therealashik.jules.gallery

import dev.therealashik.jules.KeyValueStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PromptGalleryRepository(private val store: KeyValueStore) {
    private val key = "prompt_gallery"

    fun getAll(): List<PromptItem> {
        val jsonString = store.getString(key)
        if (jsonString.isBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<PromptItem>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(item: PromptItem) {
        val current = getAll().toMutableList()
        current.add(item)
        val newJson = Json.encodeToString(current)
        store.putString(key, newJson)
    }

    fun delete(id: String) {
        val current = getAll().filter { it.id != id }
        val newJson = Json.encodeToString(current)
        store.putString(key, newJson)
    }
}
