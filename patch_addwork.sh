sed -i 's/val coroutineScope = rememberCoroutineScope()//g' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt
sed -i '/var showDatePicker by remember { mutableStateOf(false) }/a \
    val coroutineScope = rememberCoroutineScope()' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt

sed -i 's/val colName = entry.workColumnName/val colName = entry.workColumn.name/g' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt
sed -i 's/(entry as com.example.data.WorkEntryWithDetails).workColumnName/(entry as com.example.data.WorkEntryWithDetails).workColumn.name/g' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt
sed -i 's/val title = if (isFlatWork) (entry as com.example.data.WorkEntryWithDetails).workColumn.name/val title = if (isFlatWork) (entry as com.example.data.WorkEntryWithDetails).workColumn.name/g' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt

