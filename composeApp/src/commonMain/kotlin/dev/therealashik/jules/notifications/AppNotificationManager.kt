package dev.therealashik.jules.notifications

import dev.therealashik.jules.sdk.models.SessionState

data class NotificationEvent(
    val sessionId: String,
    val sessionTitle: String,
    val state: SessionState
)

expect class AppNotificationManager() {
    fun requestPermission()
    fun notifySessionEvent(event: NotificationEvent)
}
