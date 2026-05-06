package dev.therealashik.jules

import android.content.Context
import android.content.SharedPreferences

actual class KeyValueStore {
    private val prefs: SharedPreferences =
        AppContext.get().getSharedPreferences("jules_prefs", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String) =
        prefs.getString(key, defaultValue) ?: defaultValue

    actual fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()
}

object AppContext {
    private lateinit var context: Context
    fun init(ctx: Context) { context = ctx.applicationContext }
    fun get(): Context = context
}
