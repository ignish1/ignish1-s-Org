package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BotRuleDao
import com.example.data.dao.ChatLogDao
import com.example.data.model.BotRule
import com.example.data.model.ChatLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BotRule::class, ChatLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun botRuleDao(): BotRuleDao
    abstract fun chatLogDao(): ChatLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsbot_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDefaultRules(getInstance(context).botRuleDao())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaultRules(dao: BotRuleDao) {
            val defaultRules = listOf(
                BotRule(
                    triggerKeyword = "hola",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "¡Hola! 👋 Bienvenido a nuestro canal oficial de WhatsApp. Escribe *menu* para ver todas las opciones disponibles.",
                    category = "Bienvenida",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "menu",
                    matchType = BotRule.MATCH_EXACT,
                    responseText = """📌 *MENÚ DE OPCIONES*
1️⃣ *Precios* y Catálogo
2️⃣ *Horarios* de atención
3️⃣ *Ubicación* de nuestra tienda
4️⃣ *Soporte* técnico
5️⃣ Hablar con un *asesor* humano

Escribe el número o la palabra clave para continuar.""",
                    category = "Menú",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "1",
                    matchType = BotRule.MATCH_EXACT,
                    responseText = "📦 *Catálogo y Precios:*\nNuestros planes van desde $19.99/mes. Puedes consultar nuestro catálogo digital completo con fotos y ofertas en: https://tienda.ejemplo.com",
                    category = "Ventas",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "precios",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "💰 *Lista de Precios:*\n• Plan Básico: $19 / mes\n• Plan Pro: $49 / mes\n• Plan Empresarial: $99 / mes\nTodos incluyen soporte 24/7 y garantía de satisfacción.",
                    category = "Ventas",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "horario",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "🕒 *Horarios de Atención:*\nLunes a Viernes: 9:00 AM - 7:00 PM\nSábados: 10:00 AM - 3:00 PM\nDomingos: Cerrado (atención automatizada por este bot).",
                    category = "Información",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "ubicacion",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "📍 *Nuestra Ubicación:*\nNos encontramos en Av. Principal #123, Zona Centro.\nEnlace en Google Maps: https://maps.google.com/?q=Av+Principal+123",
                    category = "Información",
                    isEnabled = true
                ),
                BotRule(
                    triggerKeyword = "asesor",
                    matchType = BotRule.MATCH_CONTAINS,
                    responseText = "👨‍💼 *Conectando con un Asesor Humano...*\nHe transferido tu solicitud a nuestro equipo. Un ejecutivo te responderá en breve. Por favor déjanos tu nombre completo.",
                    category = "Atención",
                    isEnabled = true
                )
            )
            dao.insertAll(defaultRules)
        }
    }
}
