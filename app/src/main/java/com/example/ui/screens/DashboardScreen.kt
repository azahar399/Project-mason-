package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flat
import com.example.data.WorkEntryWithDetails
import com.example.ui.AppViewModel
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.TilesCalculatorDialog
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToFlat: (Int) -> Unit,
    onNavigateToPersons: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAddWork: () -> Unit,
    onNavigateToAddOtherWork: () -> Unit,
    onNavigateToWorkColumn: (Int) -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToWageLedger: () -> Unit,
    onNavigateToDailyWageSheet: () -> Unit,
    onNavigateToChatbot: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val triggerAd = {
        val activity = context as? android.app.Activity
        if (activity != null) {
            val adManager = (context.applicationContext as com.example.MasonApplication).adManager
            adManager?.incrementActionAndShowInterstitial(activity)
        }
    }

    val floors by viewModel.allFloors.collectAsState()
    val flats by viewModel.allFlats.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()
    val allEntries by viewModel.allWorkEntries.collectAsState()
    val masons by viewModel.allMasons.collectAsState()
    
    val sharedPref = remember(context) { context.getSharedPreferences("excel_styles", android.content.Context.MODE_PRIVATE) }
    val stylesMap = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(sharedPref) {
        val allPrefs = sharedPref.all
        allPrefs.forEach { (key, value) ->
            if (value is String) {
                stylesMap[key] = value
            }
        }
    }

    val saveStyle = { key: String, bgColor: String, textColor: String, isBold: Boolean, isItalic: Boolean ->
        val value = "$bgColor|$textColor|$isBold|$isItalic"
        stylesMap[key] = value
        sharedPref.edit().putString(key, value).apply()
    }
    
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddFlat by remember { mutableStateOf(false) }
    var showAddFloor by remember { mutableStateOf(false) }
    var showAddWorkColumn by remember { mutableStateOf(false) }
    var selectedFloorId by remember(floors) { mutableStateOf<Int?>(floors.firstOrNull()?.id) } // Default to first floor to prevent loading all 150 rows
    var editingWorkColumn by remember { mutableStateOf<com.example.data.WorkColumn?>(null) }
    var editingFlat by remember { mutableStateOf<com.example.data.Flat?>(null) }
    var floorToDelete by remember { mutableStateOf<com.example.data.Floor?>(null) }

    var showGlobalCalculator by remember { mutableStateOf(false) }
    var showFlatCalculator by remember { mutableStateOf(false) }
    var selectedCellForAction by remember { mutableStateOf<Pair<Flat, com.example.data.WorkColumn>?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            var floorDropdownExpanded by remember { mutableStateOf(false) }
            CenterAlignedTopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { floorDropdownExpanded = true }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Work Tracker ▾", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    selectedFloorId = floor.id
                                                    floorDropdownExpanded = false
                                                },
                                                onLongClick = {
                                                    floorToDelete = floor
                                                    floorDropdownExpanded = false
                                                }
                                            )
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = floor.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                    }
                },
                actions = {
                    if (selectedFloorId != null) {
                        val currentFloor = floors.find { it.id == selectedFloorId }
                        if (currentFloor != null) {
                            IconButton(onClick = { floorToDelete = currentFloor }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Floor", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    IconButton(onClick = { showAddFlat = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Flat", tint = MaterialTheme.colorScheme.primary)
                    }
                    var overflowMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = onNavigateToChatbot) {
                            Icon(Icons.Default.Person, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Box {
                        IconButton(onClick = { overflowMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(expanded = overflowMenuExpanded, onDismissRequest = { overflowMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Add Floor") }, onClick = { overflowMenuExpanded = false; showAddFloor = true })
                            DropdownMenuItem(text = { Text("Material Calculator") }, onClick = { overflowMenuExpanded = false; showGlobalCalculator = true })
                            DropdownMenuItem(text = { Text("Personnel") }, onClick = { overflowMenuExpanded = false; onNavigateToPersons() })
                            DropdownMenuItem(text = { Text("Calendar") }, onClick = { overflowMenuExpanded = false; onNavigateToCalendar() })
                            DropdownMenuItem(text = { Text("Report") }, onClick = { overflowMenuExpanded = false; onNavigateToReport() })
                            DropdownMenuItem(text = { Text("Wage Ledger") }, onClick = { overflowMenuExpanded = false; onNavigateToWageLedger() })
                            DropdownMenuItem(text = { Text("Daily Sheet") }, onClick = { overflowMenuExpanded = false; onNavigateToDailyWageSheet() })
                            DropdownMenuItem(text = { Text("Backup & Restore Data") }, onClick = { overflowMenuExpanded = false; showBackupDialog = true })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Slate200,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (fabExpanded) {
                    ExtendedFloatingActionButton(
                        onClick = { fabExpanded = false; onNavigateToAddOtherWork() },
                        icon = { Icon(Icons.Default.Add, "Log Other Work") },
                        text = { Text("Other Work") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    ExtendedFloatingActionButton(
                        onClick = { fabExpanded = false; onNavigateToAddWork() },
                        icon = { Icon(Icons.Default.Add, "Log Flat Work") },
                        text = { Text("Flat Work") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(if (fabExpanded) Icons.Default.Close else Icons.Default.Edit, "Log Work")
                }
            }

            if (showAddFloor) {
                var newFloorName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAddFloor = false },
                    title = { Text("Add New Floor") },
                    text = {
                        OutlinedTextField(
                            value = newFloorName,
                            onValueChange = { newFloorName = it },
                            label = { Text("Floor Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newFloorName.isNotBlank()) {
                                viewModel.insertFloor(newFloorName)
                                showAddFloor = false
                                triggerAd()
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFloor = false }) { Text("Cancel") }
                    }
                )
            }

            if (floorToDelete != null) {
                AlertDialog(
                    onDismissRequest = { floorToDelete = null },
                    title = { Text("Delete Floor") },
                    text = { Text("Are you sure you want to delete ${floorToDelete?.name}? This will delete all flats and their work records on this floor.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                floorToDelete?.let {
                                    viewModel.deleteFloor(it)
                                    if (selectedFloorId == it.id) {
                                        selectedFloorId = null
                                    }
                                }
                                floorToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { floorToDelete = null }) { Text("Cancel") }
                    }
                )
            }

            if (showAddFlat) {
                var newFlatName by remember { mutableStateOf("") }
                var newFlatSqFt by remember { mutableStateOf("1200") }
                var flatFloorId by remember { mutableStateOf<Int?>(selectedFloorId ?: floors.firstOrNull()?.id) }
                var floorDropdownExpanded by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showAddFlat = false },
                    title = { Text("Add New Flat") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = floorDropdownExpanded,
                                onExpandedChange = { floorDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = floors.find { it.id == flatFloorId }?.name ?: "Select Floor",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Floor") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = floorDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = floorDropdownExpanded, onDismissRequest = { floorDropdownExpanded = false }) {
                                    floors.forEach { floor ->
                                        DropdownMenuItem(
                                            text = { Text(floor.name) },
                                            onClick = { flatFloorId = floor.id; floorDropdownExpanded = false }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = newFlatName,
                                onValueChange = { newFlatName = it },
                                label = { Text("Flat Name/Number") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newFlatSqFt,
                                onValueChange = { newFlatSqFt = it },
                                label = { Text("Area Size (Sq Ft)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newFlatName.isNotBlank() && flatFloorId != null) {
                                val sqFtVal = newFlatSqFt.toDoubleOrNull() ?: 1200.0
                                viewModel.insertFlat(newFlatName, flatFloorId!!, sqFtVal)
                                showAddFlat = false
                                triggerAd()
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFlat = false }) { Text("Cancel") }
                    }
                )
            }

            if (showAddWorkColumn) {
                var newColName by remember { mutableStateOf("") }
                var isSequential by remember { mutableStateOf(false) }
                var requiresColumnId by remember { mutableStateOf<Int?>(null) }
                var isDropdownExpanded by remember { mutableStateOf(false) }
                
                AlertDialog(
                    onDismissRequest = { showAddWorkColumn = false },
                    title = { Text("Add Column") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newColName,
                                onValueChange = { newColName = it },
                                label = { Text("Column Name") },
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
                                            DropdownMenuItem(
                                                text = { Text(col.name) },
                                                onClick = { requiresColumnId = col.id; isDropdownExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newColName.isNotBlank()) {
                                viewModel.insertWorkColumn(com.example.data.WorkColumn(name = newColName, displayOrder = (workColumns.maxOfOrNull { it.displayOrder } ?: 0) + 1, isSequential = isSequential, requiresColumnId = requiresColumnId))
                                showAddWorkColumn = false
                                triggerAd()
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddWorkColumn = false }) { Text("Cancel") }
                    }
                )
            }

            if (editingWorkColumn != null) {
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

            if (showGlobalCalculator) {
                TilesCalculatorDialog(
                    onDismissRequest = { showGlobalCalculator = false }
                )
            }

            if (showBackupDialog) {
                CloudSyncDialog(
                    onDismissRequest = { showBackupDialog = false }
                )
            }

            if (selectedCellForAction != null) {
                val cellPair = selectedCellForAction!!
                val flat = cellPair.first
                val col = cellPair.second
                val entry = allEntries.find { it.workEntry.flatId == flat.id && it.workEntry.workColumnId == col.id }
                
                val cellKey = "cell_style_f${flat.id}_c${col.id}"
                val cellStyle = getStyleProperties(cellKey, stylesMap)
                var cellBgColor by remember(cellKey) { mutableStateOf<String>(cellStyle.bgColor ?: "") }
                var cellTextColor by remember(cellKey) { mutableStateOf<String>(cellStyle.textColor ?: "") }
                var cellIsBold by remember(cellKey) { mutableStateOf<Boolean>(cellStyle.isBold) }
                var cellIsItalic by remember(cellKey) { mutableStateOf<Boolean>(cellStyle.isItalic) }

                var showCellCalculator by remember { mutableStateOf(false) }
                
                if (showCellCalculator) {
                    TilesCalculatorDialog(
                        onDismissRequest = { showCellCalculator = false },
                        onApplyDescription = { calculatedText ->
                            if (entry != null) {
                                viewModel.updateWorkEntry(entry.workEntry.copy(description = calculatedText))
                            } else {
                                // Copy to clipboard for easy pasting when they log work
                            }
                        }
                    )
                }
                
                AlertDialog(
                    onDismissRequest = { selectedCellForAction = null },
                    title = { 
                        Text(
                            text = "${flat.name} - ${col.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        ) 
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (entry != null) {
                                val isProb = entry.workEntry.isProblem
                                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(entry.workEntry.date))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isProb) Red50 else Green50, RoundedCornerShape(12.dp))
                                        .border(1.dp, if (isProb) Color(0xFFFEE2E2) else Color(0xFFD1FAE5), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "STATUS: ${if (isProb) "PROBLEM / PENDING" else "COMPLETED"}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isProb) Color(0xFFB91C1C) else Color(0xFF047857),
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Worker: ${entry.mason?.name ?: "None"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Slate800
                                        )
                                        if (entry.helper != null) {
                                            Text(
                                                text = "Helper: ${entry.helper.name}",
                                                fontSize = 13.sp,
                                                color = Slate500
                                            )
                                        }
                                        Text(
                                            text = "Date: $dateStr",
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                                
                                if (!entry.workEntry.description.isNullOrBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate50, RoundedCornerShape(12.dp))
                                            .border(1.dp, Slate100, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text("Description / Notes:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate500)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(entry.workEntry.description, fontSize = 13.sp, color = Slate900)
                                    }
                                } else {
                                    Text("No description added yet.", fontSize = 13.sp, color = Slate400)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Slate50, RoundedCornerShape(12.dp))
                                        .border(1.dp, Slate100, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "No entry has been logged for this work yet.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate500,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))

                            ExcelStyleSelector(
                                currentBgColor = cellBgColor,
                                currentTextColor = cellTextColor,
                                currentIsBold = cellIsBold,
                                currentIsItalic = cellIsItalic,
                                onStyleChange = { bg, tc, bold, italic ->
                                    cellBgColor = bg
                                    cellTextColor = tc
                                    cellIsBold = bold
                                    cellIsItalic = italic
                                    saveStyle(cellKey, bg, tc, bold, italic)
                                }
                            )

                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                            
                            // Cell level Calculator Button
                            Button(
                                onClick = { showCellCalculator = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calculate Material")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Material Calculator")
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (entry != null) {
                                Button(
                                    onClick = {
                                        viewModel.deleteWorkEntry(entry.workEntry)
                                        selectedCellForAction = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {

                                    Text("Delete Entry")

                                    }
                            } else {
                                Button(
                                    onClick = {
                                        selectedCellForAction = null
                                        onNavigateToAddWork()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Log Entry")
                                }
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedCellForAction = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = Slate500)
                        }
                    }
                )
            }
        },
        bottomBar = {
            com.example.ui.components.AdBanner()
        }
    ) { padding ->
        val horizontalScrollState = rememberScrollState()
        
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(CleanBackground)) {
            com.example.ui.components.AdBanner(modifier = Modifier.padding(bottom = 4.dp))
            
            // Search Option Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search flat, worker, area, status...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Slate200
                ),
                singleLine = true
            )
            
            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = "Total Flats", value = flats.size.toString(), modifier = Modifier.weight(1f))
                SummaryCard(title = "Active Workers", value = masons.size.toString(), modifier = Modifier.weight(1f))
                SummaryCard(title = "Entries", value = allEntries.size.toString(), modifier = Modifier.weight(1f))
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Progress", 
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { showAddFlat = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Row", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Row", fontSize = 11.sp)
                }
                FilledTonalButton(
                    onClick = { showAddWorkColumn = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Column", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Col", fontSize = 11.sp)
                }
            }

            val allDisplayedFlats: List<com.example.data.Flat> = if (selectedFloorId == null) flats else flats.filter { it.floorId == selectedFloorId }
            val displayedFlats = remember(allDisplayedFlats, searchQuery, allEntries) {
                if (searchQuery.isBlank()) {
                    allDisplayedFlats
                } else {
                    val q = searchQuery.trim().lowercase(java.util.Locale.getDefault())
                    allDisplayedFlats.filter { flat ->
                        val flatMatches = flat.name.lowercase(java.util.Locale.getDefault()).contains(q) ||
                                flat.sqFt.toString().contains(q)
                        
                        val entriesForFlat: List<com.example.data.WorkEntryWithDetails> = allEntries.filter { it.workEntry.flatId == flat.id }
                        val entryMatches = entriesForFlat.any { entry: com.example.data.WorkEntryWithDetails ->
                            entry.mason?.name?.lowercase(java.util.Locale.getDefault())?.contains(q) == true ||
                            entry.helper?.name?.lowercase(java.util.Locale.getDefault())?.contains(q) == true ||
                            entry.workEntry.description?.lowercase(java.util.Locale.getDefault())?.contains(q) == true ||
                            entry.workColumn.name.lowercase(java.util.Locale.getDefault()).contains(q)
                        }
                        
                        flatMatches || entryMatches
                    }
                }
            }

            // Table Container with Formula Bar
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Column {
                        // Table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E3A8A)) // Blue Header
                                    .drawBehind {
                                        drawLine(
                                            color = Color(0xFF1E40AF), // Darker blue border
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                            ) {
                                // Corner row-index header box (width 40.dp)
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(56.dp)
                                        .background(Color(0xFF0F172A)) // Very dark slate
                                        .drawBehind {
                                            drawLine(
                                                color = Color(0xFF334155),
                                                start = Offset(size.width, 0f),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color(0xFF334155),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("#", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                }

                                // Frozen first column (A)
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(56.dp)
                                        .background(Color(0xFF1E3A8A))
                                        .drawBehind {
                                            drawLine(
                                                color = Color(0xFF1E40AF),
                                                start = Offset(size.width, 0f),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("A", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFFFCD34D)) // Excel letter
                                        Text("", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                                    }
                                }
                                
                                // Scrollable columns (B, C, D...)
                                Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                    workColumns.sortedBy { it.displayOrder }.forEachIndexed { index, col ->
                                        val colLetter = com.example.utils.ExcelFormulaParser.getColumnLetter(index + 1)
                                        
                                        val colKey = "col_style_c${col.id}"
                                        val colStyle = getStyleProperties(colKey, stylesMap)
                                        val colBgColor = colStyle.bgColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Transparent
                                        val colTextColor = colStyle.textColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White
                                        val colFontWeight = if (colStyle.isBold) FontWeight.ExtraBold else FontWeight.Bold
                                        val colFontStyle = if (colStyle.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                        Box(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(56.dp)
                                                .background(colBgColor)
                                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                                .clickable { editingWorkColumn = col }
                                                .drawBehind {
                                                    drawLine(
                                                        color = Color(0xFF1E40AF),
                                                        start = Offset(0f, 0f),
                                                        end = Offset(0f, size.height),
                                                        strokeWidth = 1.dp.toPx()
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = colLetter, 
                                                    fontWeight = FontWeight.Black, 
                                                    fontSize = 14.sp, 
                                                    color = if (colTextColor == Color.White) Color(0xFFFCD34D) else colTextColor
                                                ) // Excel letter
                                                Text(
                                                    text = col.name.uppercase(), 
                                                    fontWeight = colFontWeight, 
                                                    fontStyle = colFontStyle,
                                                    fontSize = 10.sp, 
                                                    color = colTextColor, 
                                                    maxLines = 1, 
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(56.dp)
                                            .clickable { showAddWorkColumn = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, "Add Column", tint = Color.White)
                                    }
                                }
                            }
                            
                            // Table body

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(displayedFlats) { rowIndex, flat ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .drawBehind {
                                                    drawLine(
                                                        color = Color(0xFF333333), // Dark gray border
                                                        start = Offset(0f, size.height),
                                                        end = Offset(size.width, size.height),
                                                        strokeWidth = 1.dp.toPx()
                                                    )
                                                }
                                                .background(Color.Black) // Black background
                                                .clickable { onNavigateToFlat(flat.id) }
                                        ) {
                                            // Row number on the far-left
                                            Box(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(48.dp)
                                                    .background(Color(0xFF0F172A)) // Dark slate
                                                    .drawBehind {
                                                        drawLine(
                                                            color = Color(0xFF334155),
                                                            start = Offset(size.width, 0f),
                                                            end = Offset(size.width, size.height),
                                                            strokeWidth = 1.dp.toPx()
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text((rowIndex + 1).toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF94A3B8))
                                            }

                                            // Frozen first column
                                            val flatKey = "flat_style_f${flat.id}"
                                            val flatStyle = getStyleProperties(flatKey, stylesMap)
                                            val flatBgColor = parseHexColor(flatStyle.bgColor) ?: Color(0xFF111111)
                                            val flatTextColor = parseHexColor(flatStyle.textColor) ?: Color.White
                                            val flatFontWeight = if (flatStyle.isBold) FontWeight.ExtraBold else FontWeight.Bold
                                            val flatFontStyle = if (flatStyle.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                            Box(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(48.dp)
                                                    .background(flatBgColor)
                                                    .clickable { editingFlat = flat }
                                                    .drawBehind {
                                                        drawLine(
                                                            color = Color(0xFF333333),
                                                            start = Offset(size.width, 0f),
                                                            end = Offset(size.width, size.height),
                                                            strokeWidth = 2.dp.toPx()
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(flat.name, fontWeight = flatFontWeight, fontStyle = flatFontStyle, fontSize = 12.sp, color = flatTextColor, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                                    if (flat.sqFt > 0.0) Text("${flat.sqFt.toInt()} sqft", fontSize = 9.sp, color = if (flatTextColor == Color.White) Color.Gray else flatTextColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            val flatEntries = allEntries.filter { it.workEntry.flatId == flat.id }
                                            val pendingWorkCols = viewModel.getPendingWorksForFlat(flat.id, flatEntries, workColumns)
                                            val pendingWorkColIds = pendingWorkCols.map { it.id }
                                            
                                            // Scrollable columns
                                            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                                workColumns.sortedBy { it.displayOrder }.forEach { col ->
                                                    val entry = flatEntries.find { it.workEntry.workColumnId == col.id }
                                                    val isProblem = entry?.workEntry?.isProblem == true
                                                    val isDone = entry != null && !isProblem
                                                    val isPending = pendingWorkColIds.contains(col.id)
                                                    
                                                    // Colorful cell backgrounds to look beautiful
                                                    val bgColor = if (isProblem) Color(0xFF7F1D1D) // Dark Red for Problem
                                                                  else if (isDone) Color(0xFF064E3B) // Dark Emerald for Done
                                                                  else if (isPending) Color(0xFF451A03) // Dark Amber for Pending
                                                                  else Color.Transparent
                                                    val textColor = if (isProblem) Color(0xFFFCA5A5) 
                                                                    else if (isDone) Color(0xFFD1FAE5)
                                                                    else if (isPending) Color(0xFFFDE68A)
                                                                    else Color.White // White for empty
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .width(100.dp)
                                                            .height(48.dp)
                                                            .background(bgColor)
                                                            .clickable {
                                                                selectedCellForAction = Pair(flat, col)
                                                            }
                                                            .drawBehind {
                                                                drawLine(
                                                                    color = Color(0xFF333333),
                                                                    start = Offset(size.width, 0f),
                                                                    end = Offset(size.width, size.height),
                                                                    strokeWidth = 1.dp.toPx()
                                                                )
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (entry != null) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                                Text(entry.mason?.name?.take(10) ?: (if (isProblem) "PROB" else "DONE"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                                                Text(java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.getDefault()).format(java.util.Date(entry.workEntry.date)), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.8f))
                                                            }
                                                        } else {
                                                            Text(
                                                                text = if (isPending) "PEND" else "-",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textColor
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if ((rowIndex + 1) % 5 == 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black)
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                com.example.ui.components.AdBanner()
                                            
}
}
}
}
}
}
}
}
}
}
}
@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
        }
    }
}

data class ExcelStyleProperties(
    val bgColor: String?,
    val textColor: String?,
    val isBold: Boolean,
    val isItalic: Boolean
)

fun getStyleProperties(key: String, stylesMap: Map<String, String>): ExcelStyleProperties {
    val raw = stylesMap[key] ?: return ExcelStyleProperties(null, null, false, false)
    val parts = raw.split("|")
    val bg = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
    val tc = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
    val bold = parts.getOrNull(2)?.toBoolean() ?: false
    val italic = parts.getOrNull(3)?.toBoolean() ?: false
    return ExcelStyleProperties(bg, tc, bold, italic)
}

fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        null
    }
}

val Slate300 = Color(0xFFCBD5E1)

@Composable
fun ExcelStyleSelector(
    currentBgColor: String,
    currentTextColor: String,
    currentIsBold: Boolean,
    currentIsItalic: Boolean,
    onStyleChange: (bgColor: String, textColor: String, isBold: Boolean, isItalic: Boolean) -> Unit
) {
    val bgColors = listOf(
        "" to "None",
        "#E0F2FE" to "Blue",
        "#DCFCE7" to "Green",
        "#FEF9C3" to "Yellow",
        "#FEE2E2" to "Red",
        "#F3E8FF" to "Purple",
        "#FFEDD5" to "Orange"
    )

    val textColors = listOf(
        "" to "Default",
        "#0369A1" to "Blue",
        "#15803D" to "Green",
        "#B91C1C" to "Red",
        "#7E22CE" to "Purple",
        "#334155" to "Charcoal"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Format Cell Style", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate500)

        // Bold & Italic row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = currentIsBold,
                onClick = { onStyleChange(currentBgColor, currentTextColor, !currentIsBold, currentIsItalic) },
                label = { Text("Bold", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    if (currentIsBold) {
                        Icon(Icons.Default.Check, contentDescription = "Checked", modifier = Modifier.size(12.dp))
                    }
                }
            )

            FilterChip(
                selected = currentIsItalic,
                onClick = { onStyleChange(currentBgColor, currentTextColor, currentIsBold, !currentIsItalic) },
                label = { Text("Italic", fontSize = 11.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) },
                leadingIcon = {
                    if (currentIsItalic) {
                        Icon(Icons.Default.Check, contentDescription = "Checked", modifier = Modifier.size(12.dp))
                    }
                }
            )
        }

        // Background Color Row
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Fill Color", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Bold)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bgColors) { pair ->
                    val colorHex = pair.first
                    val isSelected = currentBgColor == colorHex
                    val parsedColor = if (colorHex.isEmpty()) Color.LightGray else Color(android.graphics.Color.parseColor(colorHex))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(parsedColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Slate300,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onStyleChange(colorHex, currentTextColor, currentIsBold, currentIsItalic)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorHex.isEmpty()) {
                            Text("None", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        } else if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Slate800, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Text Color Row
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Text Color", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Bold)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(textColors) { pair ->
                    val colorHex = pair.first
                    val isSelected = currentTextColor == colorHex
                    val parsedColor = if (colorHex.isEmpty()) Slate900 else Color(android.graphics.Color.parseColor(colorHex))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (colorHex.isEmpty()) Color.White else parsedColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Slate300,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onStyleChange(currentBgColor, colorHex, currentIsBold, currentIsItalic)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorHex.isEmpty()) {
                            Text("Def", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        } else if (isSelected) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = "Selected", 
                                tint = if (colorHex == "#FEF9C3") Color.Black else Color.White, 
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text("A", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (colorHex.isEmpty()) Slate900 else parsedColor)
                        }
                    }
                }
            }
        }
    }
}