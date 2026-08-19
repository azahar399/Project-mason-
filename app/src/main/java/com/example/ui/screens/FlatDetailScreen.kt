package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.Green50
import com.example.ui.theme.Orange50
import com.example.ui.theme.Slate200
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatDetailScreen(
    flatId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToAddWork: (Int) -> Unit
) {
    val flats by viewModel.allFlats.collectAsState()
    val allEntries by viewModel.allWorkEntries.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()
    val flat = flats.find { it.id == flatId }
    val flatEntries = allEntries.filter { it.workEntry.flatId == flatId }
    val pendingWorkCols = viewModel.getPendingWorksForFlat(flatId, flatEntries, workColumns)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flat: ${flat?.name ?: ""}", fontWeight = FontWeight.SemiBold) },
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
        },
        floatingActionButton = {
            if (pendingWorkCols.isNotEmpty()) {
                FloatingActionButton(onClick = { onNavigateToAddWork(flatId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Work")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text("Completed Works", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (flatEntries.isEmpty()) {
                Text("No work completed yet.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(flatEntries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Green50)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val colName = workColumns.find { it.id == entry.workEntry.workColumnId }?.name ?: "Unknown"
                                if (entry.workEntry.isProblem) {
                                    Text("$colName (PROBLEM / PENDING)", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB91C1C))
                                    entry.workEntry.description?.let { Text("Note: $it") }
                                } else {
                                    Text(colName, style = MaterialTheme.typography.titleMedium)
                                    Text("Worker: ${entry.mason?.name ?: "Unknown"}")
                                    entry.helper?.let { Text("Helper: ${it.name}") }
                                    entry.workEntry.description?.let { Text("Note: $it") }
                                }
                                Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(entry.workEntry.date))}")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Pending Works", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (pendingWorkCols.isEmpty()) {
                Text("All works completed!")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(pendingWorkCols) { col ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Orange50)
                        ) {
                            Text(
                                text = col.name,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
