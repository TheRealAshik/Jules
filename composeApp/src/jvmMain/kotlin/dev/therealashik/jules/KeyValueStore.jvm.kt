package dev.therealashik.jules

import java.io.File
import java.util.Properties

actual class KeyValueStore {
    private val file = File(System.getProperty("user.home"), ".jules_prefs")
    private val props = Properties().also { if (file.exists()) it.load(file.inputStream()) }

    actual fun getString(key: String, defaultValue: String) =
        props.getProperty(key, defaultValue)

    actual fun putString(key: String, value: String) {
        props.setProperty(key, value)
        props.store(file.outputStream(), null)
    }
}
