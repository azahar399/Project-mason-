package com.example.utils

import com.example.data.Flat
import com.example.data.WorkColumn
import com.example.data.WorkEntryWithDetails
import java.util.Locale

object ExcelFormulaParser {

    fun getColumnLetter(index: Int): String {
        var temp = index
        val sb = StringBuilder()
        while (temp >= 0) {
            sb.append(('A'.code + (temp % 26)).toChar())
            temp = (temp / 26) - 1
        }
        return sb.reverse().toString()
    }

    fun getColumnIndex(letter: String): Int {
        var result = 0
        val upperLetter = letter.uppercase(Locale.getDefault())
        for (i in 0 until upperLetter.length) {
            result = result * 26 + (upperLetter[i] - 'A' + 1)
        }
        return result - 1
    }

    fun evaluateFormula(
        formulaStr: String,
        flats: List<Flat>,
        workColumns: List<WorkColumn>,
        allEntries: List<WorkEntryWithDetails>,
        pendingWorkColIdsMap: Map<Int, List<Int>> // flatId -> list of pending workColumnIds
    ): String {
        var f = formulaStr.trim()
        if (!f.startsWith("=")) {
            return "Formula must start with ="
        }
        f = f.substring(1).uppercase(Locale.getDefault()).trim()

        try {
            // Parse function name and arguments
            val openParen = f.indexOf('(')
            val closeParen = f.lastIndexOf(')')
            if (openParen == -1 || closeParen == -1 || closeParen < openParen) {
                return "Error: Invalid formula format. Use FUNCTION(ARG)"
            }

            val functionName = f.substring(0, openParen).trim()
            val argsStr = f.substring(openParen + 1, closeParen).trim()

            // Support functions
            return when (functionName) {
                "SUM" -> {
                    val cells = resolveRange(argsStr, flats, workColumns, allEntries, pendingWorkColIdsMap)
                    val sum = cells.sumOf { it.numericValue }
                    if (sum == sum.toInt().toDouble()) sum.toInt().toString() else String.format(Locale.US, "%.2f", sum)
                }
                "AVERAGE" -> {
                    val cells = resolveRange(argsStr, flats, workColumns, allEntries, pendingWorkColIdsMap)
                    if (cells.isEmpty()) "0"
                    else {
                        val avg = cells.sumOf { it.numericValue } / cells.size
                        if (avg == avg.toInt().toDouble()) avg.toInt().toString() else String.format(Locale.US, "%.2f", avg)
                    }
                }
                "COUNTA" -> {
                    val cells = resolveRange(argsStr, flats, workColumns, allEntries, pendingWorkColIdsMap)
                    val count = cells.count { it.textValue.isNotEmpty() }
                    count.toString()
                }
                "COUNT" -> {
                    val cells = resolveRange(argsStr, flats, workColumns, allEntries, pendingWorkColIdsMap)
                    val count = cells.count { it.numericValue > 0 || it.textValue == "DONE" }
                    count.toString()
                }
                "COUNTIF" -> {
                    val parts = splitArgs(argsStr)
                    if (parts.size != 2) {
                        return "Error: COUNTIF requires range and criteria. e.g. COUNTIF(B1:B5, \"DONE\")"
                    }
                    val rangeStr = parts[0].trim()
                    var criteria = parts[1].trim()
                    if (criteria.startsWith("\"") && criteria.endsWith("\"")) {
                        criteria = criteria.substring(1, criteria.length - 1)
                    }
                    val cells = resolveRange(rangeStr, flats, workColumns, allEntries, pendingWorkColIdsMap)
                    val count = cells.count { it.textValue.equals(criteria, ignoreCase = true) }
                    count.toString()
                }
                "CONCAT" -> {
                    val parts = splitArgs(argsStr)
                    val sb = StringBuilder()
                    for (part in parts) {
                        var p = part.trim()
                        if (p.startsWith("\"") && p.endsWith("\"")) {
                            sb.append(p.substring(1, p.length - 1))
                        } else {
                            // Resolve single cell
                            val cells = resolveRange(p, flats, workColumns, allEntries, pendingWorkColIdsMap)
                            if (cells.isNotEmpty()) {
                                sb.append(cells[0].textValue)
                            }
                        }
                    }
                    sb.toString()
                }
                else -> "Unknown Function: $functionName"
            }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun splitArgs(argsStr: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (char in argsStr) {
            if (char == '"') {
                inQuotes = !inQuotes
                current.append(char)
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString())
                current = StringBuilder()
            } else {
                current.append(char)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }

    data class CellValue(
        val textValue: String,
        val numericValue: Double
    )

    private fun resolveRange(
        rangeStr: String,
        flats: List<Flat>,
        workColumns: List<WorkColumn>,
        allEntries: List<WorkEntryWithDetails>,
        pendingWorkColIdsMap: Map<Int, List<Int>>
    ): List<CellValue> {
        val result = mutableListOf<CellValue>()
        val sortedCols = workColumns.sortedBy { it.displayOrder }

        if (rangeStr.contains(":")) {
            // Range like A1:B5
            val parts = rangeStr.split(":")
            if (parts.size != 2) return emptyList()
            val startCell = parseCell(parts[0]) ?: return emptyList()
            val endCell = parseCell(parts[1]) ?: return emptyList()

            val startColIdx = getColumnIndex(startCell.first)
            val endColIdx = getColumnIndex(endCell.first)
            val startRowIdx = startCell.second - 1
            val endRowIdx = endCell.second - 1

            val minCol = minOf(startColIdx, endColIdx)
            val maxCol = maxOf(startColIdx, endColIdx)
            val minRow = minOf(startRowIdx, endRowIdx)
            val maxRow = maxOf(startRowIdx, endRowIdx)

            for (colIdx in minCol..maxCol) {
                for (rowIdx in minRow..maxRow) {
                    if (rowIdx in flats.indices) {
                        val flat = flats[rowIdx]
                        val value = getCellValue(flat, colIdx, sortedCols, allEntries, pendingWorkColIdsMap)
                        result.add(value)
                    }
                }
            }
        } else {
            // Single cell like A1
            val cell = parseCell(rangeStr)
            if (cell != null) {
                val colIdx = getColumnIndex(cell.first)
                val rowIdx = cell.second - 1
                if (rowIdx in flats.indices) {
                    val flat = flats[rowIdx]
                    val value = getCellValue(flat, colIdx, sortedCols, allEntries, pendingWorkColIdsMap)
                    result.add(value)
                }
            }
        }
        return result
    }

    private fun parseCell(cellStr: String): Pair<String, Int>? {
        val s = cellStr.trim().uppercase(Locale.getDefault())
        val letterPart = s.filter { it.isLetter() }
        val digitPart = s.filter { it.isDigit() }
        if (letterPart.isEmpty() || digitPart.isEmpty()) return null
        val rowNum = digitPart.toIntOrNull() ?: return null
        return Pair(letterPart, rowNum)
    }

    private fun getCellValue(
        flat: Flat,
        colIdx: Int,
        sortedCols: List<WorkColumn>,
        allEntries: List<WorkEntryWithDetails>,
        pendingWorkColIdsMap: Map<Int, List<Int>>
    ): CellValue {
        if (colIdx == 0) {
            // Column A is FLAT (Name and SqFt)
            return CellValue(
                textValue = flat.name,
                numericValue = flat.sqFt
            )
        } else {
            // Columns B, C, D... are Work Columns
            val colListIdx = colIdx - 1
            if (colListIdx in sortedCols.indices) {
                val col = sortedCols[colListIdx]
                val entry = allEntries.find { it.workEntry.flatId == flat.id && it.workEntry.workColumnId == col.id }
                val isProblem = entry?.workEntry?.isProblem == true
                val isDone = entry != null && !isProblem
                val pendingIds = pendingWorkColIdsMap[flat.id] ?: emptyList()
                val isPending = pendingIds.contains(col.id)

                val text = if (isProblem) "PROB"
                           else if (isDone) "DONE"
                           else if (isPending) "PEND"
                           else ""

                val num = if (isDone) 1.0 else 0.0
                return CellValue(text, num)
            }
        }
        return CellValue("", 0.0)
    }
}
