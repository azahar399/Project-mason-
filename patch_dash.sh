sed -i 's/onNavigateToDailyWageSheet: () -> Unit/onNavigateToDailyWageSheet: () -> Unit,\n    onNavigateToChatbot: () -> Unit/g' app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i 's/Icon(Icons.Default.MoreVert, contentDescription = "Menu")/Icon(Icons.Default.MoreVert, contentDescription = "Menu")\n                        }\n                        IconButton(onClick = onNavigateToChatbot) {\n                            Icon(androidx.compose.material.icons.automirrored.filled.Chat, contentDescription = "AI Assistant")/g' app/src/main/java/com/example/ui/screens/DashboardScreen.kt

