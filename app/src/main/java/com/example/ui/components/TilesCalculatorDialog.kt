package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlin.math.ceil

data class TileSegment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val length: String = "",
    val width: String = "",
    val unit: String = "Feet" // "Feet", "Meters", "Inches", "mm"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TilesCalculatorDialog(
    onDismissRequest: () -> Unit,
    onApplyArea: ((Double) -> Unit)? = null,
    onApplyDescription: ((String) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current

    // Tile Dimensions state
    var tileLength by remember { mutableStateOf("2") }
    var tileWidth by remember { mutableStateOf("2") }
    var tileUnit by remember { mutableStateOf("Feet") } // "Feet", "Meters", "Inches", "mm"
    var tileUnitExpanded by remember { mutableStateOf(false) }
    var unitsPerBox by remember { mutableStateOf("") }

    // List of segments
    var segments by remember {
        mutableStateOf(
            listOf(
                TileSegment(name = "Room 1", length = "12", width = "10", unit = "Feet")
            )
        )
    }

    // Wastage Percentage
    var wastagePercent by remember { mutableStateOf(10) } // 0, 5, 10, 15, 20

    // Available units
    val units = listOf("Feet", "Meters", "Inches", "mm")

    // Calculations helper
    val calculations = remember(tileLength, tileWidth, tileUnit, segments, wastagePercent, unitsPerBox) {
        val tL = tileLength.toDoubleOrNull() ?: 0.0
        val tW = tileWidth.toDoubleOrNull() ?: 0.0
        
        // Single Tile Area in Sq Ft
        val singleTileSqFt = when (tileUnit) {
            "Feet" -> tL * tW
            "Meters" -> (tL * 3.28084) * (tW * 3.28084)
            "Inches" -> (tL * tW) / 144.0
            "mm" -> (tL * tW) / 92903.04
            else -> tL * tW
        }

        // Segments total Sq Ft
        var totalSqFt = 0.0
        val segmentDetails = mutableListOf<String>()

        segments.forEach { segment ->
            val sL = segment.length.toDoubleOrNull() ?: 0.0
            val sW = segment.width.toDoubleOrNull() ?: 0.0
            
            val segmentSqFt = when (segment.unit) {
                "Feet" -> sL * sW
                "Meters" -> (sL * 3.28084) * (sW * 3.28084)
                "Inches" -> (sL * sW) / 144.0
                "mm" -> (sL * sW) / 92903.04
                else -> sL * sW
            }
            
            totalSqFt += segmentSqFt
            if (sL > 0 && sW > 0) {
                val nameLabel = if (segment.name.isNotBlank()) "${segment.name}: " else ""
                segmentDetails.add("$nameLabel${segment.length}x${segment.width} ${segment.unit} (${segmentSqFt.toInt()} Sq Ft)")
            }
        }

        val totalSqMeter = totalSqFt / 10.7639
        val singleTileSqMeter = singleTileSqFt / 10.7639

        val netTiles = if (singleTileSqFt > 0) ceil(totalSqFt / singleTileSqFt).toInt() else 0
        val wastageMultiplier = 1.0 + (wastagePercent / 100.0)
        val grossTiles = if (singleTileSqFt > 0) ceil((totalSqFt * wastageMultiplier) / singleTileSqFt).toInt() else 0

        val unitsPerBoxVal = unitsPerBox.toIntOrNull() ?: 0
        val netBoxes = if (unitsPerBoxVal > 0) ceil(netTiles.toDouble() / unitsPerBoxVal).toInt() else 0
        val grossBoxes = if (unitsPerBoxVal > 0) ceil(grossTiles.toDouble() / unitsPerBoxVal).toInt() else 0

        object {
            val totalAreaSqFt = totalSqFt
            val totalAreaSqMeter = totalSqMeter
            val tileAreaSqFt = singleTileSqFt
            val tileAreaSqMeter = singleTileSqMeter
            val netTilesRequired = netTiles
            val grossTilesRequired = grossTiles
            val netBoxesRequired = netBoxes
            val grossBoxesRequired = grossBoxes
            val detailsList = segmentDetails
            val unitsPerBox = unitsPerBoxVal
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📐 Area & Material Calculator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Calculate total area and number of units needed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close Calculator", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // CARD 1: Single Tile Size
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "1. Material Dimensions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = tileLength,
                                    onValueChange = { tileLength = it },
                                    label = { Text("Length") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = tileWidth,
                                    onValueChange = { tileWidth = it },
                                    label = { Text("Width") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Unit Selector Dropdown
                                Box(modifier = Modifier.weight(1.2f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = tileUnitExpanded,
                                        onExpandedChange = { tileUnitExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = tileUnit,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Unit") },
                                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, "Unit Dropdown") },
                                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                                            colors = OutlinedTextFieldDefaults.colors()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = tileUnitExpanded,
                                            onDismissRequest = { tileUnitExpanded = false }
                                        ) {
                                            units.forEach { unit ->
                                                DropdownMenuItem(
                                                    text = { Text(unit) },
                                                    onClick = {
                                                        tileUnit = unit
                                                        tileUnitExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = unitsPerBox,
                                    onValueChange = { unitsPerBox = it },
                                    label = { Text("Units per Box (Optional)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // CARD 2: Work Area / Sections
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "2. Work Area (Multiple Sections)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Text(
                                    "${segments.size} Section(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate500
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            segments.forEachIndexed { index, segment ->
                                var unitExpanded by remember { mutableStateOf(false) }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(Slate50, RoundedCornerShape(12.dp))
                                        .border(1.dp, Slate100, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Section #${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800
                                            )
                                            if (segments.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        segments = segments.filter { it.id != segment.id }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete section",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Section Name input
                                        OutlinedTextField(
                                            value = segment.name,
                                            onValueChange = { newVal ->
                                                segments = segments.map { 
                                                    if (it.id == segment.id) it.copy(name = newVal) else it
                                                }
                                            },
                                            placeholder = { Text("Section Name (e.g., Living Room)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodySmall,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = segment.length,
                                                onValueChange = { newVal ->
                                                    segments = segments.map {
                                                        if (it.id == segment.id) it.copy(length = newVal) else it
                                                    }
                                                },
                                                label = { Text("Length", fontSize = 11.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            OutlinedTextField(
                                                value = segment.width,
                                                onValueChange = { newVal ->
                                                    segments = segments.map {
                                                        if (it.id == segment.id) it.copy(width = newVal) else it
                                                    }
                                                },
                                                label = { Text("Width", fontSize = 11.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            
                                            // Segment Unit Selector
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                ExposedDropdownMenuBox(
                                                    expanded = unitExpanded,
                                                    onExpandedChange = { unitExpanded = it }
                                                ) {
                                                    OutlinedTextField(
                                                        value = segment.unit,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text("Unit", fontSize = 11.sp) },
                                                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, "Unit Dropdown") },
                                                        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedContainerColor = Color.White,
                                                            unfocusedContainerColor = Color.White
                                                        )
                                                    )
                                                    ExposedDropdownMenu(
                                                        expanded = unitExpanded,
                                                        onDismissRequest = { unitExpanded = false }
                                                    ) {
                                                        units.forEach { unit ->
                                                            DropdownMenuItem(
                                                                text = { Text(unit) },
                                                                onClick = {
                                                                    segments = segments.map {
                                                                        if (it.id == segment.id) it.copy(unit = unit) else it
                                                                    }
                                                                    unitExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FilledTonalButton(
                                onClick = {
                                    val nextNum = segments.size + 1
                                    segments = segments + TileSegment(
                                        name = "Room $nextNum",
                                        unit = segments.lastOrNull()?.unit ?: "Feet"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Section")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Area Section")
                            }
                        }
                    }

                    // CARD 3: Wastage & Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "3. Wastage Buffer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Recommended 10% to cover cutting, breakage, and future repairs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val wastageOptions = listOf(0, 5, 10, 15, 20)
                                wastageOptions.forEach { pct ->
                                    FilterChip(
                                        selected = wastagePercent == pct,
                                        onClick = { wastagePercent = pct },
                                        label = { Text("$pct%") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // CARD 4: Live Results
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Green50),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD1FAE5)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "📊 Calculations Dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Total Area
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TOTAL WORK AREA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    Text(
                                        text = "${"%.1f".format(calculations.totalAreaSqFt)} Sq Ft",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF064E3B)
                                    )
                                    Text(
                                        text = "${"%.1f".format(calculations.totalAreaSqMeter)} Sq Meter",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }

                                // Single Tile size
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SINGLE UNIT SIZE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    Text(
                                        text = "${"%.2f".format(calculations.tileAreaSqFt)} Sq Ft",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF064E3B)
                                    )
                                    Text(
                                        text = "${"%.2f".format(calculations.tileAreaSqMeter)} Sq Meter",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFD1FAE5))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Net Required
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("NET UNITS REQUIRED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    Text(
                                        text = "${calculations.netTilesRequired} Pieces",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF064E3B)
                                    )
                                    if (calculations.unitsPerBox > 0) {
                                        Text("${calculations.netBoxesRequired} Boxes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF064E3B))
                                    }
                                    Text("Exact count, no buffer", fontSize = 11.sp, color = Slate500)
                                }

                                // Gross with Wastage
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GROSS (WITH $wastagePercent% WASTE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    Text(
                                        text = "${calculations.grossTilesRequired} Pieces",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF92400E)
                                    )
                                    if (calculations.unitsPerBox > 0) {
                                        Text("${calculations.grossBoxesRequired} Boxes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    }
                                    Text("With recommended buffer", fontSize = 11.sp, color = Color(0xFF92400E))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action buttons
                val calculatedTextSummary = remember(calculations, wastagePercent, tileLength, tileWidth, tileUnit) {
                    val base = StringBuilder()
                    base.append("=== Material & Area Calculation ===\n")
                    base.append("Total Area: ${"%.1f".format(calculations.totalAreaSqFt)} Sq Ft (${"%.1f".format(calculations.totalAreaSqMeter)} Sq M)\n")
                    base.append("Unit Size: $tileLength x $tileWidth $tileUnit (Area: ${"%.2f".format(calculations.tileAreaSqFt)} Sq Ft)\n")
                    base.append("Net Units: ${calculations.netTilesRequired} pcs")
                    if (calculations.unitsPerBox > 0) base.append(" (${calculations.netBoxesRequired} boxes)")
                    base.append("\n")
                    base.append("Gross Units (+${wastagePercent}% waste): ${calculations.grossTilesRequired} pcs")
                    if (calculations.unitsPerBox > 0) base.append(" (${calculations.grossBoxesRequired} boxes)")
                    base.append("\n")
                    if (calculations.detailsList.isNotEmpty()) {
                        base.append("Sections:\n")
                        calculations.detailsList.forEach { base.append("- $it\n") }
                    }
                    base.toString()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(calculatedTextSummary))
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Copy Summary", fontSize = 12.sp)
                        }

                        if (onApplyArea != null) {
                            Button(
                                onClick = {
                                    onApplyArea(calculations.totalAreaSqFt)
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Apply Area", fontSize = 12.sp, maxLines = 1)
                            }
                        }

                        if (onApplyDescription != null) {
                            Button(
                                onClick = {
                                    val formattedDesc = "Calculated Units: ${calculations.grossTilesRequired} pcs (${tileLength}x${tileWidth} ${tileUnit} tiles for ${"%.1f".format(calculations.totalAreaSqFt)} Sq Ft, with ${wastagePercent}% waste)."
                                    onApplyDescription(formattedDesc)
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1.8f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
                            ) {
                                Text("Apply to Entry", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }

                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Slate500)
                    }
                }
            }
        }
    }
}
