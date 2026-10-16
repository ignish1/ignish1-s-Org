package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.preference.BotPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WhatsAppNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var botEngine: BotEngine
    private lateinit var preferences: BotPreferences

    // Anti-spam throttling: remembers last sender and message hash
    private val recentReplies = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        botEngine = BotEngine(applicationContext)
        preferences = BotPreferences(applicationContext)
        Log.i(TAG, "WhatsAppNotificationListener creado y listo.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return
        val packageName = sbn.packageName

        // Target standard WhatsApp and WhatsApp Business
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }

        if (!preferences.isServiceActive) {
            Log.d(TAG, "Servicio WhatsBot desactivado por el usuario.")
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract title (sender name or phone) and text (message)
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() || text.isBlank()) return

        // Skip generic WhatsApp system notifications (e.g. "Checking for new messages", "Backup in progress", "WhatsApp Web")
        if (title.contains("WhatsApp", ignoreCase = true) ||
            text.contains("messages", ignoreCase = true) && text.contains("new", ignoreCase = true)
        ) {
            return
        }

        // Avoid self-reply loops: throttle within 5 seconds for the same text from the same sender
        val key = "$title:$text"
        val lastTime = recentReplies[key] ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastTime < 5000L) {
            Log.d(TAG, "Mensaje duplicado o reciente ignorado para evitar bucles: $key")
            return
        }
        recentReplies[key] = now

        // Find reply action with RemoteInput
        val (action, remoteInput) = findQuickReplyAction(notification)
        if (action == null || remoteInput == null) {
            Log.d(TAG, "No se encontró RemoteInput para responder a la notificación de $title")
            return
        }

        serviceScope.launch {
            try {
                // Optional delay to simulate natural human typing
                val delaySeconds = preferences.replyDelaySeconds
                if (delaySeconds > 0) {
                    delay(delaySeconds * 1000L)
                }

                val replyResult = botEngine.processIncomingMessage(
                    sender = title,
                    messageText = text,
                    isSimulated = false
                )

                // Build reply intent with RemoteInput results
                val replyIntent = Intent()
                val bundle = Bundle()
                bundle.putCharSequence(remoteInput.resultKey, replyResult.replyText)
                RemoteInput.addResultsToIntent(arrayOf(remoteInput), replyIntent, bundle)

                action.actionIntent.send(applicationContext, 0, replyIntent)
                Log.i(TAG, "Respuesta enviada a $title: ${replyResult.replyText}")
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando auto-respuesta", e)
            }
        }
    }

    private fun findQuickReplyAction(notification: Notification): Pair<Notification.Action?, RemoteInput?> {
        val actions = notification.actions ?: return Pair(null, null)
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.allowFreeFormInput) {
                    return Pair(action, remoteInput)
                }
            }
        }
        return Pair(null, null)
    }

    companion object {
        private const val TAG = "WhatsBotListener"
    }
}
