package dev.therealashik.jules

actual fun setCrashHandler(handler: (Throwable) -> Unit) {
    kotlinx.browser.window.addEventListener("error", { event ->
        val errorEvent = event as? org.w3c.dom.ErrorEvent
        val error = errorEvent?.error as? Throwable ?: Throwable(errorEvent?.message ?: "Unknown JS Error")
        handler(error)
    })

    kotlinx.browser.window.addEventListener("unhandledrejection", { event ->
        val promiseRejectionEvent = event.asDynamic()
        val reason = promiseRejectionEvent.reason
        val error = reason as? Throwable ?: Throwable(reason?.toString() ?: "Unhandled Promise Rejection")
        handler(error)
    })
}
