package dev.therealashik.jules

import kotlin.native.setUnhandledExceptionHook

actual fun setCrashHandler(handler: (Throwable) -> Unit) {
    @OptIn(kotlin.experimental.ExperimentalNativeApi::class)
    setUnhandledExceptionHook({ throwable -> handler(throwable) })
}
