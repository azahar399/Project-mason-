package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WageLedgerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.allPersons.collectAsState()
    val allWorkEntries by viewModel.allWorkEntries.collectAsState()
    val allOtherWorkEntries by viewModel.allOtherWorkEntries.collectAsState()
    val allDailyWageEntries by viewModel.allDailyWageEntries.collectAsState()
    val workColumns by viewModel.allWorkColumns.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Masons, 1 = Helpers
    var showEditRateDialog by remember { mutableStateOf<Person?>(null) }
    var editRateValue by remember { mutableStateOf("") }

    var selectedPersonForDetails by remember { mutableStateOf<Person?>(null) }

    // Filter persons
    val filteredPersons = remember(persons, selectedTab) {
        persons.filter { it.isMason == (selectedTab == 0) }
    }

    // Calculations helper for each person
    fun getPersonCalculations(person: Person): PersonLedgerStats {
        val regularCount = allWorkEntries.count { it.workEntry.masonId == person.id || it.workEntry.helperId == person.id }
        val otherCount = allOtherWorkEntries.count { it.otherWorkEntry.personId == person.id || it.otherWorkEntry.helperId == person.id }
        
        
        val personTx = allDailyWageEntries.filter { it.personId == person.id }
        val totalDays = personTx.sumOf { it.unit }
        val totalEarned = totalDays * person.dailyRate
        val totalAdvance = personTx.sumOf { it.advance }
        val totalReward = personTx.sumOf { it.reward }
        val totalPenalty = personTx.sumOf { it.penalty }
        val balance = (totalEarned + totalReward) - (totalAdvance + totalPenalty)
        return PersonLedgerStats(
            totalDays = totalDays,
            totalEarned = totalEarned,
            totalAdvance = totalAdvance,
            totalReward = totalReward,
            totalPenalty = totalPenalty,
            balance = balance,
            regularCount = regularCount,
            otherCount = otherCount
        )
    }

    // Overall summary statistics
    val totalEarnedAll = remember(persons, allWorkEntries, allOtherWorkEntries) {
        persons.sumOf { getPersonCalculations(it).totalEarned }
    }
    val totalAdvanceAll = remember(allDailyWageEntries) {
        allDailyWageEntries.sumOf { it.advance }
    }
    val totalBalanceAll = totalEarnedAll - totalAdvanceAll

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wage Ledger", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showExportMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Export All", tint = PrimaryColor)
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("PDF Share") },
                                onClick = {
                                    showExportMenu = false
                                    shareMasterLedgerPdf(context, persons, allDailyWageEntries)
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TertiaryColor) }
                            )
                            DropdownMenuItem(
                                text = { Text("CSV Share") },
                                onClick = {
                                    showExportMenu = false
                                    shareMasterLedgerCsv(context, persons, allDailyWageEntries)
                                },
                                leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF16A34A)) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
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
        containerColor = CleanBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Overview summary cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Earned",
                    amount = "Tk ${String.format(Locale.US, "%,.0f", totalEarnedAll)}",
                    containerColor = Blue50,
                    contentColor = PrimaryColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Advance",
                    amount = "Tk ${String.format(Locale.US, "%,.0f", totalAdvanceAll)}",
                    containerColor = Orange50,
                    contentColor = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Due Balance",
                    amount = "Tk ${String.format(Locale.US, "%,.0f", totalBalanceAll)}",
                    containerColor = if (totalBalanceAll >= 0) Green50 else Red50,
                    contentColor = if (totalBalanceAll >= 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs for Masons and Helpers
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Masons (${persons.count { it.isMason }})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Helpers (${persons.count { !it.isMason }})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }

            if (filteredPersons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No personnel found under this tab.",
                        color = Slate500,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPersons) { person ->
                        val stats = getPersonCalculations(person)
        val personTx = allDailyWageEntries.filter { it.personId == person.id }
                        Card(
                            onClick = { selectedPersonForDetails = person },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                    // Rate modifier
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate100)
                                            .clickable {
                                                showEditRateDialog = person
                                                editRateValue = person.dailyRate.toInt().toString()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Rate", modifier = Modifier.size(12.dp), tint = PrimaryColor)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Tk ${person.dailyRate.toInt()}/Day",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Attendance", fontSize = 11.sp, color = Slate500)
                                        Text("${stats.totalDays} Day / Days", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Earned", fontSize = 11.sp, color = Slate500)
                                        Text("Tk ${stats.totalEarned.toInt()}", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Advance", fontSize = 11.sp, color = Slate500)
                                        Text("Tk ${stats.totalAdvance.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C), fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (stats.balance >= 0) Green50 else Red50)
                                        .padding(vertical = 6.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (stats.balance >= 0) "Net Balance (Due)" else "Overpaid",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (stats.balance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                        Text(
                                            text = "Tk ${Math.abs(stats.balance).toInt()}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (stats.balance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Daily Rate Dialog
    if (showEditRateDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditRateDialog = null },
            title = { Text("Edit Rate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Personnel: ${showEditRateDialog?.name}", fontWeight = FontWeight.Bold, color = Slate800)
                    OutlinedTextField(
                        value = editRateValue,
                        onValueChange = { editRateValue = it },
                        label = { Text("Daily Rate (Tk )") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newRate = editRateValue.toDoubleOrNull() ?: 0.0
                        viewModel.updatePerson(showEditRateDialog!!.copy(dailyRate = newRate))
                        showEditRateDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRateDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Personal Ledger Bottom Sheet / Details Dialog
    if (selectedPersonForDetails != null) {
        val person = selectedPersonForDetails!!
        val stats = getPersonCalculations(person)
        val personTx = allDailyWageEntries.filter { it.personId == person.id }

        // Combined logs sorted by date descending
        val combinedLogs = remember(personTx) {
            val list = mutableListOf<LedgerLogItem>()
            personTx.forEach {
                list.add(LedgerLogItem(it.date, it.unit, it.unit * person.dailyRate, it.advance, it.reward, it.penalty))
            }
            list.sortedByDescending { it.date }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedPersonForDetails = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = CleanBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${person.name} - Detailed Ledger",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Slate800,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Earned", fontSize = 12.sp, color = Slate500)
                        Text("Tk ${stats.totalEarned.toInt()}", fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Advance", fontSize = 12.sp, color = Slate500)
                        Text("Tk ${stats.totalAdvance.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reward", fontSize = 12.sp, color = Slate500)
                        Text("Tk ${stats.totalReward.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Penalty", fontSize = 12.sp, color = Slate500)
                        Text("Tk ${stats.totalPenalty.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Logs", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    IconButton(onClick = {
                        sharePersonLedgerPdf(context, person, stats, combinedLogs)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = TertiaryColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                if (combinedLogs.isEmpty()) {
                    Text("No records found", color = Slate500)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(combinedLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(log.date)),
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Attendance: ${log.unit}", fontSize = 13.sp)
                                        Text("Income: Tk ${log.income.toInt()}", fontSize = 13.sp, color = Color(0xFF16A34A))
                                    }
                                    if (log.advance > 0) Text("Advance: Tk ${log.advance.toInt()}", fontSize = 13.sp, color = Color(0xFFEA580C))
                                    if (log.reward > 0) Text("Reward: Tk ${log.reward.toInt()}", fontSize = 13.sp, color = Color(0xFF2563EB))
                                    if (log.penalty > 0) Text("Penalty: Tk ${log.penalty.toInt()}", fontSize = 13.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class LedgerLogItem(
    val date: Long,
    val unit: Double,
    val income: Double,
    val advance: Double,
    val reward: Double,
    val penalty: Double
)

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .height(90.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Slate500, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = modifier.height(4.dp))
            Text(amount, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun shareMasterLedgerPdf(context: Context, persons: List<Person>, allDailyWageEntries: List<DailyWageEntry>) {
    val activity = context as? Activity
    if (activity != null) {
        val adManager = (context.applicationContext as com.example.MasonApplication).adManager
        adManager?.showRewardedAd(activity) {
            generateMasterPdf(context, persons, allDailyWageEntries)
        }
    } else {
        generateMasterPdf(context, persons, allDailyWageEntries)
    }
}

fun shareMasterLedgerCsv(context: Context, persons: List<Person>, allDailyWageEntries: List<DailyWageEntry>) {
    try {
        val ledgerDir = File(context.cacheDir, "ledgers")
        if (!ledgerDir.exists()) ledgerDir.mkdirs()
        val file = File(ledgerDir, "Master_Wage_Ledger_${System.currentTimeMillis()}.csv")
        val writer = file.printWriter()

        // Headers: Name, Rate, Attendance Unit, Total Income, Advance Taken, Reward, Penalty, Due Balance
        writer.println("Name,Rate,Attendance Unit,Total Income,Advance Taken,Reward,Penalty,Due Balance")

        persons.forEach { person ->
            val personEntries = allDailyWageEntries.filter { it.personId == person.id }
            val totalDays = personEntries.sumOf { it.unit }
            val earned = totalDays * person.dailyRate
            val advance = personEntries.sumOf { it.advance }
            val reward = personEntries.sumOf { it.reward }
            val penalty = personEntries.sumOf { it.penalty }
            val balance = (earned + reward) - (advance + penalty)

            writer.println("\"${person.name}\",${person.dailyRate.toInt()},$totalDays,${earned.toInt()},${advance.toInt()},${reward.toInt()},${penalty.toInt()},${balance.toInt()}")
        }

        writer.flush()
        writer.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Master Ledger (CSV)"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun sharePersonLedgerPdf(context: Context, person: Person, stats: PersonLedgerStats, logs: List<LedgerLogItem>) {
    val activity = context as? Activity
    if (activity != null) {
        val adManager = (context.applicationContext as com.example.MasonApplication).adManager
        adManager?.showRewardedAd(activity) {
            generatePersonPdf(context, person, stats, logs)
        }
    } else {
        generatePersonPdf(context, person, stats, logs)
    }
}

data class PersonLedgerStats(
    val totalDays: Double,
    val totalEarned: Double,
    val totalAdvance: Double,
    val totalReward: Double,
    val totalPenalty: Double,
    val balance: Double,
    val regularCount: Int,
    val otherCount: Int
)

private fun generateMasterPdf(context: Context, persons: List<Person>, allDailyWageEntries: List<DailyWageEntry>) {
    try {
        val document = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Master Wage Ledger", 200f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 100f
        
        canvas.drawText("Name", 50f, y, paint)
        canvas.drawText("Total Days", 200f, y, paint)
        canvas.drawText("Earned", 300f, y, paint)
        canvas.drawText("Advance", 400f, y, paint)
        canvas.drawText("Balance", 500f, y, paint)
        y += 30f

        for (person in persons) {
            val personEntries = allDailyWageEntries.filter { it.personId == person.id }
            val totalDays = personEntries.sumOf { it.unit }
            val earned = totalDays * person.dailyRate
            val advance = personEntries.sumOf { it.advance }
            val reward = personEntries.sumOf { it.reward }
            val penalty = personEntries.sumOf { it.penalty }
            val balance = (earned + reward) - (advance + penalty)

            canvas.drawText(person.name, 50f, y, paint)
            canvas.drawText(totalDays.toString(), 200f, y, paint)
            canvas.drawText(earned.toInt().toString(), 300f, y, paint)
            canvas.drawText(advance.toInt().toString(), 400f, y, paint)
            canvas.drawText(balance.toInt().toString(), 500f, y, paint)
            y += 30f
        }

        document.finishPage(page)

        val ledgerDir = java.io.File(context.cacheDir, "ledgers")
        if (!ledgerDir.exists()) ledgerDir.mkdirs()
        val file = java.io.File(ledgerDir, "Master_Ledger_${System.currentTimeMillis()}.pdf")
        document.writeTo(java.io.FileOutputStream(file))
        document.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))

    } catch (e: Exception) {
        Toast.makeText(context, "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun generatePersonPdf(context: Context, person: Person, stats: PersonLedgerStats, logs: List<LedgerLogItem>) {
    try {
        val document = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = android.graphics.Paint()

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Ledger: ${person.name}", 200f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 100f
        
        canvas.drawText("Total Days: ${stats.totalDays}", 50f, y, paint)
        canvas.drawText("Total Earned: ${stats.totalEarned}", 300f, y, paint)
        y += 30f
        canvas.drawText("Total Advance: ${stats.totalAdvance}", 50f, y, paint)
        canvas.drawText("Balance: ${stats.balance}", 300f, y, paint)
        y += 50f

        canvas.drawText("Date", 50f, y, paint)
        canvas.drawText("Unit", 150f, y, paint)
        canvas.drawText("Income", 250f, y, paint)
        canvas.drawText("Advance", 350f, y, paint)
        y += 30f

        for (log in logs) {
            if (y > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(log.date))
            canvas.drawText(dateStr, 50f, y, paint)
            canvas.drawText(log.unit.toString(), 150f, y, paint)
            canvas.drawText(log.income.toInt().toString(), 250f, y, paint)
            canvas.drawText(log.advance.toInt().toString(), 350f, y, paint)
            y += 30f
        }

        document.finishPage(page)

        val ledgerDir = java.io.File(context.cacheDir, "ledgers")
        if (!ledgerDir.exists()) ledgerDir.mkdirs()
        val file = java.io.File(ledgerDir, "Ledger_${person.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        document.writeTo(java.io.FileOutputStream(file))
        document.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))

    } catch (e: Exception) {
        Toast.makeText(context, "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
