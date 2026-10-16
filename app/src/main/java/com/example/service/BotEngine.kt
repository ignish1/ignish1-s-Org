package com.example.service

import android.content.Context
import com.example.ai.GeminiBotService
import com.example.data.database.AppDatabase
import com.example.data.model.BotRule
import com.example.data.model.ChatLog
import com.example.data.preference.BotPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BotReplyResult(
    val replyText: String,
    val matchedRuleName: String,
    val isAiGenerated: Boolean = false
)

class BotEngine(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val preferences = BotPreferences(context)
    private val geminiService = GeminiBotService()

    suspend fun processIncomingMessage(
        sender: String,
        messageText: String,
        isSimulated: Boolean = false
    ): BotReplyResult {
        val trimmed = messageText.trim()
        val normalized = normalizeText(trimmed)

        // 1. Fetch active rules
        val activeRules = database.botRuleDao().getActiveRules()

        // 2. Exact match check first
        var matchedRule: BotRule? = activeRules.firstOrNull { rule ->
            rule.matchType == BotRule.MATCH_EXACT &&
                    normalizeText(rule.triggerKeyword) == normalized
        }

        // 3. Starts with check
        if (matchedRule == null) {
            matchedRule = activeRules.firstOrNull { rule ->
                rule.matchType == BotRule.MATCH_STARTS_WITH &&
                        normalized.startsWith(normalizeText(rule.triggerKeyword))
            }
        }

        // 4. Contains check
        if (matchedRule == null) {
            matchedRule = activeRules.firstOrNull { rule ->
                rule.matchType == BotRule.MATCH_CONTAINS &&
                        normalized.contains(normalizeText(rule.triggerKeyword))
            }
        }

        val result: BotReplyResult
        if (matchedRule != null) {
            database.botRuleDao().incrementHitCount(matchedRule.id)
            val replacedText = replaceVariables(matchedRule.responseText)
            result = BotReplyResult(
                replyText = replacedText,
                matchedRuleName = "Regla: [${matchedRule.triggerKeyword}] (${matchedRule.category})",
                isAiGenerated = false
            )
        } else if (preferences.isAiModeEnabled) {
            // Intelligent Gemini Response
            val aiResponse = geminiService.generateBotReply(
                userMessage = trimmed,
                businessName = preferences.businessName,
                businessDescription = preferences.businessDescription
            )
            result = BotReplyResult(
                replyText = aiResponse,
                matchedRuleName = "Asistente IA (Gemini)",
                isAiGenerated = true
            )
        } else {
            // Fallback message
            val fallback = replaceVariables(preferences.fallbackMessage)
            result = BotReplyResult(
                replyText = fallback,
                matchedRuleName = "Mensaje por defecto (Fallback)",
                isAiGenerated = false
            )
        }

        // Log the exchange in the database
        val log = ChatLog(
            sender = sender,
            incomingMessage = trimmed,
            repliedMessage = result.replyText,
            matchedRule = result.matchedRuleName,
            timestamp = System.currentTimeMillis(),
            isSimulated = isSimulated
        )
        database.chatLogDao().insertLog(log)

        return result
    }

    private fun replaceVariables(text: String): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = Date()

        return text
            .replace("{negocio}", preferences.businessName)
            .replace("{empresa}", preferences.businessName)
            .replace("{hora}", timeFormat.format(now))
            .replace("{fecha}", dateFormat.format(now))
    }

    private fun normalizeText(input: String): String {
        return input.lowercase(Locale.getDefault())
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("¿", "")
            .replace("?", "")
            .replace("¡", "")
            .replace("!", "")
            .trim()
    }
}
