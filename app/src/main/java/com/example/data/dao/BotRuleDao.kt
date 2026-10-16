package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BotRule
import kotlinx.coroutines.flow.Flow

@Dao
interface BotRuleDao {
    @Query("SELECT * FROM bot_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<BotRule>>

    @Query("SELECT * FROM bot_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<BotRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: BotRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<BotRule>)

    @Update
    suspend fun updateRule(rule: BotRule)

    @Delete
    suspend fun deleteRule(rule: BotRule)

    @Query("UPDATE bot_rules SET hitCount = hitCount + 1 WHERE id = :ruleId")
    suspend fun incrementHitCount(ruleId: Long)

    @Query("SELECT COUNT(*) FROM bot_rules")
    suspend fun getRuleCount(): Int
}
