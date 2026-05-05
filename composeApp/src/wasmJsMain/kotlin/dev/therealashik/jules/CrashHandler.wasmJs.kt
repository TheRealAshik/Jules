package dev.therealashik.jules

import kotlinx.browser.window
import org.w3c.dom.ErrorEvent
import org.w3c.dom.events.Event

actual fun setCrashHandler(handler: (Throwable) -> Unit) {
    window.addEventListener("error", { event: Event ->
        val errorEvent = event as? ErrorEvent
        val msg = errorEvent?.message ?: "Unknown Wasm Error"
        handler(Throwable(msg))
    })

    window.addEventListener("unhandledrejection", { event: Event ->
        handler(Throwable("Unhandled Promise Rejection"))
    })
}
