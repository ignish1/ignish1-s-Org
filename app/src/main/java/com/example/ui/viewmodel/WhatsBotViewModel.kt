package com.example.ui.viewmodel

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.CloudApiResult
import com.example.api.WhatsAppCloudApiService
import com.example.data.database.AppDatabase
import com.example.data.model.BotRule
import com.example.data.model.ChatLog
import com.example.data.preference.BotPreferences
import com.example.service.BotEngine
import com.example.ui.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isServiceActive: Boolean = true,
    val isAiModeEnabled: Boolean = false,
    val businessName: String = "",
    val businessDescription: String = "",
    val fallbackMessage: String = "",
    val replyDelaySeconds: Int = 1,
    val cloudApiPhoneNumberId: String = "",
    val cloudApiAccessToken: String = "",
    val cloudApiRecipient: String = ""
)

class WhatsBotViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val preferences = BotPreferences(context)
    private val botEngine = BotEngine(context)
    private val cloudApiService = WhatsAppCloudApiService()

    // Rules from Room
    val allRules: StateFlow<List<BotRule>> = database.botRuleDao().getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Logs from Room
    val allLogs: StateFlow<List<ChatLog>> = database.chatLogDao().getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Settings
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsUiState> = _settings.asStateFlow()

    // Notification Permission Granted
    private val _isNotificationAccessGranted = MutableStateFlow(checkNotificationAccess())
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    // Chat Simulator State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            text = "¡Hola! 👋 Soy tu WhatsBot en modo de simulación interactiva. Escribe un mensaje o prueba una de las sugerencias rápidas abajo.",
            isFromUser = false,
            matchedRule = "Sistema WhatsBot"
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping.asStateFlow()

    // Cloud API Tester State
    private val _isCloudSending = MutableStateFlow(false)
    val isCloudSending: StateFlow<Boolean> = _isCloudSending.asStateFlow()

    private val _cloudSendStatus = MutableStateFlow<Pair<Boolean, String>?>(null)
    val cloudSendStatus: StateFlow<Pair<Boolean, String>?> = _cloudSendStatus.asStateFlow()

    fun refreshNotificationPermission() {
        _isNotificationAccessGranted.value = checkNotificationAccess()
    }

    private fun checkNotificationAccess(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }

    private fun loadSettings(): SettingsUiState {
        return SettingsUiState(
            isServiceActive = preferences.isServiceActive,
            isAiModeEnabled = preferences.isAiModeEnabled,
            businessName = preferences.businessName,
            businessDescription = preferences.businessDescription,
            fallbackMessage = preferences.fallbackMessage,
            replyDelaySeconds = preferences.replyDelaySeconds,
            cloudApiPhoneNumberId = preferences.cloudApiPhoneNumberId,
            cloudApiAccessToken = preferences.cloudApiAccessToken,
            cloudApiRecipient = preferences.cloudApiRecipient
        )
    }

    fun updateServiceActive(active: Boolean) {
        preferences.isServiceActive = active
        _settings.value = _settings.value.copy(isServiceActive = active)
    }

    fun updateAiMode(enabled: Boolean) {
        preferences.isAiModeEnabled = enabled
        _settings.value = _settings.value.copy(isAiModeEnabled = enabled)
    }

    fun updateBusinessDetails(
        name: String,
        description: String,
        fallback: String,
        delaySeconds: Int
    ) {
        preferences.businessName = name
        preferences.businessDescription = description
        preferences.fallbackMessage = fallback
        preferences.replyDelaySeconds = delaySeconds

        _settings.value = _settings.value.copy(
            businessName = name,
            businessDescription = description,
            fallbackMessage = fallback,
            replyDelaySeconds = delaySeconds
        )
    }

    fun updateCloudApiCredentials(
        phoneId: String,
        token: String,
        recipient: String
    ) {
        preferences.cloudApiPhoneNumberId = phoneId
        preferences.cloudApiAccessToken = token
        preferences.cloudApiRecipient = recipient

        _settings.value = _settings.value.copy(
            cloudApiPhoneNumberId = phoneId,
            cloudApiAccessToken = token,
            cloudApiRecipient = recipient
        )
    }

    // Rules management
    fun saveRule(rule: BotRule) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                database.botRuleDao().insertRule(rule)
            } else {
                database.botRuleDao().updateRule(rule)
            }
        }
    }

    fun toggleRule(rule: BotRule) {
        viewModelScope.launch {
            database.botRuleDao().updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(rule: BotRule) {
        viewModelScope.launch {
            database.botRuleDao().deleteRule(rule)
        }
    }

    fun resetDefaultRules() {
        viewModelScope.launch {
            val defaults = listOf(
                BotRule(
                    triggerKeyword = "hola",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "¡Hola! 👋 Bienvenido a nuestro WhatsApp. Escribe *menu* para ver las opciones.",
                    category = "Bienvenida",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "menu",
                    matchType = BotRule.MATCH_EXACT,
                    responseText = """📌 *MENÚ PRINCIPAL*
1️⃣ *Precios* y Planes
2️⃣ *Horarios* de atención
3️⃣ *Ubicación*
4️⃣ *Soporte* técnico
5️⃣ Hablar con un *asesor* humano

Escribe la opción deseada para más información.""",
                    category = "Menú",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "precios",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "💰 *Nuestros Planes:*\n• Básico: $19/mes\n• Profesional: $49/mes\n• Enterprise: $99/mes\nIncluyen soporte prioritario y activación inmediata.",
                    category = "Ventas",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "horario",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "🕒 *Horarios de Atención:*\nLunes a Viernes: 9:00 AM - 7:00 PM\nSábados: 10:00 AM - 3:00 PM\n*Nota:* Este bot atiende 24/7.",
                    category = "Información",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "ubicacion",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "📍 *Dirección física:*\nAv. Central 500, Oficina 4B.\nGoogle Maps: https://maps.google.com/?q=Av+Central+500",
                    category = "Información",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "asesor",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "👨‍💼 *Asesor Humano:*\nUn agente de soporte revisará tu mensaje y se comunicará contigo lo antes posible.",
                    category = "Atención",
                    isEnabled = true
                )
            )
            for (rule in defaults) {
                database.botRuleDao().insertRule(rule)
            }
        }
    }

    // Chat Simulator
    fun sendSimulatedMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = ChatMessage(text = userText, isFromUser = true)
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isBotTyping.value = true
            // Small simulated delay
            delay(800)

            val reply = botEngine.processIncomingMessage(
                sender = "Simulador (+54 9 11 1234-5678)",
                messageText = userText,
                isSimulated = true
            )

            _isBotTyping.value = false
            val botMsg = ChatMessage(
                text = reply.replyText,
                isFromUser = false,
                matchedRule = reply.matchedRuleName,
                isAi = reply.isAiGenerated
            )
            val updated = _chatMessages.value.toMutableList()
            updated.add(botMsg)
            _chatMessages.value = updated
        }
    }

    fun clearSimulatedChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "¡Hola! 👋 Soy tu WhatsBot en modo de simulación interactiva. Escribe un mensaje o prueba una de las sugerencias rápidas abajo.",
                isFromUser = false,
                matchedRule = "Sistema WhatsBot"
            )
        )
    }

    // Clear logs
    fun clearLogs() {
        viewModelScope.launch {
            database.chatLogDao().clearAllLogs()
        }
    }

    // Meta WhatsApp Cloud API test message
    fun sendCloudApiTest(messageText: String) {
        val phoneId = _settings.value.cloudApiPhoneNumberId
        val token = _settings.value.cloudApiAccessToken
        val recipient = _settings.value.cloudApiRecipient

        viewModelScope.launch {
            _isCloudSending.value = true
            _cloudSendStatus.value = null

            val result = cloudApiService.sendTextMessage(
                phoneNumberId = phoneId,
                accessToken = token,
                recipientPhoneNumber = recipient,
                messageBody = messageText
            )

            _isCloudSending.value = false
            when (result) {
                is CloudApiResult.Success -> {
                    _cloudSendStatus.value = Pair(true, "¡Mensaje enviado con éxito! ID: ${result.messageId}")
                }
                is CloudApiResult.Error -> {
                    _cloudSendStatus.value = Pair(false, result.errorMessage)
                }
            }
        }
    }

    fun dismissCloudStatus() {
        _cloudSendStatus.value = null
    }
}
