package com.example.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class CloudApiResult {
    data class Success(val messageId: String) : CloudApiResult()
    data class Error(val errorMessage: String) : CloudApiResult()
}

class WhatsAppCloudApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun sendTextMessage(
        phoneNumberId: String,
        accessToken: String,
        recipientPhoneNumber: String,
        messageBody: String
    ): CloudApiResult = withContext(Dispatchers.IO) {
        if (phoneNumberId.isBlank()) {
            return@withContext CloudApiResult.Error("El Phone Number ID de WhatsApp Cloud API es requerido.")
        }
        if (accessToken.isBlank()) {
            return@withContext CloudApiResult.Error("El Token de Acceso de Meta Developer es requerido.")
        }
        if (recipientPhoneNumber.isBlank()) {
            return@withContext CloudApiResult.Error("El número del destinatario con código de país es requerido.")
        }

        try {
            val url = "https://graph.facebook.com/v20.0/${phoneNumberId.trim()}/messages"

            val jsonBody = JSONObject().apply {
                put("messaging_product", "whatsapp")
                put("recipient_type", "individual")
                put("to", recipientPhoneNumber.trim().replace("+", "").replace(" ", "").replace("-", ""))
                put("type", "text")
                put("text", JSONObject().put("body", messageBody))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseString)
                val messages = json.optJSONArray("messages")
                val msgId = messages?.optJSONObject(0)?.optString("id") ?: "OK"
                CloudApiResult.Success(msgId)
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseString).optJSONObject("error")
                    errJson?.optString("message") ?: "Error ${response.code}: $responseString"
                } catch (e: Exception) {
                    "Error ${response.code}: $responseString"
                }
                CloudApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e("WhatsAppCloudApi", "Error sending message", e)
            CloudApiResult.Error("Error de conexión: ${e.localizedMessage ?: e.message}")
        }
    }
}
