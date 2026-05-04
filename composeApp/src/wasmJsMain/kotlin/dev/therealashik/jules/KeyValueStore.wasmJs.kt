package dev.therealashik.jules

import kotlinx.browser.localStorage

actual class KeyValueStore {
    actual fun getString(key: String, defaultValue: String) =
        localStorage.getItem(key) ?: defaultValue

    actual fun putString(key: String, value: String) =
        localStorage.setItem(key, value)
}
