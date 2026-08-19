package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkScreen(
    initialFlatId: Int?,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val floors by viewModel.allFloors.collectAsState()
    val flats by viewModel.allFlats.collectAsState()
    val allEntries by viewModel.allWorkEntries.collectAsState()
    val masons by viewModel.allMasons.collectAsState()
    val helpers by viewModel.allHelpers.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()
    
    var selectedFloorId by remember { mutableStateOf<Int?>(null) }
    var selectedFlatId by remember { mutableStateOf(initialFlatId) }
    
    var isBulkEntry by remember { mutableStateOf(false) }
    var selectedBulkFlatIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var bulkFlatNamesText by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(selectedFlatId, flats) {
        if (selectedFlatId != null) {
            val flat = flats.find { it.id == selectedFlatId }
            if (flat != null) {
                selectedFloorId = flat.floorId
            }
        }
    }
    
    val flatEntries = allEntries.filter { it.workEntry.flatId == selectedFlatId }
    val pendingWorkCols = if (selectedFlatId != null) viewModel.getPendingWorksForFlat(selectedFlatId!!, flatEntries, workColumns) else emptyList()
    val activeWorkColumns = remember(workColumns) { workColumns.filter { it.name.isNotBlank() } }
    
    var selectedWorkColumnId by remember { mutableStateOf<Int?>(if (initialFlatId != null) pendingWorkCols.firstOrNull()?.id else null) }
    var selectedMasonId by remember { mutableStateOf<Int?>(null) }
    var selectedHelperId by remember { mutableStateOf<Int?>(null) }
    
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var floorExpanded by remember { mutableStateOf(false) }
    var flatExpanded by remember { mutableStateOf(false) }
    var workExpanded by remember { mutableStateOf(false) }
    var masonExpanded by remember { mutableStateOf(false) }
    var helperExpanded by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var isProblem by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    
    val existingEntry = remember(selectedFlatId, selectedWorkColumnId, allEntries) {
        if (selectedFlatId != null && selectedWorkColumnId != null) {
            allEntries.find { it.workEntry.flatId == selectedFlatId && it.workEntry.workColumnId == selectedWorkColumnId }
        } else null
    }
    
    val existingBulkEntriesCount = remember(selectedBulkFlatIds, selectedWorkColumnId, allEntries) {
        if (selectedBulkFlatIds.isNotEmpty() && selectedWorkColumnId != null) {
            allEntries.count { selectedBulkFlatIds.contains(it.workEntry.flatId) && it.workEntry.workColumnId == selectedWorkColumnId }
        } else 0
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Flat Work") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bulk entry toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isBulkEntry, onCheckedChange = { isBulkEntry = it })
                Text("Bulk Entry (Multiple Flats)")
            }

            // Floor Selection
            ExposedDropdownMenuBox(expanded = floorExpanded, onExpandedChange = { floorExpanded = it }) {
                OutlinedTextField(
                    value = floors.find { it.id == selectedFloorId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Floor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = floorExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = floorExpanded, onDismissRequest = { floorExpanded = false }) {
                    floors.forEach { floor ->
                        DropdownMenuItem(
                            text = { Text(floor.name) },
                            onClick = {
                                selectedFloorId = floor.id
                                floorExpanded = false
                            }
                        )
                    }
                }
            }

            // Flat Selection
            if (!isBulkEntry) {
                ExposedDropdownMenuBox(expanded = flatExpanded, onExpandedChange = { flatExpanded = it }) {
                    OutlinedTextField(
                        value = flats.find { it.id == selectedFlatId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Flat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = flatExpanded, onDismissRequest = { flatExpanded = false }) {
                        val currentFlats = if (selectedFloorId != null) flats.filter { it.floorId == selectedFloorId } else flats
                        currentFlats.forEach { flat ->
                            DropdownMenuItem(
                                text = { Text(flat.name) },
                                onClick = {
                                    selectedFlatId = flat.id
                                    selectedFloorId = flat.floorId
                                    flatExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = bulkFlatNamesText,
                    onValueChange = { 
                        bulkFlatNamesText = it
                        val names = it.split(",").map{ n -> n.trim().lowercase() }.filter{ n -> n.isNotEmpty() }
                        selectedBulkFlatIds = flats.filter { f -> names.contains(f.name.lowercase()) }.map { f -> f.id }.toSet()
                    },
                    label = { Text("Flat Names (comma separated)") },
                    placeholder = { Text("e.g. 101, 102") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Problem Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isProblem, onCheckedChange = { isProblem = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log as Problem", color = MaterialTheme.colorScheme.error)
            }

            // Work Column Selection
            ExposedDropdownMenuBox(expanded = workExpanded, onExpandedChange = { workExpanded = it }) {
                OutlinedTextField(
                    value = workColumns.find { it.id == selectedWorkColumnId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Work Component") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = workExpanded, onDismissRequest = { workExpanded = false }) {
                    activeWorkColumns.forEach { col ->
                        DropdownMenuItem(
                            text = { Text(col.name) },
                            onClick = {
                                selectedWorkColumnId = col.id
                                workExpanded = false
                            }
                        )
                    }
                }
            }

            // Worker Selection
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = masonExpanded, 
                    onExpandedChange = { masonExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = masons.find { it.id == selectedMasonId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = masonExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = masonExpanded, onDismissRequest = { masonExpanded = false }) {
                        masons.forEach { mason ->
                            DropdownMenuItem(
                                text = { Text(mason.name) },
                                onClick = {
                                    selectedMasonId = mason.id
                                    masonExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = helperExpanded, 
                    onExpandedChange = { helperExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = helpers.find { it.id == selectedHelperId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Helper (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = helperExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = helperExpanded, onDismissRequest = { helperExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedHelperId = null
                                helperExpanded = false
                            }
                        )
                        helpers.forEach { helper ->
                            DropdownMenuItem(
                                text = { Text(helper.name) },
                                onClick = {
                                    selectedHelperId = helper.id
                                    helperExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            val parsedBulkNames = bulkFlatNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            val isFormValid = if (isBulkEntry) {
                parsedBulkNames.isNotEmpty() && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)
            } else {
                selectedFlatId != null && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)
            }

            val onSave = {
                coroutineScope.launch {
                    if (isBulkEntry) {
                        val names = bulkFlatNamesText.split(",").map{ it.trim().lowercase() }.filter{ it.isNotEmpty() }
                        val matchedFlats = flats.filter { names.contains(it.name.lowercase()) }
                        matchedFlats.forEach { flat ->
                            val flatExistingEntry = allEntries.find { it.workEntry.flatId == flat.id && it.workEntry.workColumnId == selectedWorkColumnId }
                            if (flatExistingEntry != null) {
                                viewModel.deleteWorkEntry(flatExistingEntry.workEntry)
                            }
                            viewModel.insertWorkEntry(
                                flatId = flat.id,
                                workColumnId = selectedWorkColumnId!!,
                                masonId = if (isProblem && selectedMasonId == null) null else selectedMasonId,
                                helperId = selectedHelperId,
                                date = selectedDateMillis,
                                description = description.takeIf { it.isNotBlank() },
                                isProblem = isProblem
                            )
                        }
                    } else {
                        if (selectedFlatId != null && selectedWorkColumnId != null) {
                            if (existingEntry != null) {
                                viewModel.deleteWorkEntry(existingEntry.workEntry)
                            }
                            viewModel.insertWorkEntry(
                                flatId = selectedFlatId!!,
                                workColumnId = selectedWorkColumnId!!,
                                masonId = if (isProblem && selectedMasonId == null) null else selectedMasonId,
                                helperId = selectedHelperId,
                                date = selectedDateMillis,
                                description = description.takeIf { it.isNotBlank() },
                                isProblem = isProblem
                            )
                        }
                    }
                    onBack()
                }
            }

            Button(
                onClick = {
                    val hasExisting = if (isBulkEntry) existingBulkEntriesCount > 0 else existingEntry != null
                    if (hasExisting) {
                        showWarningDialog = true
                    } else {
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProblem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isProblem) "Log Problem" else "Save Entry")
            }

            if (showWarningDialog) {
                AlertDialog(
                    onDismissRequest = { showWarningDialog = false },
                    title = { Text("Entry Exists!") },
                    text = { Text(if (isBulkEntry) "Some of these flats already have an entry for this work. Overwrite?" else "An entry for this work already exists for this flat. Overwrite?") },
                    confirmButton = {
                        Button(onClick = { showWarningDialog = false; onSave() }) { Text("Yes, Overwrite") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWarningDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
