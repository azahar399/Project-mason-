with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""            CenterAlignedTopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { floorDropdownExpanded = true }, horizontalAlignment = Alignment.CenterHorizontally) {""",
"""            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.clickable { floorDropdownExpanded = true }, horizontalAlignment = Alignment.CenterHorizontally) {""")

text = text.replace(
"""                        if (selectedFloorId != null) {
                            val currentFloor = floors.find { it.id == selectedFloorId }
                            if (currentFloor != null) {
                                IconButton(onClick = { floorToDelete = currentFloor }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Floor", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                },""",
"""                        }
                        if (selectedFloorId != null) {
                            val currentFloor = floors.find { it.id == selectedFloorId }
                            if (currentFloor != null) {
                                IconButton(onClick = { floorToDelete = currentFloor }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Floor", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                },""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
