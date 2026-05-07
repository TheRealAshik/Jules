package dev.therealashik.jules

import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.event.ActionListener
import java.awt.Frame

actual class NotificationService actual constructor() {
    actual fun notify(id: String, title: String, body: String) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            if (tray.trayIcons.isEmpty()) {
                val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                val graphics = image.createGraphics()
                graphics.color = java.awt.Color.BLUE
                graphics.fillRect(0, 0, 16, 16)
                graphics.dispose()

                val trayIcon = TrayIcon(image, "Jules")
                trayIcon.isImageAutoSize = true
                trayIcon.addActionListener {
                    Frame.getFrames().firstOrNull { it.isVisible }?.let { frame ->
                        frame.state = Frame.NORMAL
                        frame.toFront()
                        frame.requestFocus()
                    }
                }
                try {
                    tray.add(trayIcon)
                } catch (e: Exception) {
                    return
                }
            }
            tray.trayIcons.firstOrNull()?.displayMessage(title, body, TrayIcon.MessageType.INFO)
        }
    }

    actual fun cancel(id: String) {}
}
