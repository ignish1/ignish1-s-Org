package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AutoResponderScreen
import com.example.ui.screens.CloudApiScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.WhatsBotViewModel

enum class BotTab(val label: String, val icon: ImageVector, val tag: String) {
    SIMULATOR("Simulador", Icons.Default.Forum, "tab_simulator"),
    RULES("Reglas", Icons.Default.Rule, "tab_rules"),
    AUTO_REPLY("Auto-Reply", Icons.Default.NotificationsActive, "tab_auto_reply"),
    CLOUD_API("Cloud API", Icons.Default.CloudQueue, "tab_cloud_api"),
    LOGS("Historial", Icons.Default.Assessment, "tab_logs")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val botViewModel: WhatsBotViewModel = viewModel()
                WhatsBotApp(viewModel = botViewModel)
            }
        }
    }
}

@Composable
fun WhatsBotApp(viewModel: WhatsBotViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(BotTab.SIMULATOR) }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF1F2C34) else Color(0xFFF0F2F5),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                BotTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDark) Color.White else Color(0xFF004D40),
                            selectedTextColor = WhatsAppGreen,
                            indicatorColor = WhatsAppGreen.copy(alpha = if (isDark) 0.35f else 0.2f),
                            unselectedIconColor = if (isDark) Color(0xFF8696A0) else Color(0xFF54656F),
                            unselectedTextColor = if (isDark) Color(0xFF8696A0) else Color(0xFF54656F)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())

        when (currentTab) {
            BotTab.SIMULATOR -> SimulatorScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            BotTab.RULES -> RulesScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            BotTab.AUTO_REPLY -> AutoResponderScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            BotTab.CLOUD_API -> CloudApiScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            BotTab.LOGS -> LogsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
        }
    }
}
