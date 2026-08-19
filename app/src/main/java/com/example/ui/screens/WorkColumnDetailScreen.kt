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
import com.example.ui.theme.Green50
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkColumnDetailScreen(
    workColumnId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val workColumns by viewModel.allWorkColumns.collectAsState()
    val allFlats by viewModel.allFlats.collectAsState()
    val allEntries by viewModel.allWorkEntries.collectAsState()

    val col = workColumns.find { it.id == workColumnId }

    if (col == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Done works for this column
    val doneEntries = allEntries.filter { it.workEntry.workColumnId == workColumnId }
    val doneFlatIds = doneEntries.map { it.workEntry.flatId }.toSet()

    // Pending works
    val pendingFlats = allFlats.filter { flat ->
        val flatEntries = allEntries.filter { it.workEntry.flatId == flat.id }
        val pendingCols = viewModel.getPendingWorksForFlat(flat.id, flatEntries, workColumns)
        pendingCols.any { it.id == workColumnId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(col.name + " History", fontWeight = FontWeight.SemiBold) },
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
            
            Text("Completed", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(doneEntries.sortedByDescending { it.workEntry.date }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Green50)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Flat: ${entry.flat.name}", style = MaterialTheme.typography.titleMedium)
                            if (entry.workEntry.isProblem) {
                                Text("(PROBLEM / PENDING)", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                entry.workEntry.description?.let { Text("Note: $it") }
                            } else {
                                Text("Worker: ${entry.mason?.name ?: "Unknown"}")
                                entry.helper?.let { Text("Helper: ${it.name}") }
                                entry.workEntry.description?.let { Text("Note: $it") }
                            }
                            Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(entry.workEntry.date))}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Currently Pending for Flats", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pendingFlats) { flat ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(flat.name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
