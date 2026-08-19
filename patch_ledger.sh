sed -i 's/val allLedgerTransactions by viewModel.allLedgerTransactions.collectAsState()/val allDailyWageEntries by viewModel.allDailyWageEntries.collectAsState()/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/val personTx = allLedgerTransactions.filter { it.personId == person.id }/val personTx = allDailyWageEntries.filter { it.personId == person.id }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/val totalAdvance = personTx.filter { it.type == "advance" }.sumOf { it.amount }/val totalAdvance = personTx.sumOf { it.advance }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/val totalReward = personTx.filter { it.type == "reward" }.sumOf { it.amount }/val totalReward = personTx.sumOf { it.reward }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/val totalPenalty = personTx.filter { it.type == "penalty" }.sumOf { it.amount }/val totalPenalty = personTx.sumOf { it.penalty }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/val totalDays = regularCount + otherCount/val totalDays = personTx.sumOf { it.unit }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/totalDays = totalDays,/totalDays = totalDays,/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/val totalAdvanceAll = remember(allLedgerTransactions) {/val totalAdvanceAll = remember(allDailyWageEntries) {/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/allLedgerTransactions.sumOf { it.amount }/allDailyWageEntries.sumOf { it.advance }/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/shareMasterLedgerPdf(context, persons, allWorkEntries, allOtherWorkEntries, allLedgerTransactions)/shareMasterLedgerPdf(context, persons, allDailyWageEntries)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/shareMasterLedgerCsv(context, persons, allWorkEntries, allOtherWorkEntries, allLedgerTransactions)/shareMasterLedgerCsv(context, persons, allDailyWageEntries)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/sharePersonLedgerPdf(context, person, stats, combinedLogs)/sharePersonLedgerPdf(context, person, stats, combinedLogs)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

