with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""        }
    }
}
    }
}

data class ExcelStyleProperties""",
"""        }
    }
}

data class ExcelStyleProperties""")

text = text.replace(
"""                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },""",
"""                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
