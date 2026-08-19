with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re

text = re.sub(
r'''            CenterAlignedTopAppBar\(
                title = \{ 
                    Row\(verticalAlignment = Alignment\.CenterVertically\) \{
                        Column\(modifier = Modifier\.clickable \{ floorDropdownExpanded = true \}, horizontalAlignment = Alignment\.CenterHorizontally\) \{
                            Text\("Project Mason ▾", fontWeight = FontWeight\.SemiBold, fontSize = 20\.sp\)
                            val floorName = if \(selectedFloorId == null\) "All Floors \(Master Sheet\)" else floors\.find \{ it\.id == selectedFloorId \}\?\.name \?: ""
                            Text\(floorName, fontSize = 12\.sp, color = Slate500\)
                            
                            DropdownMenu\(
                                expanded = floorDropdownExpanded,
                                onDismissRequest = \{ floorDropdownExpanded = false \}
                            \) \{
                                DropdownMenuItem\(
                                    text = \{ Text\("All Floors \(Master Sheet\)"\) \},
                                    onClick = \{ selectedFloorId = null; floorDropdownExpanded = false \}
                                \)
                                floors\.forEach \{ floor ->
                                    DropdownMenuItem\(
                                        text = \{ Text\(floor\.name\) \},
                                        onClick = \{ selectedFloorId = floor\.id; floorDropdownExpanded = false \}
                                    \)
                                \}
                            \}
                        \}
                        \}
                        if \(selectedFloorId != null\) \{
                            val currentFloor = floors\.find \{ it\.id == selectedFloorId \}
                            if \(currentFloor != null\) \{
                                IconButton\(onClick = \{ floorToDelete = currentFloor \}\) \{
                                    Icon\(Icons\.Default\.Delete, contentDescription = "Delete Floor", tint = MaterialTheme\.colorScheme\.error\)
                                \}
                            \}
                        \}
                    \}
                \},''',
'''            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.clickable { floorDropdownExpanded = true }, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Project Mason ▾", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                            val floorName = if (selectedFloorId == null) "All Floors (Master Sheet)" else floors.find { it.id == selectedFloorId }?.name ?: ""
                            Text(floorName, fontSize = 12.sp, color = Slate500)
                            
                            DropdownMenu(
                                expanded = floorDropdownExpanded,
                                onDismissRequest = { floorDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Floors (Master Sheet)") },
                                    onClick = { selectedFloorId = null; floorDropdownExpanded = false }
                                )
                                floors.forEach { floor ->
                                    DropdownMenuItem(
                                        text = { Text(floor.name) },
                                        onClick = { selectedFloorId = floor.id; floorDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                        if (selectedFloorId != null) {
                            val currentFloor = floors.find { it.id == selectedFloorId }
                            if (currentFloor != null) {
                                IconButton(onClick = { floorToDelete = currentFloor }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Floor", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                },''', text)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
