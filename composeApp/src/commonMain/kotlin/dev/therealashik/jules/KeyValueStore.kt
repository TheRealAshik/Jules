package dev.therealashik.jules

expect class KeyValueStore() {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}
