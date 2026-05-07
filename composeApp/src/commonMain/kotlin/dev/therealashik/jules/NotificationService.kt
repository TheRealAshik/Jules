package dev.therealashik.jules

expect class NotificationService() {
    fun notify(id: String, title: String, body: String)
    fun cancel(id: String)
}
