with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re

# Fix confirmButton at 416
text = text.replace(
"""                    confirmButton = {
                        Button(onClick = {
                            if (newColName.isNotBlank()) {
                                viewModel.insertWorkColumn(com.example.data.WorkColumn(name = newColName, displayOrder = (workColumns.maxOfOrNull { it.displayOrder } ?: 0) + 1, isSequential = isSequential, requiresColumnId = requiresColumnId))
                                showAddWorkColumn = false
                                triggerAd()
                            }
                    },
                            }
                        }) {""",
"""                    confirmButton = {
                        Button(onClick = {
                            if (newColName.isNotBlank()) {
                                viewModel.insertWorkColumn(com.example.data.WorkColumn(name = newColName, displayOrder = (workColumns.maxOfOrNull { it.displayOrder } ?: 0) + 1, isSequential = isSequential, requiresColumnId = requiresColumnId))
                                showAddWorkColumn = false
                                triggerAd()
                            }
                        }) {""")

# Missing } at end of AlertDialog? Let's see 423
text = text.replace(
"""                    dismissButton = {
                        TextButton(onClick = { showAddWorkColumn = false }) {
                            Text("Cancel")
                        }
                    }
            if (editingWorkColumn != null) {""",
"""                    dismissButton = {
                        TextButton(onClick = { showAddWorkColumn = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            if (editingWorkColumn != null) {""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
