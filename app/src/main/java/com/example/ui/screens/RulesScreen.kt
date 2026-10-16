package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BotRule
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.viewmodel.WhatsBotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: WhatsBotViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.allRules.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("Todas") }
    var ruleToEdit by remember { mutableStateOf<BotRule?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val categories = listOf("Todas", "Menú", "Ventas", "Información", "Atención", "Bienvenida")
    val filteredRules = if (selectedCategory == "Todas") {
        rules
    } else {
        rules.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reglas y Flujos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("${rules.count { it.isEnabled }} activas de ${rules.size} reglas", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.resetDefaultRules() },
                        modifier = Modifier.testTag("reset_rules_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar por defecto", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF1F2C34) else WhatsAppGreenDark
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    ruleToEdit = null
                    isDialogOpen = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva Regla") },
                containerColor = WhatsAppGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_rule_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsAppGreen.copy(alpha = 0.2f),
                            selectedLabelColor = WhatsAppGreen
                        )
                    )
                }
            }

            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay reglas en esta categoría",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            ruleToEdit = null
                            isDialogOpen = true
                        }) {
                            Text("Crear regla para $selectedCategory")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
                        RuleItemCard(
                            rule = rule,
                            onToggle = { viewModel.toggleRule(rule) },
                            onEdit = {
                                ruleToEdit = rule
                                isDialogOpen = true
                            },
                            onDelete = { viewModel.deleteRule(rule) }
                        )
                    }
                }
            }
        }

        // Add/Edit Rule Dialog
        if (isDialogOpen) {
            RuleEditDialog(
                initialRule = ruleToEdit,
                onDismiss = { isDialogOpen = false },
                onSave = { savedRule ->
                    viewModel.saveRule(savedRule)
                    isDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun RuleItemCard(
    rule: BotRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Keyword badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WhatsAppGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rule.triggerKeyword,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WhatsAppGreen
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Match Type badge
                val matchLabel = when (rule.matchType) {
                    BotRule.MATCH_EXACT -> "Exacta"
                    BotRule.MATCH_STARTS_WITH -> "Empieza con"
                    else -> "Contiene"
                }
                Text(
                    text = matchLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                // Switch
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WhatsAppGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Response Text Preview
            Text(
                text = rule.responseText,
                fontSize = 13.5.sp,
                maxLines = 3,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Categoría: ${rule.category}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (rule.hitCount > 0) {
                    Text(
                        text = " • Usada ${rule.hitCount}x",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RuleEditDialog(
    initialRule: BotRule?,
    onDismiss: () -> Unit,
    onSave: (BotRule) -> Unit
) {
    var trigger by remember { mutableStateOf(initialRule?.triggerKeyword ?: "") }
    var response by remember { mutableStateOf(initialRule?.responseText ?: "") }
    var matchType by remember { mutableStateOf(initialRule?.matchType ?: BotRule.MATCH_CONTAINS) }
    var category by remember { mutableStateOf(initialRule?.category ?: "General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRule == null) "Crear Nueva Regla" else "Editar Regla",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Palabra clave o detonador") },
                    placeholder = { Text("Ej. precios, horario, hola") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Match Type Selection
                Text("Tipo de coincidencia:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        BotRule.MATCH_CONTAINS to "Contiene",
                        BotRule.MATCH_EXACT to "Exacta",
                        BotRule.MATCH_STARTS_WITH to "Empieza con"
                    ).forEach { (type, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = matchType == type,
                                onClick = { matchType = type }
                            )
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    placeholder = { Text("Ventas, Menú, Información...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = response,
                    onValueChange = { response = it },
                    label = { Text("Mensaje de respuesta") },
                    placeholder = { Text("Hola! Ofrecemos servicio en...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    maxLines = 6
                )

                // Variables chip helper
                Text(
                    text = "💡 Puedes usar variables: {negocio}, {hora}, {fecha}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (trigger.isNotBlank() && response.isNotBlank()) {
                        val newRule = (initialRule ?: BotRule(
                            triggerKeyword = "",
                            responseText = ""
                        )).copy(
                            triggerKeyword = trigger.trim(),
                            responseText = response.trim(),
                            matchType = matchType,
                            category = category.trim().ifBlank { "General" }
                        )
                        onSave(newRule)
                    }
                },
                enabled = trigger.isNotBlank() && response.isNotBlank()
            ) {
                Text("Guardar Regla", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
