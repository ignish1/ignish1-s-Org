package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.viewmodel.WhatsBotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudApiScreen(
    viewModel: WhatsBotViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isSending by viewModel.isCloudSending.collectAsStateWithLifecycle()
    val sendStatus by viewModel.cloudSendStatus.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var phoneId by remember(settings.cloudApiPhoneNumberId) { mutableStateOf(settings.cloudApiPhoneNumberId) }
    var token by remember(settings.cloudApiAccessToken) { mutableStateOf(settings.cloudApiAccessToken) }
    var recipient by remember(settings.cloudApiRecipient) { mutableStateOf(settings.cloudApiRecipient) }
    var testMessage by remember { mutableStateOf("¡Hola! Mensaje de prueba enviado desde WhatsBot Cloud API Tester 🚀") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("WhatsApp Cloud API (Meta)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF1F2C34) else WhatsAppGreenDark
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Banner
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isDark) Color(0xFF1A282F) else Color(0xFFE8F5E9)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("API Oficial de Meta (WhatsApp Business)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Conecta tu bot directamente con los servidores de Meta sin necesidad de tener tu teléfono encendido.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Architecture Walkthrough Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📋 ¿Cómo funciona un Bot de WhatsApp en la Nube?", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    StepRow(
                        step = "1",
                        title = "Regístrate en Meta for Developers",
                        desc = "Ve a developers.facebook.com, crea una App tipo 'Negocios' y añade el producto 'WhatsApp'."
                    )
                    StepRow(
                        step = "2",
                        title = "Obtén tus Credenciales",
                        desc = "En el panel de WhatsApp obtendrás tu 'Phone Number ID' de prueba y un 'Token Temporal' de 24h (o token de sistema permanente)."
                    )
                    StepRow(
                        step = "3",
                        title = "Configura el Webhook",
                        desc = "Meta enviará cada mensaje entrante por POST JSON a tu servidor (Node.js, Python o Firebase Cloud Functions) donde evalúas las reglas y devuelves la respuesta."
                    )
                    StepRow(
                        step = "4",
                        title = "Envía la respuesta",
                        desc = "Haces una petición POST a https://graph.facebook.com/v20.0/{ID}/messages con el token de portador."
                    )
                }
            }

            // Live API Tester
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Api, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probador en Vivo de Envío (Cloud API)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedTextField(
                        value = phoneId,
                        onValueChange = {
                            phoneId = it
                            viewModel.updateCloudApiCredentials(it, token, recipient)
                        },
                        label = { Text("Phone Number ID") },
                        placeholder = { Text("Ej: 104589234857291") },
                        modifier = Modifier.fillMaxWidth().testTag("cloud_phone_id_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = token,
                        onValueChange = {
                            token = it
                            viewModel.updateCloudApiCredentials(phoneId, it, recipient)
                        },
                        label = { Text("Access Token de Meta (Bearer)") },
                        placeholder = { Text("EAA...") },
                        modifier = Modifier.fillMaxWidth().testTag("cloud_token_input"),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = recipient,
                        onValueChange = {
                            recipient = it
                            viewModel.updateCloudApiCredentials(phoneId, token, it)
                        },
                        label = { Text("Número Destinatario (Con código de país)") },
                        placeholder = { Text("Ej: 5491123456789 o 5215512345678") },
                        modifier = Modifier.fillMaxWidth().testTag("cloud_recipient_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = testMessage,
                        onValueChange = { testMessage = it },
                        label = { Text("Cuerpo del Mensaje a Enviar") },
                        modifier = Modifier.fillMaxWidth().height(85.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            viewModel.sendCloudApiTest(testMessage)
                        },
                        enabled = !isSending && phoneId.isNotBlank() && token.isNotBlank() && recipient.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        modifier = Modifier.fillMaxWidth().testTag("send_cloud_api_button")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviando a Meta Graph API...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Mensaje vía Cloud API", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Result Status Feedback
                    AnimatedVisibility(visible = sendStatus != null) {
                        sendStatus?.let { (isSuccess, text) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = text,
                                        fontSize = 12.sp,
                                        color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Webhook Code Example Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141E24) else Color(0xFFF7F8FA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ejemplo de Webhook (Node.js / Express)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
app.post('/webhook', (req, res) => {
  const body = req.body;
  if (body.object === 'whatsapp_business_account') {
    const message = body.entry?.[0]?.changes?.[0]?.value?.messages?.[0];
    if (message?.text?.body) {
      const incomingText = message.text.body;
      const senderPhone = message.from;
      // Evaluar tus reglas o llamar a Gemini AI
      enviarRespuestaWhatsApp(senderPhone, "¡Hola! Respuesta automática.");
    }
    return res.sendStatus(200);
  }
  res.sendStatus(404);
});
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = if (isDark) Color(0xFF80CBC4) else Color(0xFF004D40)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepRow(step: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(WhatsAppGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}
