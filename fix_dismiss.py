with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""                    dismissButton = {
                        TextButton(onClick = { showAddFlat = false }) {
                            Text("Cancel")
                        }
                    dismissButton = {
                        TextButton(onClick = { showAddFlat = false }) {
                            Text("Cancel")
                        }
                    }""",
"""                    dismissButton = {
                        TextButton(onClick = { showAddFlat = false }) {
                            Text("Cancel")
                        }
                    }""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
