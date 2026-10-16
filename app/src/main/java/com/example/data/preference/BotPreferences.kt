package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences

class BotPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("whatsbot_prefs", Context.MODE_PRIVATE)

    var isServiceActive: Boolean
        get() = prefs.getBoolean("is_service_active", true)
        set(value) = prefs.edit().putBoolean("is_service_active", value).apply()

    var isAiModeEnabled: Boolean
        get() = prefs.getBoolean("is_ai_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("is_ai_mode_enabled", value).apply()

    var businessName: String
        get() = prefs.getString("business_name", "Mi Negocio Digital") ?: "Mi Negocio Digital"
        set(value) = prefs.edit().putString("business_name", value).apply()

    var businessDescription: String
        get() = prefs.getString(
            "business_description",
            "Somos una tienda de tecnología y soluciones digitales. Ofrecemos asesoría, venta de dispositivos y soporte al cliente."
        ) ?: ""
        set(value) = prefs.edit().putString("business_description", value).apply()

    var fallbackMessage: String
        get() = prefs.getString(
            "fallback_message",
            "¡Hola! No logré entender tu consulta. Por favor escribe *menu* para ver nuestras opciones o déjanos tu duda detallada."
        ) ?: ""
        set(value) = prefs.edit().putString("fallback_message", value).apply()

    var replyDelaySeconds: Int
        get() = prefs.getInt("reply_delay_seconds", 1)
        set(value) = prefs.edit().putInt("reply_delay_seconds", value).apply()

    // WhatsApp Cloud API credentials
    var cloudApiPhoneNumberId: String
        get() = prefs.getString("cloud_api_phone_id", "") ?: ""
        set(value) = prefs.edit().putString("cloud_api_phone_id", value).apply()

    var cloudApiAccessToken: String
        get() = prefs.getString("cloud_api_token", "") ?: ""
        set(value) = prefs.edit().putString("cloud_api_token", value).apply()

    var cloudApiRecipient: String
        get() = prefs.getString("cloud_api_recipient", "") ?: ""
        set(value) = prefs.edit().putString("cloud_api_recipient", value).apply()
}
