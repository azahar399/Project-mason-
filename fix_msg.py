with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""androidx.compose.material.icons.filled.Warning""",
"""Icons.Default.Warning""")

text = text.replace(
"""import androidx.compose.material.icons.filled.MoreVert""",
"""import androidx.compose.material.icons.filled.MoreVert\nimport androidx.compose.material.icons.filled.Warning""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
