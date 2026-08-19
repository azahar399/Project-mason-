cat << 'INNER_EOF' > topbar_replacement.txt
                actions = {
                    IconButton(onClick = { showGlobalCalculator = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Tiles Calculator", tint = com.example.ui.theme.SecondaryColor)
                    }
                    IconButton(onClick = { showAddFloor = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Floor", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showAddFlat = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Flat", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onNavigateToPersons) {
                        Icon(Icons.Default.Person, contentDescription = "Personnel", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Report", tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    var overflowMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { overflowMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = overflowMenuExpanded,
                            onDismissRequest = { overflowMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("বেতনের হিসাব (মাস্টার) / Wage Ledger") },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onNavigateToWageLedger()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("দৈনিক খতিয়ান / Daily Sheet") },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onNavigateToDailyWageSheet()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Backup & Restore Data") },
                                onClick = {
                                    overflowMenuExpanded = false
                                    showBackupDialog = true
                                }
                            )
                        }
                    }
                },
INNER_EOF

# Find the start and end of actions in DashboardScreen.kt and replace it
awk '
/actions = \{/ {
    in_actions = 1
    system("cat topbar_replacement.txt")
    next
}
in_actions {
    if (/,$/ && /}/ && /actions/) { # End of actions block is tricky, we can search for colors =
        # Actually better to just use sed
    }
}
'
