package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.example.ui.theme.Slate200
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOtherWorkScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val persons by viewModel.allMasons.collectAsState()
    val helpers by viewModel.allHelpers.collectAsState()

    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var selectedHelperId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var personExpanded by remember { mutableStateOf(false) }
    var helperExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                title = { Text("Log Other Work", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Log work done outside of standard flat jobs.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Person Dropdown
            ExposedDropdownMenuBox(expanded = personExpanded, onExpandedChange = { personExpanded = it }) {
                OutlinedTextField(
                    value = persons.find { it.id == selectedPersonId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("1. Select Worker") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = personExpanded, onDismissRequest = { personExpanded = false }) {
                    persons.forEach { person ->
                        DropdownMenuItem(
                            text = { Text(person.name) },
                            onClick = { selectedPersonId = person.id; personExpanded = false }
                        )
                    }
                }
            }

            // Helper Dropdown
            ExposedDropdownMenuBox(
                expanded = helperExpanded,
                onExpandedChange = { helperExpanded = it }
            ) {
                OutlinedTextField(
                    value = helpers.find { it.id == selectedHelperId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("2. Select Helper (Optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = helperExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = helperExpanded, onDismissRequest = { helperExpanded = false }) {
                    DropdownMenuItem(text = { Text("None") }, onClick = { selectedHelperId = null; helperExpanded = false })
                    helpers.forEach { helper ->
                        DropdownMenuItem(
                            text = { Text(helper.name) },
                            onClick = { selectedHelperId = helper.id; helperExpanded = false }
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("3. Work Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Date Picker Field
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("4. Date") },
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedPersonId != null && description.isNotBlank()) {
                        viewModel.insertOtherWorkEntry(
                            personId = selectedPersonId!!,
                            helperId = selectedHelperId,
                            description = description,
                            date = selectedDateMillis
                        )
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            val adManager = (context.applicationContext as com.example.MasonApplication).adManager
                            adManager?.incrementActionAndShowInterstitial(activity)
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedPersonId != null && description.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Other Work Entry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
