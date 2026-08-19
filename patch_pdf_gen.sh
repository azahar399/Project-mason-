cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

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
INNER_EOF

sed -i 's/Toast.makeText(context, "PDF Export is available via CSV. For full PDF, feature is currently simplified.", Toast.LENGTH_SHORT).show()/generateMasterPdf(context, persons, allDailyWageEntries)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/Toast.makeText(context, "PDF Export for ${person.name} is disabled temporarily.", Toast.LENGTH_SHORT).show()/generatePersonPdf(context, person, stats, logs)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

