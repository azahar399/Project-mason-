with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                            }) {
                            Text("Add")
                            }""",
"""                            }) {
                                Text("Add")
                            }"""
)

content = content.replace(
"""                    dismissButton = {
                            }) {
                            Text("Cancel")
                            }""",
"""                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { 
                            showAddFlat = false
                            showAddFloor = false
                            showAddWorkColumn = false
                            editingWorkColumn = null
                            editingFlat = null
                            selectedCellForAction = null
                            floorToDelete = null
                            showGlobalCalculator = false
                            showFlatCalculator = false
                            showBackupDialog = false
                        }) {
                            Text("Cancel")
                        }"""
)

content = content.replace(
"""                    dismissButton = {
                            }) {
                            Text("Close")
                            }""",
"""                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { 
                            showAddFlat = false
                            showAddFloor = false
                            showAddWorkColumn = false
                            editingWorkColumn = null
                            editingFlat = null
                            selectedCellForAction = null
                            floorToDelete = null
                            showGlobalCalculator = false
                            showFlatCalculator = false
                            showBackupDialog = false
                        }) {
                            Text("Close")
                        }"""
)

content = content.replace(
"""                    dismissButton = {
                            }) {
                            Text("No")
                            }""",
"""                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { 
                            floorToDelete = null
                        }) {
                            Text("No")
                        }"""
)


with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
