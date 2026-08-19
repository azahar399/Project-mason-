package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWageSheetScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.allPersons.collectAsState()
    val allDailyWageEntries by viewModel.allDailyWageEntries.collectAsState()

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val startOfDay = remember(selectedDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    val dailyEntries = remember(allDailyWageEntries, startOfDay) {
        allDailyWageEntries.filter { it.date == startOfDay }
    }

    var editPerson by remember { mutableStateOf<Person?>(null) }
    var editUnit by remember { mutableStateOf("") }
    var editAdvance by remember { mutableStateOf("") }
    var editReward by remember { mutableStateOf("") }
    var editPenalty by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Sheet", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareDailySheetCsv(context, selectedDateMillis, persons, dailyEntries)
                    }) {
                        Icon(Icons.Default.GridOn, contentDescription = "Export CSV", tint = Color(0xFF16A34A))
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = CleanBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Date Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDateMillis -= 86400000L }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = Color.White)
                }
                Text(
                    text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = { selectedDateMillis += 86400000L }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Day", tint = Color.White)
                }
            }

            // Table Header
            val horizontalScrollState = rememberScrollState()
            
            Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Name", modifier = Modifier.width(120.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Rate", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Unit", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Income", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                    Text("Advance", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEA580C))
                    Text("Reward", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                    Text("Penalty", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                    Text("Due", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (persons.isEmpty()) {
                    Text("No personnel found.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(persons) { person ->
                            val entry = dailyEntries.find { it.personId == person.id }
                            val totalUnits = entry?.unit ?: 0.0
                            val advances = entry?.advance ?: 0.0
                            val rewards = entry?.reward ?: 0.0
                            val penalties = entry?.penalty ?: 0.0
                            
                            val income = totalUnits * person.dailyRate
                            val todayDue = (income + rewards) - (advances + penalties)

                            Row(
                                modifier = Modifier
                                    .clickable {
                                        editPerson = person
                                        editUnit = if (totalUnits == 0.0) "" else totalUnits.toString()
                                        editAdvance = if (advances == 0.0) "" else advances.toString()
                                        editReward = if (rewards == 0.0) "" else rewards.toString()
                                        editPenalty = if (penalties == 0.0) "" else penalties.toString()
                                    }
                                    .background(Color.White)
                                    .drawBehind {
                                        drawLine(Color(0xFFF1F5F9), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                    }
                                    .padding(horizontal = 8.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(person.name, modifier = Modifier.width(120.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                Text("Tk ${person.dailyRate.toInt()}", modifier = Modifier.width(60.dp), fontSize = 13.sp, color = Slate500)
                                Text("$totalUnits", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Tk ${income.toInt()}", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                Text("Tk ${advances.toInt()}", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEA580C))
                                Text("Tk ${rewards.toInt()}", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                                Text("Tk ${penalties.toInt()}", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                                Text("Tk ${todayDue.toInt()}", modifier = Modifier.width(80.dp), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (todayDue >= 0) Color(0xFF16A34A) else Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
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

    if (editPerson != null) {
        AlertDialog(
            onDismissRequest = { editPerson = null },
            title = { Text("Edit Daily Entry: ${editPerson?.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editUnit,
                        onValueChange = { editUnit = it },
                        label = { Text("Kajer Unit (Attendance)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAdvance,
                        onValueChange = { editAdvance = it },
                        label = { Text("Advance Payment (Tk )") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editReward,
                        onValueChange = { editReward = it },
                        label = { Text("Reward / Bonus (Tk )") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPenalty,
                        onValueChange = { editPenalty = it },
                        label = { Text("Debit / Penalty (Tk )") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = editUnit.toDoubleOrNull() ?: 0.0
                        val a = editAdvance.toDoubleOrNull() ?: 0.0
                        val r = editReward.toDoubleOrNull() ?: 0.0
                        val p = editPenalty.toDoubleOrNull() ?: 0.0
                        
                        viewModel.saveDailyWageEntry(
                            personId = editPerson!!.id,
                            date = startOfDay,
                            unit = u,
                            advance = a,
                            reward = r,
                            penalty = p
                        )
                        editPerson = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editPerson = null }) { Text("Cancel") }
            }
        )
    }
}

fun shareDailySheetCsv(
    context: Context, 
    dateMillis: Long,
    persons: List<Person>, 
    dailyEntries: List<DailyWageEntry>
) {
    try {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))
        val ledgerDir = File(context.cacheDir, "ledgers")
        if (!ledgerDir.exists()) ledgerDir.mkdirs()
        val file = File(ledgerDir, "Daily_Sheet_$dateStr.csv")
        val writer = file.printWriter()

        writer.println("Daily Wage Sheet - $dateStr")
        writer.println("Name,Rate,Unit,Income(Tk),Advance(Tk),Reward(Tk),Penalty(Tk),Today Due(Tk)")

        persons.forEach { person ->
            val entry = dailyEntries.find { it.personId == person.id }
            val totalUnits = entry?.unit ?: 0.0
            val advances = entry?.advance ?: 0.0
            val rewards = entry?.reward ?: 0.0
            val penalties = entry?.penalty ?: 0.0
            
            val income = totalUnits * person.dailyRate
            val todayDue = (income + rewards) - (advances + penalties)

            if (totalUnits > 0 || advances > 0 || rewards > 0 || penalties > 0) {
                writer.println("\"${person.name}\",${person.dailyRate.toInt()},$totalUnits,${income.toInt()},${advances.toInt()},${rewards.toInt()},${penalties.toInt()},${todayDue.toInt()}")
            }
        }

        writer.flush()
        writer.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Daily Sheet (CSV)"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
