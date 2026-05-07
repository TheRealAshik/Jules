package dev.therealashik.jules

import platform.UserNotifications.*

actual class NotificationService actual constructor() {
    init {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            // handle error or denial if needed
        }
    }

    actual fun notify(id: String, title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(body)

        val request = UNNotificationRequest.requestWithIdentifier(id, content, null)
        center.addNotificationRequest(request) { error ->
            // error handled silently
        }
    }

    actual fun cancel(id: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }
}
