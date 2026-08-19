package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToPerson: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val triggerAd = {
        val activity = context as? android.app.Activity
        if (activity != null) {
            val adManager = (context.applicationContext as com.example.MasonApplication).adManager
            adManager?.incrementActionAndShowInterstitial(activity)
        }
    }

    val masons by viewModel.allMasons.collectAsState()
    val helpers by viewModel.allHelpers.collectAsState()

    var showAddPerson by remember { mutableStateOf(false) }
    var personToDelete by remember { mutableStateOf<com.example.data.Person?>(null) }
    var newPersonName by remember { mutableStateOf("") }
    var isMason by remember { mutableStateOf(true) }
    var dailyRateText by remember { mutableStateOf("800") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masons & Helpers", fontWeight = FontWeight.SemiBold) },
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
            FloatingActionButton(onClick = { showAddPerson = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }

            if (showAddPerson) {
                AlertDialog(
                    onDismissRequest = { showAddPerson = false },
                    title = { Text("Add Person") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newPersonName,
                                onValueChange = { newPersonName = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    RadioButton(selected = isMason, onClick = { 
                                        isMason = true 
                                        dailyRateText = "800"
                                    })
                                    Text("Worker")
                                }
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    RadioButton(selected = !isMason, onClick = { 
                                        isMason = false 
                                        dailyRateText = "500"
                                    })
                                    Text("Helper")
                                }
                            }
                            OutlinedTextField(
                                value = dailyRateText,
                                onValueChange = { dailyRateText = it },
                                label = { Text("Daily Rate (Tk )") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newPersonName.isNotBlank()) {
                                val rate = dailyRateText.toDoubleOrNull() ?: 0.0
                                viewModel.insertPerson(newPersonName, isMason, rate)
                                showAddPerson = false
                                newPersonName = ""
                                triggerAd()
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddPerson = false }) { Text("Cancel") }
                    }
                )
            }

            if (personToDelete != null) {
                AlertDialog(
                    onDismissRequest = { personToDelete = null },
                    title = { Text("Delete Person") },
                    text = { Text("Are you sure you want to delete ${personToDelete?.name}? This will clear their reference from work logs.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                personToDelete?.let { viewModel.deletePerson(it) }
                                personToDelete = null
                                triggerAd()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { personToDelete = null }) { Text("Cancel") }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text("Masons", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(masons) { person ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToPerson(person.id) }
                                .padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(person.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            IconButton(onClick = { personToDelete = person }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Worker", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Helpers", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(helpers) { person ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToPerson(person.id) }
                                .padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(person.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            IconButton(onClick = { personToDelete = person }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Helper", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
