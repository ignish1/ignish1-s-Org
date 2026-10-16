package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bot_rules")
data class BotRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerKeyword: String,
    val matchType: String = MATCH_CONTAINS, // EXACT, CONTAINS, STARTS_WITH
    val responseText: String,
    val category: String = "General",
    val isEnabled: Boolean = true,
    val hitCount: Int = 0
) {
    companion object {
        const val MATCH_EXACT = "EXACT"
        const val MATCH_CONTAINS = "CONTAINS"
        const val MATCH_STARTS_WITH = "STARTS_WITH"
    }
}
