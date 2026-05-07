package dev.therealashik.jules

import kotlinx.browser.window

actual class NotificationService actual constructor() {
    actual fun notify(id: String, title: String, body: String) {
        val Notification = window.asDynamic().Notification
        if (Notification != undefined) {
            if (Notification.permission == "granted") {
                createNotification(title, body)
            } else if (Notification.permission != "denied") {
                Notification.requestPermission().then { p: dynamic ->
                    if (p == "granted") {
                        createNotification(title, body)
                    }
                }
            }
        }
    }

    actual fun cancel(id: String) {}

    private fun createNotification(title: String, body: String) {
        val Notification = window.asDynamic().Notification
        val options = js("{}")
        options.body = body
        js("new Notification(title, options)")
    }
}
