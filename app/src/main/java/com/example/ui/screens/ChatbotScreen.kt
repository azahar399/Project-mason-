package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import android.content.Context

import com.example.ui.AppViewModel
import kotlinx.coroutines.launch

data class ChatMessage(val id: Int, val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf(
        ChatMessage(0, "Hello! I am your Mason Assistant. Tell me what to do. Example: 'Add mason Azahar 9876543210' or 'Azahar did 5 column work on 1st floor today'.", false)
    )) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    

    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("chatbot_prefs", Context.MODE_PRIVATE) }
    var usePremiumMode by remember { mutableStateOf(sharedPref.getBoolean("use_premium", false)) }
    var apiKey by remember { mutableStateOf(sharedPref.getString("gemini_api_key", "") ?: "") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Chatbot Settings") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = usePremiumMode,
                            onCheckedChange = { 
                                usePremiumMode = it
                                sharedPref.edit().putBoolean("use_premium", it).apply()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Premium Mode (Gemini API)")
                    }
                    if (usePremiumMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { 
                                apiKey = it
                                sharedPref.edit().putString("gemini_api_key", it).apply()
                            },
                            label = { Text("Gemini API Key") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Your API Key is stored locally and securely.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mason Assistant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed(), key = { it.id }) { msg ->
                    ChatBubble(msg)
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type command here...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val userText = inputText.text.trim()
                        if (userText.isNotEmpty()) {
                            val newId = messages.size
                            messages = messages + ChatMessage(newId, userText, true)
                            inputText = TextFieldValue("")
                            
                            coroutineScope.launch {
                                val response = if (usePremiumMode) {
                                    viewModel.processPremiumChatCommand(userText, apiKey)
                                } else {
                                    viewModel.processChatCommand(userText)
                                }
                                messages = messages + ChatMessage(newId + 1, response, false)
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor)
        }
    }
}
