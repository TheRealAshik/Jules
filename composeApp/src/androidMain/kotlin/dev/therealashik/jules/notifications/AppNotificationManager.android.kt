package dev.therealashik.jules.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.therealashik.jules.AppContext
import dev.therealashik.jules.MainActivity
import dev.therealashik.jules.sdk.models.SessionState

actual class AppNotificationManager {
    private val context: Context get() = AppContext.get()

    companion object {
        const val CHANNEL_ID = "jules_session_events"
        const val CHANNEL_NAME = "Jules Session Updates"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Jules plan approvals, completions, and session updates"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    actual fun requestPermission() {
        // Notification permissions requested on Android 13+
    }

    actual fun notifySessionEvent(event: NotificationEvent) {
        val (title, body) = when (event.state) {
            SessionState.AWAITING_PLAN_APPROVAL ->
                "Plan Approval Needed" to "Jules prepared a plan for '${event.sessionTitle.ifBlank { "Session" }}' and is waiting for review."
            SessionState.AWAITING_USER_FEEDBACK ->
                "Input Required" to "Jules needs your input on '${event.sessionTitle.ifBlank { "Session" }}'."
            SessionState.COMPLETED ->
                "Task Completed" to "Jules finished work on '${event.sessionTitle.ifBlank { "Session" }}'."
            SessionState.FAILED ->
                "Session Failed" to "Session '${event.sessionTitle.ifBlank { "Session" }}' requires attention."
            else -> return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("session_id", event.sessionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(event.sessionId.hashCode(), builder.build())
        } catch (e: Exception) {
            // Permission or notification disabled
        }
    }
}
