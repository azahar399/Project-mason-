sed -i 's/val totalDays: Int/val totalDays: Double/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/data class PersonLedgerStats(/a\    val totalReward: Double,\n    val totalPenalty: Double,' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i 's/data class LedgerLogItem(.*/data class LedgerLogItem(val date: Long, val unit: Double, val income: Double, val advance: Double, val reward: Double, val penalty: Double)/g' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val type: String/d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val date: Long,/d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val title: String/d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val notes: String/d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val advanceObj/d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

