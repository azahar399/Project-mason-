sed -i '/val regularWorks = /d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val otherWorks = /d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
sed -i '/val personAdvances = /d' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

cat << 'INNER_EOF' > replacement.txt
        val combinedLogs = remember(personTx) {
            val list = mutableListOf<LedgerLogItem>()
            personTx.forEach {
                list.add(LedgerLogItem(it.date, it.unit, it.unit * person.dailyRate, it.advance, it.reward, it.penalty))
            }
            list.sortedByDescending { it.date }
        }
INNER_EOF

# we will replace the block from "val combinedLogs =" to "list.sortedByDescending { it.date }\n        }"
sed -i '/val combinedLogs = remember(/,/list.sortedByDescending { it.date }\n        }/c\        val combinedLogs = remember(personTx) {\n            val list = mutableListOf<LedgerLogItem>()\n            personTx.forEach {\n                list.add(LedgerLogItem(it.date, it.unit, it.unit * person.dailyRate, it.advance, it.reward, it.penalty))\n            }\n            list.sortedByDescending { it.date }\n        }' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

