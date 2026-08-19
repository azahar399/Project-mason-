with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""var selectedFloorId by remember { mutableStateOf<Int?>(null) } // null = All Floors""",
"""var selectedFloorId by remember(floors) { mutableStateOf<Int?>(floors.firstOrNull()?.id) } // Default to first floor to prevent loading all 150 rows""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
