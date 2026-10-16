package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatLogDao {
    @Query("SELECT * FROM chat_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ChatLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChatLog): Long

    @Query("DELETE FROM chat_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM chat_logs")
    fun getLogCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chat_logs WHERE isSimulated = 0")
    fun getRealReplyCount(): Flow<Int>
}
