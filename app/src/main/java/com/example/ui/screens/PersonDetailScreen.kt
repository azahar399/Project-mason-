package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun PersonDetailScreen(
    personId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val masons by viewModel.allMasons.collectAsState()
    val helpers by viewModel.allHelpers.collectAsState()
    val allWorkEntries by viewModel.allWorkEntries.collectAsState()
    val allOtherWorkEntries by viewModel.allOtherWorkEntries.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()

    val person = masons.find { it.id == personId } ?: helpers.find { it.id == personId }

    if (person == null) {
        // Handle not found
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Work done by this person (either as mason or helper)
    val regularWorks = allWorkEntries.filter { it.workEntry.masonId == personId || it.workEntry.helperId == personId }
    val otherWorks = allOtherWorkEntries.filter { it.otherWorkEntry.personId == personId || it.otherWorkEntry.helperId == personId }

    val mappedRegular = regularWorks.map { Pair<String, Any>("regular", it) }
    val mappedOther = otherWorks.map { Pair<String, Any>("other", it) }

    val combinedWorks = (mappedRegular + mappedOther)
        .sortedByDescending { 
            if (it.first == "regular") (it.second as com.example.data.WorkEntryWithDetails).workEntry.date 
            else (it.second as com.example.data.OtherWorkEntryWithDetails).otherWorkEntry.date 
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(person.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
            Text("Work History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (combinedWorks.isEmpty()) {
                Text("No work logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(combinedWorks) { (type, entry) ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (type == "regular") {
                                    val work = entry as com.example.data.WorkEntryWithDetails
                                    val colName = workColumns.find { it.id == work.workEntry.workColumnId }?.name ?: "Unknown"
                                    if (work.workEntry.isProblem) {
                                        Text("Flat: ${work.flat.name} - $colName (PROBLEM / PENDING)", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB91C1C))
                                        work.workEntry.description?.let { Text("Note: $it") }
                                    } else {
                                        Text("Flat: ${work.flat.name} - $colName", style = MaterialTheme.typography.titleMedium)
                                        work.workEntry.description?.let { Text("Note: $it") }
                                    }
                                    val role = if (work.workEntry.masonId == personId) "Worker" else "Helper"
                                    Text("Role: $role")
                                    Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(work.workEntry.date))}")
                                } else {
                                    val work = entry as com.example.data.OtherWorkEntryWithDetails
                                    Text("Other Work", style = MaterialTheme.typography.titleMedium)
                                    val role = if (work.otherWorkEntry.personId == personId) "Worker" else "Helper"
                                    Text("Role: $role")
                                    Text("Desc: ${work.otherWorkEntry.description}")
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
