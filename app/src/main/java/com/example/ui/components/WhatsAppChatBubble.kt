package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.ChatMessage
import com.example.ui.theme.WhatsAppBlueCheck
import com.example.ui.theme.WhatsAppBubbleIncomingDark
import com.example.ui.theme.WhatsAppBubbleIncomingLight
import com.example.ui.theme.WhatsAppBubbleOutgoingDark
import com.example.ui.theme.WhatsAppBubbleOutgoingLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isUser = message.isFromUser

    val bubbleBg = when {
        isUser -> if (isDark) WhatsAppBubbleOutgoingDark else WhatsAppBubbleOutgoingLight
        else -> if (isDark) WhatsAppBubbleIncomingDark else WhatsAppBubbleIncomingLight
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Rule or AI Indicator badge for incoming bot message
                if (!isUser && message.matchedRule != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (message.isAi) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        if (message.isAi) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "IA",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = message.matchedRule,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (message.isAi) MaterialTheme.colorScheme.primary else Color(0xFF128C7E)
                        )
                    }
                }

                // Message Text
                Text(
                    text = message.text,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                    color = if (isDark) Color(0xFFE9EDEF) else Color(0xFF111B21)
                )

                // Timestamp and checkmarks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        fontSize = 10.5.sp,
                        color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781)
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Entregado y Leído",
                            tint = WhatsAppBlueCheck,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
