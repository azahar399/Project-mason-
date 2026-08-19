import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

# Fix the showAddWorkColumn Checkbox
text = re.sub(
    r'modifier = Modifier\.clickable \{ isSequential = !isSequential \}\n\s+\) \{\n\s+Spacer\(Modifier\.width\(4\.dp\)\)',
    r'modifier = Modifier.clickable { isSequential = !isSequential }\n                            ) {\n                                Checkbox(checked = isSequential, onCheckedChange = { isSequential = it })\n                                Spacer(Modifier.width(4.dp))',
    text
)

# Fix editingWorkColumn OutlinedTextField and Checkbox
text = re.sub(
    r'modifier = Modifier\.fillMaxWidth\(\)\n\s+Row\(\n\s+verticalAlignment = Alignment\.CenterVertically,\n\s+modifier = Modifier\.clickable \{ isSequential = !isSequential \}\n\s+\) \{\n\s+Checkbox\(checked = isSequential, onCheckedChange = \{ isSequential = it \}\)\n\s+Spacer\(Modifier\.width\(4\.dp\)\)',
    r'modifier = Modifier.fillMaxWidth()\n                            )\n                            Row(\n                                verticalAlignment = Alignment.CenterVertically,\n                                modifier = Modifier.clickable { isSequential = !isSequential }\n                            ) {\n                                Checkbox(checked = isSequential, onCheckedChange = { isSequential = it })\n                                Spacer(Modifier.width(4.dp))',
    text
)

# Fix editingWorkColumn dismissButton and extra braces
text = re.sub(
    r'Text\("Save"\)\n\s+dismissButton = \{\n\s+Row \{\n\s+TextButton\(onClick = \{\n\s+onNavigateToWorkColumn\(editingWorkColumn!!\.id\)\n\s+editingWorkColumn = null\n\s+\}\) \{ Text\("View History"\) \}\n\s+Spacer\(modifier = Modifier\.width\(8\.dp\)\)\n\s+TextButton\(onClick = \{\n\s+viewModel\.deleteWorkColumn\(editingWorkColumn!!\)\n\s+editingWorkColumn = null\n\s+\}, colors = ButtonDefaults\.textButtonColors\(contentColor = Color\(0xFFB91C1C\)\)\) \{ Text\("Delete"\) \}\n\s+\}\n\s+\}\n\s+\}\n\s+\}\n\s+\)',
    r'Text("Save")\n                        }\n                    },\n                    dismissButton = {\n                        Row {\n                            TextButton(onClick = {\n                                onNavigateToWorkColumn(editingWorkColumn!!.id)\n                                editingWorkColumn = null\n                            }) { Text("View History") }\n                            Spacer(modifier = Modifier.width(8.dp))\n                            TextButton(onClick = {\n                                viewModel.deleteWorkColumn(editingWorkColumn!!)\n                                editingWorkColumn = null\n                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }\n                        }\n                    }\n                )',
    text
)

# Fix editingFlat confirmButton and dismissButton
text = re.sub(
    r'viewModel\.updateFlat\(editingFlat!!\.copy\(name = newFlatName, sqFt = sqFtVal\)\)\n\s+editingFlat = null\n\s+dismissButton = \{\n\s+Row \{\n\s+TextButton\(onClick = \{\n\s+onNavigateToFlat\(editingFlat!!\.id\)\n\s+editingFlat = null\n\s+\}\) \{ Text\("View History"\) \}\n\s+Spacer\(modifier = Modifier\.width\(8\.dp\)\)\n\s+TextButton\(onClick = \{\n\s+viewModel\.deleteFlat\(editingFlat!!\)\n\s+editingFlat = null\n\s+triggerAd\(\)\n\s+\}, colors = ButtonDefaults\.textButtonColors\(contentColor = Color\(0xFFB91C1C\)\)\) \{ Text\("Delete"\) \}\n\s+\}\n\s+\}\n\s+editingFlat = null\n\s+triggerAd\(\)\n\s+\}, colors = ButtonDefaults\.textButtonColors\(contentColor = Color\(0xFFB91C1C\)\)\) \{ Text\("Delete"\) \}\n\s+Spacer\(modifier = Modifier\.width\(8\.dp\)\)\n\s+\}\n\s+\}',
    r'viewModel.updateFlat(editingFlat!!.copy(name = newFlatName, sqFt = sqFtVal))\n                                editingFlat = null\n                                triggerAd()\n                            }\n                        }) {\n                            Text("Save")\n                        }\n                    },\n                    dismissButton = {\n                        Row {\n                            TextButton(onClick = {\n                                onNavigateToFlat(editingFlat!!.id)\n                                editingFlat = null\n                            }) { Text("View History") }\n                            Spacer(modifier = Modifier.width(8.dp))\n                            TextButton(onClick = {\n                                viewModel.deleteFlat(editingFlat!!)\n                                editingFlat = null\n                                triggerAd()\n                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }\n                        }\n                    }',
    text
)

with open('/app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)

print("Fixes applied.")
