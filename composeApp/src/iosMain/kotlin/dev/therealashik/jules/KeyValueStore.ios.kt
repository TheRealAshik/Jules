package dev.therealashik.jules

import platform.Foundation.NSUserDefaults

actual class KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, defaultValue: String) =
        defaults.stringForKey(key) ?: defaultValue

    actual fun putString(key: String, value: String) =
        defaults.setObject(value, key)
}
