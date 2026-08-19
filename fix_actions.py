with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re

actions_block = """                actions = {
                    IconButton(onClick = { showAddFloor = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Floor", tint = MaterialTheme.colorScheme.primary)
                    }
                    var overflowMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { overflowMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(expanded = overflowMenuExpanded, onDismissRequest = { overflowMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Add Flat") }, onClick = { overflowMenuExpanded = false; showAddFlat = true })
                            DropdownMenuItem(text = { Text("Tiles Calculator") }, onClick = { overflowMenuExpanded = false; showGlobalCalculator = true })
                            DropdownMenuItem(text = { Text("Personnel") }, onClick = { overflowMenuExpanded = false; onNavigateToPersons() })
                            DropdownMenuItem(text = { Text("Calendar") }, onClick = { overflowMenuExpanded = false; onNavigateToCalendar() })
                            DropdownMenuItem(text = { Text("Report") }, onClick = { overflowMenuExpanded = false; onNavigateToReport() })
                            DropdownMenuItem(text = { Text("Wage Ledger") }, onClick = { overflowMenuExpanded = false; onNavigateToWageLedger() })
                            DropdownMenuItem(text = { Text("Daily Sheet") }, onClick = { overflowMenuExpanded = false; onNavigateToDailyWageSheet() })
                            DropdownMenuItem(text = { Text("Backup & Restore Data") }, onClick = { overflowMenuExpanded = false; showBackupDialog = true })
                        }
                    }
                },"""

text = re.sub(
r'''                actions = \{.*?                \},'''
, actions_block, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
