package dev.therealashik.jules

import java.util.Properties
import java.io.File

actual fun getApiKey(): String {
    val props = Properties()
    val localPropsFile = File("local.properties")
    if (localPropsFile.exists()) {
        props.load(localPropsFile.inputStream())
    }
    return props.getProperty("jules.api.key", "")
}
