package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.example.ui.theme.Slate200
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val allEntries by viewModel.allWorkEntries.collectAsState()
    val allOtherEntries by viewModel.allOtherWorkEntries.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) } // null = All Dates

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
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
                title = { Text(if (selectedDateMillis == null) "All Work Logs" else "Logs for " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!)), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                    if (selectedDateMillis != null) {
                        TextButton(onClick = { selectedDateMillis = null }) { Text("Clear") }
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            // Format dates as strings to easily compare without time portion
            val selectedDateStr = selectedDateMillis?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) }

            val filteredWorks = allEntries.filter { 
                selectedDateStr == null || SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.workEntry.date)) == selectedDateStr 
            }
            val filteredOtherWorks = allOtherEntries.filter { 
                selectedDateStr == null || SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.otherWorkEntry.date)) == selectedDateStr 
            }

            val mappedRegular = filteredWorks.map { Pair<String, Any>("regular", it) }
            val mappedOther = filteredOtherWorks.map { Pair<String, Any>("other", it) }
            
            val combinedWorks = (mappedRegular + mappedOther)
                .sortedByDescending { 
                    if (it.first == "regular") (it.second as com.example.data.WorkEntryWithDetails).workEntry.date 
                    else (it.second as com.example.data.OtherWorkEntryWithDetails).otherWorkEntry.date 
                }

            if (combinedWorks.isEmpty()) {
                Text("No work logged for this selection.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(combinedWorks) { (type, entry) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == "regular") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (type == "regular") {
                                    val work = entry as com.example.data.WorkEntryWithDetails
                                    Text("Flat: ${work.flat.name}", style = MaterialTheme.typography.titleMedium)
                                    val colName = workColumns.find { it.id == work.workEntry.workColumnId }?.name ?: "Unknown"
                                    if (work.workEntry.isProblem) {
                                        Text("Work: $colName (PROBLEM / PENDING)", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                        work.workEntry.description?.let { Text("Note: $it") }
                                    } else {
                                        Text("Work: $colName")
                                        Text("Worker: ${work.mason?.name ?: "Unknown"}")
                                        work.helper?.let { Text("Helper: ${it.name}") }
                                        work.workEntry.description?.let { Text("Note: $it") }
                                    }
                                    Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(work.workEntry.date))}")
                                } else {
                                    val work = entry as com.example.data.OtherWorkEntryWithDetails
                                    Text("Other Work", style = MaterialTheme.typography.titleMedium)
                                    Text("Description: ${work.otherWorkEntry.description}")
                                    Text("Worker: ${work.person.name}")
                                    work.helper?.let { Text("Helper: ${it.name}") }
                                    Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(work.otherWorkEntry.date))}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
