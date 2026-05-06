package dev.therealashik.jules

actual fun setCrashHandler(handler: (Throwable) -> Unit) {
    val default = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        handler(throwable)
        default?.uncaughtException(thread, throwable)
    }
}
