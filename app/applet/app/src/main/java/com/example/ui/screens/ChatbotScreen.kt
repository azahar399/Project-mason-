package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimaryColor

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: AppViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf(
        ChatMessage("Hello! Tell me what work has been completed.\nExample: 'Flat 101 Brick work is done'", false)
    )) }

    val flats by viewModel.flats.collectAsState(initial = emptyList())
    val workColumns by viewModel.workColumns.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatHistory) { message ->
                    ChatBubble(message)
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
                    placeholder = { Text("Type here...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMsg = inputText
                            chatHistory = chatHistory + ChatMessage(userMsg, true)
                            inputText = ""

                            // Simple Regex/String Matching Parser
                            val lowerText = userMsg.lowercase()
                            
                            val matchedFlat = flats.find { lowerText.contains(it.name.lowercase()) }
                            val matchedCol = workColumns.find { lowerText.contains(it.name.lowercase()) }

                            if (matchedFlat != null && matchedCol != null) {
                                // Insert work entry
                                val entry = com.example.data.WorkEntry(
                                    flatId = matchedFlat.id,
                                    workColumnId = matchedCol.id,
                                    masonId = null,
                                    helperId = null,
                                    date = System.currentTimeMillis()
                                )
                                viewModel.insertWorkEntry(entry)
                                chatHistory = chatHistory + ChatMessage("✅ Saved! Marked ${matchedCol.name} as done for Flat ${matchedFlat.name}.", false)
                            } else {
                                var response = "❌ Couldn't understand fully."
                                if (matchedFlat == null) response += "\n- Which Flat?"
                                if (matchedCol == null) response += "\n- Which Work?"
                                chatHistory = chatHistory + ChatMessage(response, false)
                            }
                        }
                    },
                    modifier = Modifier
                        .background(PrimaryColor, RoundedCornerShape(50))
                        .padding(4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) PrimaryColor else Color(0xFF2A2A2A)
    val textColor = if (message.isUser) Color.Black else Color.White
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = textColor,
            modifier = Modifier
                .background(bgColor, shape)
                .padding(12.dp),
            fontSize = 15.sp
        )
    }
}
