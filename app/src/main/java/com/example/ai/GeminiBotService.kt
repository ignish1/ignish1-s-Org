package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiBotService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateBotReply(
        userMessage: String,
        businessName: String,
        businessDescription: String,
        recentContext: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "🤖 *[Modo Asistente IA]* Hola, soy el asistente virtual de *$businessName*. En este momento estoy listo para ayudarte con: $businessDescription. (Para activar IA generativa en vivo con Gemini, configura tu clave en AI Studio)."
        }

        try {
            val systemPrompt = """
                Eres un asistente de atención al cliente para WhatsApp llamado 'WhatsBot' que trabaja para el negocio: '$businessName'.
                Descripción del negocio: $businessDescription.
                
                Instrucciones importantes:
                1. Responde de forma muy concisa, cordial y profesional (como un chat de WhatsApp auténtico).
                2. Usa emojis moderados y formato de WhatsApp (*negrita*, _cursiva_).
                3. Mantén las respuestas breves (máximo 3 párrafos o lista corta).
                4. Si el cliente pregunta por precios, horarios o contacto, responde basándote en la información dada o invítale cordialmente a agendar una cita o contactar a un asesor.
                5. Responde siempre en el mismo idioma que te hable el usuario (por defecto español).
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include recent turns if any
            for ((role, text) in recentContext.takeLast(4)) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", text))
                turnObj.put("parts", partsArray)
                contentsArray.put(turnObj)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            val rootJson = JSONObject()
            rootJson.put("contents", contentsArray)

            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            systemInstruction.put("parts", sysParts)
            rootJson.put("systemInstruction", systemInstruction)

            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.6)
            generationConfig.put("maxOutputTokens", 500)
            rootJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            // Supported modern model per gemini-api skill: gemini-3.5-flash
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val parsed = JSONObject(responseBody)
                val candidates = parsed.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text")
                        if (replyText.isNotBlank()) {
                            return@withContext replyText.trim()
                        }
                    }
                }
            }

            Log.w("GeminiBotService", "Gemini call non-successful: ${response.code} $responseBody")
            return@withContext "¡Hola! Gracias por escribir a *$businessName*. ¿En qué podemos ayudarte hoy? Escribe *menu* para ver nuestras opciones."
        } catch (e: Exception) {
            Log.e("GeminiBotService", "Error calling Gemini", e)
            return@withContext "¡Hola! En este momento estoy procesando tu solicitud para *$businessName*. Escribe *menu* para ver las opciones disponibles."
        }
    }
}
