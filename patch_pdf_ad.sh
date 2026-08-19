sed -i '/fun shareMasterLedgerPdf/,/^}/c\
fun shareMasterLedgerPdf(context: Context, persons: List<Person>, allDailyWageEntries: List<DailyWageEntry>) {\
    val activity = context as? Activity\
    if (activity != null) {\
        val adManager = (context.applicationContext as com.example.MasonApplication).adManager\
        adManager.showRewardedAd(activity) {\
            Toast.makeText(context, "PDF Export is available via CSV. For full PDF, feature is currently simplified.", Toast.LENGTH_SHORT).show()\
        }\
    } else {\
        Toast.makeText(context, "PDF Export is available via CSV. For full PDF, feature is currently simplified.", Toast.LENGTH_SHORT).show()\
    }\
}' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt

sed -i '/fun sharePersonLedgerPdf/,/^}/c\
fun sharePersonLedgerPdf(context: Context, person: Person, stats: PersonLedgerStats, logs: List<LedgerLogItem>) {\
    val activity = context as? Activity\
    if (activity != null) {\
        val adManager = (context.applicationContext as com.example.MasonApplication).adManager\
        adManager.showRewardedAd(activity) {\
            Toast.makeText(context, "PDF Export for ${person.name} is disabled temporarily.", Toast.LENGTH_SHORT).show()\
        }\
    } else {\
        Toast.makeText(context, "PDF Export for ${person.name} is disabled temporarily.", Toast.LENGTH_SHORT).show()\
    }\
}' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
