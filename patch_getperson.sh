sed -i 's/val totalDays = personTx.sumOf { it.unit }//g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i 's/val totalEarned = totalDays \* person.dailyRate//g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i '/val personTx = allDailyWageEntries.filter { it.personId == person.id }/a\        val totalDays = personTx.sumOf { it.unit }\n        val totalEarned = totalDays * person.dailyRate' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

