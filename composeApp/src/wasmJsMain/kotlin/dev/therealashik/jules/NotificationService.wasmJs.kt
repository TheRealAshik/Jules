package dev.therealashik.jules

actual class NotificationService actual constructor() {
    actual fun notify(id: String, title: String, body: String) {
        notifyWasm(title, body)
    }

    actual fun cancel(id: String) {}
}

@kotlin.js.ExperimentalWasmJsInterop
@JsFun("function(title, body) { if (window.Notification) { if (window.Notification.permission === 'granted') { new window.Notification(title, { body: body }); } else if (window.Notification.permission !== 'denied') { window.Notification.requestPermission().then(function(p) { if (p === 'granted') { new window.Notification(title, { body: body }); } }); } } }")
private external fun notifyWasm(title: String, body: String)
