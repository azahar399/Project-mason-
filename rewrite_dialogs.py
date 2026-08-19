import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

# We will find the start of editingWorkColumn and replace up to "val allDisplayedFlats =" (which is right after editingFlat).
# Actually, the structure was:
# if (showAddWorkColumn) { ... }
# if (editingWorkColumn != null) { ... }
# if (editingFlat != null) { ... }
# val allDisplayedFlats = ...

# Let's find "if (editingWorkColumn != null) {"
start_idx = text.find("if (editingWorkColumn != null) {")
end_idx = text.find("val allDisplayedFlats =")

if start_idx != -1 and end_idx != -1:
    replacement = """            if (editingWorkColumn != null) {
                val colKey = "col_style_c${editingWorkColumn!!.id}"
                val colStyle = getStyleProperties(colKey, stylesMap)
                var colBgColor by remember(colKey) { mutableStateOf<String>(colStyle.bgColor ?: "") }
                var colTextColor by remember(colKey) { mutableStateOf<String>(colStyle.textColor ?: "") }
                var colIsBold by remember(colKey) { mutableStateOf<Boolean>(colStyle.isBold) }
                var colIsItalic by remember(colKey) { mutableStateOf<Boolean>(colStyle.isItalic) }
                var newColName by remember { mutableStateOf(editingWorkColumn!!.name) }
                var displayOrderStr by remember { mutableStateOf(editingWorkColumn!!.displayOrder.toString()) }
                var isSequential by remember { mutableStateOf(editingWorkColumn!!.isSequential) }
                var requiresColumnId by remember { mutableStateOf(editingWorkColumn!!.requiresColumnId) }
                var isDropdownExpanded by remember { mutableStateOf(false) }
                
                AlertDialog(
                    onDismissRequest = { editingWorkColumn = null },
                    title = { Text("Edit or Delete Column") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newColName,
                                onValueChange = { newColName = it },
                                label = { Text("Column Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = displayOrderStr,
                                onValueChange = { displayOrderStr = it },
                                label = { Text("Display Order") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isSequential = !isSequential }
                            ) {
                                Checkbox(checked = isSequential, onCheckedChange = { isSequential = it })
                                Spacer(Modifier.width(4.dp))
                                Text("Strict Sequential (Blocks others)", fontSize = 14.sp)
                            }
                            if (!isSequential) {
                                ExposedDropdownMenuBox(
                                    expanded = isDropdownExpanded,
                                    onExpandedChange = { isDropdownExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = workColumns.find { it.id == requiresColumnId }?.name ?: "None (Independent)",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Requires Completion Of") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("None (Independent)") },
                                            onClick = { requiresColumnId = null; isDropdownExpanded = false }
                                        )
                                        workColumns.forEach { col ->
                                            if (col.id != editingWorkColumn!!.id) {
                                                DropdownMenuItem(
                                                    text = { Text(col.name) },
                                                    onClick = { requiresColumnId = col.id; isDropdownExpanded = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                            ExcelStyleSelector(
                                currentBgColor = colBgColor,
                                currentTextColor = colTextColor,
                                currentIsBold = colIsBold,
                                currentIsItalic = colIsItalic,
                                onStyleChange = { bg, tc, bold, italic ->
                                    colBgColor = bg
                                    colTextColor = tc
                                    colIsBold = bold
                                    colIsItalic = italic
                                    saveStyle(colKey, bg, tc, bold, italic)
                                }
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newColName.isNotBlank()) {
                                viewModel.updateWorkColumn(
                                    editingWorkColumn!!.copy(
                                        name = newColName,
                                        displayOrder = displayOrderStr.toIntOrNull() ?: editingWorkColumn!!.displayOrder,
                                        isSequential = isSequential,
                                        requiresColumnId = requiresColumnId
                                    )
                                )
                                editingWorkColumn = null
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                onNavigateToWorkColumn(editingWorkColumn!!.id)
                                editingWorkColumn = null
                            }) { Text("View History") }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                viewModel.deleteWorkColumn(editingWorkColumn!!)
                                editingWorkColumn = null
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }
                        }
                    }
                )
            }

            if (editingFlat != null) {
                val flatKey = "flat_style_f${editingFlat!!.id}"
                val flatStyle = getStyleProperties(flatKey, stylesMap)
                var flatBgColor by remember(flatKey) { mutableStateOf<String>(flatStyle.bgColor ?: "") }
                var flatTextColor by remember(flatKey) { mutableStateOf<String>(flatStyle.textColor ?: "") }
                var flatIsBold by remember(flatKey) { mutableStateOf<Boolean>(flatStyle.isBold) }
                var flatIsItalic by remember(flatKey) { mutableStateOf<Boolean>(flatStyle.isItalic) }

                var newFlatName by remember { mutableStateOf(editingFlat!!.name) }
                var newFlatSqFt by remember { mutableStateOf(editingFlat!!.sqFt.toString()) }

                if (showFlatCalculator) {
                    TilesCalculatorDialog(
                        onDismissRequest = { showFlatCalculator = false },
                        onApplyArea = { calculatedArea ->
                            newFlatSqFt = calculatedArea.toInt().toString()
                        }
                    )
                }

                AlertDialog(
                    onDismissRequest = { editingFlat = null },
                    title = { Text("Edit or Delete Flat") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = newFlatName,
                                onValueChange = { newFlatName = it },
                                label = { Text("Flat Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newFlatSqFt,
                                onValueChange = { newFlatSqFt = it },
                                label = { Text("Area Size (Sq Ft)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                            ExcelStyleSelector(
                                currentBgColor = flatBgColor,
                                currentTextColor = flatTextColor,
                                currentIsBold = flatIsBold,
                                currentIsItalic = flatIsItalic,
                                onStyleChange = { bg, tc, bold, italic ->
                                    flatBgColor = bg
                                    flatTextColor = tc
                                    flatIsBold = bold
                                    flatIsItalic = italic
                                    saveStyle(flatKey, bg, tc, bold, italic)
                                }
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                            Button(
                                onClick = { showFlatCalculator = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calculate Material/Area")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Material/Area Calculator")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newFlatName.isNotBlank()) {
                                val sqFtVal = newFlatSqFt.toDoubleOrNull() ?: editingFlat!!.sqFt
                                viewModel.updateFlat(editingFlat!!.copy(name = newFlatName, sqFt = sqFtVal))
                                editingFlat = null
                                triggerAd()
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                onNavigateToFlat(editingFlat!!.id)
                                editingFlat = null
                            }) { Text("View History") }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                viewModel.deleteFlat(editingFlat!!)
                                editingFlat = null
                                triggerAd()
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }
                        }
                    }
                )
            }

            """
    text = text[:start_idx] + replacement + text[end_idx:]

with open('/app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)

print("Replaced dialogs.")
