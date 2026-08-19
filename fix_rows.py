with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""            val allDisplayedFlats: List<com.example.data.Flat> = if (selectedFloorId == null) flats else flats.filter { it.floorId == selectedFloorId }""",
"""            val allDisplayedFlats: List<com.example.data.Flat> = if (selectedFloorId == null) emptyList() else flats.filter { it.floorId == selectedFloorId }""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
