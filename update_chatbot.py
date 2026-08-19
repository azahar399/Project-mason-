import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'r') as f:
    text = f.read()

imports = """
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import android.content.Context
"""

text = text.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\n" + imports)

# Setup context and shared prefs inside ChatbotScreen
new_top_bar = """
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
"""

text = text.replace("""    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mason Assistant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }""", new_top_bar)

old_send = """                            coroutineScope.launch {
                                val response = viewModel.processChatCommand(userText)
                                messages = messages + ChatMessage(newId + 1, response, false)
                            }"""
new_send = """                            coroutineScope.launch {
                                val response = if (usePremiumMode) {
                                    viewModel.processPremiumChatCommand(userText, apiKey)
                                } else {
                                    viewModel.processChatCommand(userText)
                                }
                                messages = messages + ChatMessage(newId + 1, response, false)
                            }"""
text = text.replace(old_send, new_send)

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'w') as f:
    f.write(text)
