package com.example.ui.screens

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val allEntries by viewModel.allWorkEntries.collectAsState()
    val flats by viewModel.allFlats.collectAsState()
    val masons by viewModel.allMasons.collectAsState()
    val helpers by viewModel.allHelpers.collectAsState()
    val persons = (masons + helpers).distinctBy { it.id }
    val workColumns by viewModel.allWorkColumns.collectAsState()

    var selectedFlatId by remember { mutableStateOf<Int?>(null) }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var selectedWorkColumnId by remember { mutableStateOf<Int?>(null) }
    var selectedStatus by remember { mutableStateOf<String>("All") } // All, Completed, Pending (Problem)
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    var showFlatDropdown by remember { mutableStateOf(false) }
    var showPersonDropdown by remember { mutableStateOf(false) }
    var showWorkColumnDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val activity = context as? android.app.Activity
    val adManager = (context.applicationContext as com.example.MasonApplication).adManager

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val filteredEntries = filterEntries(allEntries, selectedFlatId, selectedPersonId, selectedWorkColumnId, selectedStatus, selectedDateMillis)
                val outputStream = context.contentResolver.openOutputStream(it)
                generatePdf(context, outputStream, filteredEntries, flats, persons, workColumns)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedDateMillis = null
                    showDatePicker = false
                }) { Text("Clear Date") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Flat Filter
            ExposedDropdownMenuBox(
                expanded = showFlatDropdown,
                onExpandedChange = { showFlatDropdown = it }
            ) {
                OutlinedTextField(
                    value = flats.find { it.id == selectedFlatId }?.name ?: "All Flats",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Flat") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFlatDropdown) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showFlatDropdown, onDismissRequest = { showFlatDropdown = false }) {
                    DropdownMenuItem(text = { Text("All Flats") }, onClick = { selectedFlatId = null; showFlatDropdown = false })
                    flats.forEach { flat ->
                        DropdownMenuItem(
                            text = { Text(flat.name) },
                            onClick = { selectedFlatId = flat.id; showFlatDropdown = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Person Filter
            ExposedDropdownMenuBox(
                expanded = showPersonDropdown,
                onExpandedChange = { showPersonDropdown = it }
            ) {
                OutlinedTextField(
                    value = persons.find { it.id == selectedPersonId }?.name ?: "All Persons",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Person") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPersonDropdown) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showPersonDropdown, onDismissRequest = { showPersonDropdown = false }) {
                    DropdownMenuItem(text = { Text("All Persons") }, onClick = { selectedPersonId = null; showPersonDropdown = false })
                    persons.forEach { person ->
                        DropdownMenuItem(
                            text = { Text(person.name) },
                            onClick = { selectedPersonId = person.id; showPersonDropdown = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            // Work Column Filter
            ExposedDropdownMenuBox(
                expanded = showWorkColumnDropdown,
                onExpandedChange = { showWorkColumnDropdown = it }
            ) {
                OutlinedTextField(
                    value = workColumns.find { it.id == selectedWorkColumnId }?.name ?: "All Works",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Work Column") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWorkColumnDropdown) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showWorkColumnDropdown, onDismissRequest = { showWorkColumnDropdown = false }) {
                    DropdownMenuItem(text = { Text("All Works") }, onClick = { selectedWorkColumnId = null; showWorkColumnDropdown = false })
                    workColumns.forEach { col ->
                        DropdownMenuItem(
                            text = { Text(col.name) },
                            onClick = { selectedWorkColumnId = col.id; showWorkColumnDropdown = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            // Status Filter
            ExposedDropdownMenuBox(
                expanded = showStatusDropdown,
                onExpandedChange = { showStatusDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showStatusDropdown, onDismissRequest = { showStatusDropdown = false }) {
                    listOf("All", "Completed", "Pending (Problem)").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = { selectedStatus = status; showStatusDropdown = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Date Filter
            val dateStr = selectedDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "All Dates"
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val fileName = "WorkReport_${sdf.format(Date())}.pdf"
                        if (activity != null) {
                            adManager?.showRewardedAd(activity) {
                                createDocumentLauncher.launch(fileName)
                            }
                        } else {
                            createDocumentLauncher.launch(fileName)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Download")
                }
                
                OutlinedButton(
                    onClick = {
                        val action = {
                            coroutineScope.launch {
                                val filteredEntries = filterEntries(allEntries, selectedFlatId, selectedPersonId, selectedWorkColumnId, selectedStatus, selectedDateMillis)
                                sharePdf(context, filteredEntries, flats, persons, workColumns)
                            }
                        }
                        if (activity != null) {
                            adManager?.showRewardedAd(activity) {
                                action()
                            }
                        } else {
                            action()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val filteredEntries = filterEntries(allEntries, selectedFlatId, selectedPersonId, selectedWorkColumnId, selectedStatus, selectedDateMillis)
            Text("Showing ${filteredEntries.size} entries", fontWeight = FontWeight.Bold)
            
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredEntries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Flat: ${flats.find { it.id == entry.workEntry.flatId }?.name ?: "N/A"}", fontWeight = FontWeight.Bold)
                            Text("Work: ${workColumns.find { it.id == entry.workEntry.workColumnId }?.name ?: "N/A"}")
                            val masonName = persons.find { it.id == entry.workEntry.masonId }?.name
                            val helperName = persons.find { it.id == entry.workEntry.helperId }?.name
                            if (masonName != null) Text("Worker: $masonName")
                            if (helperName != null) Text("Helper: $helperName")
                            val eDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.workEntry.date))
                            Text("Date: $eDate")
                            if (entry.workEntry.isProblem) {
                                Text("Status: Problem", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Status: Completed")
                            }
                            if (!entry.workEntry.description.isNullOrBlank()) {
                                Text("Note: ${entry.workEntry.description}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun filterEntries(
    allEntries: List<WorkEntryWithDetails>,
    selectedFlatId: Int?,
    selectedPersonId: Int?,
    selectedWorkColumnId: Int?,
    selectedStatus: String,
    selectedDateMillis: Long?
): List<WorkEntryWithDetails> {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val selectedDateStr = selectedDateMillis?.let { sdf.format(Date(it)) }

    return allEntries.filter { entry ->
        val matchFlat = selectedFlatId == null || entry.workEntry.flatId == selectedFlatId
        val matchPerson = selectedPersonId == null || entry.workEntry.masonId == selectedPersonId || entry.workEntry.helperId == selectedPersonId
        val matchWork = selectedWorkColumnId == null || entry.workEntry.workColumnId == selectedWorkColumnId
        val matchStatus = when (selectedStatus) {
            "Completed" -> !entry.workEntry.isProblem
            "Pending (Problem)" -> entry.workEntry.isProblem
            else -> true
        }
        val matchDate = selectedDateStr == null || sdf.format(Date(entry.workEntry.date)) == selectedDateStr

        matchFlat && matchPerson && matchWork && matchStatus && matchDate
    }
}

fun generatePdf(
    context: Context,
    outputStream: java.io.OutputStream?,
    entries: List<WorkEntryWithDetails>,
    flats: List<Flat>,
    persons: List<Person>,
    workColumns: List<WorkColumn>
) {
    try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("Work Report", 50f, 50f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        var y = 100f

        for (entry in entries) {
            if (y > 750f) {
                document.finishPage(page)
                // In a real app we'd create more pages, keeping it simple here
                break
            }
            val flatName = flats.find { it.id == entry.workEntry.flatId }?.name ?: "N/A"
            val workName = workColumns.find { it.id == entry.workEntry.workColumnId }?.name ?: "N/A"
            val masonName = persons.find { it.id == entry.workEntry.masonId }?.name ?: "None"
            val eDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.workEntry.date))
            
            canvas.drawText("Flat: $flatName | Work: $workName | Date: $eDate", 50f, y, paint)
            y += 20f
            canvas.drawText("Worker: $masonName | Status: ${if (entry.workEntry.isProblem) "Problem" else "Completed"}", 50f, y, paint)
            y += 30f
        }

        document.finishPage(page)
        
        outputStream?.let { 
            document.writeTo(it)
            it.close()
        }
        document.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sharePdf(
    context: Context,
    entries: List<WorkEntryWithDetails>,
    flats: List<Flat>,
    persons: List<Person>,
    workColumns: List<WorkColumn>
) {
    try {
        val pdfsDir = java.io.File(context.cacheDir, "pdfs")
        if (!pdfsDir.exists()) {
            pdfsDir.mkdirs()
        }
        val file = java.io.File(pdfsDir, "WorkReport_${System.currentTimeMillis()}.pdf")
        val outputStream = java.io.FileOutputStream(file)
        
        generatePdf(context, outputStream, entries, flats, persons, workColumns)
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Work Report"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
