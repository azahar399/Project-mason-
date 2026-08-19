with open('app/src/main/java/com/example/ui/screens/AddWorkScreen.kt', 'r') as f:
    text = f.read()

import re

# We need to add the new state: var bulkFlatNamesText by remember { mutableStateOf("") }
text = re.sub(
    r'''    var isBulkEntry by remember \{ mutableStateOf\(false\) \}
    var selectedBulkFlatIds by remember \{ mutableStateOf<Set<Int>>\(emptySet\(\)\) \}''',
    r'''    var isBulkEntry by remember { mutableStateOf(false) }
    var selectedBulkFlatIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var bulkFlatNamesText by remember { mutableStateOf("") }''',
    text
)

# Then we need to change `existingBulkEntriesCount` to use parsed `bulkFlatNamesText`
old_bulk_count = r'''    val existingBulkEntriesCount = remember\(selectedBulkFlatIds, selectedWorkColumnId, allEntries\) \{
        if \(selectedBulkFlatIds\.isNotEmpty\(\) && selectedWorkColumnId != null\) \{
            allEntries\.count \{ it\.workEntry\.workColumnId == selectedWorkColumnId && selectedBulkFlatIds\.contains\(it\.workEntry\.flatId\) \}
        \} else 0
    \}'''

new_bulk_count = '''    val existingBulkEntriesCount = remember(bulkFlatNamesText, selectedWorkColumnId, allEntries, flats) {
        val parsedNames = bulkFlatNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parsedNames.isNotEmpty() && selectedWorkColumnId != null) {
            val matchingFlatIds = flats.filter { parsedNames.any { name -> it.name.equals(name, ignoreCase = true) } }.map { it.id }
            allEntries.count { it.workEntry.workColumnId == selectedWorkColumnId && matchingFlatIds.contains(it.workEntry.flatId) }
        } else 0
    }'''

text = re.sub(old_bulk_count, new_bulk_count, text)

# Then we replace the bulk entry UI
old_bulk_ui = r'''                    } else {
                        // Bulk flat selection \(Multiple Flats\)
                        Column\(
                            modifier = Modifier\.fillMaxWidth\(\),
                            verticalArrangement = Arrangement\.spacedBy\(8\.dp\)
                        \) \{
                            Text\(
                                text = "Select Flats",
                                fontWeight = FontWeight\.SemiBold,
                                fontSize = 14\.sp,
                                color = Color\(0xFF1F2937\)
                            \)
                            
                            Card\(
                                modifier = Modifier\.fillMaxWidth\(\)\.heightIn\(max = 240\.dp\),
                                colors = CardDefaults\.cardColors\(containerColor = Color\(0xFFF9FAFB\)\),
                                border = androidx\.compose\.foundation\.BorderStroke\(1\.dp, Color\(0xFFE5E7EB\)\),
                                shape = RoundedCornerShape\(12\.dp\)
                            \) \{
                                Column\(
                                    modifier = Modifier
                                        \.fillMaxWidth\(\)
                                        \.verticalScroll\(rememberScrollState\(\)\)
                                        \.padding\(12\.dp\),
                                    verticalArrangement = Arrangement\.spacedBy\(12\.dp\)
                                \) \{
                                    floors\.forEach \{ floor ->
                                        val floorFlats = flats\.filter \{ it\.floorId == floor\.id \}
                                        if \(floorFlats\.isNotEmpty\(\)\) \{
                                            Column\(
                                                modifier = Modifier\.fillMaxWidth\(\),
                                                verticalArrangement = Arrangement\.spacedBy\(4\.dp\)
                                            \) \{
                                                val floorFlatIds = floorFlats\.map \{ it\.id \}
                                                val isAllSelectedOnFloor = floorFlatIds\.isNotEmpty\(\) && selectedBulkFlatIds\.containsAll\(floorFlatIds\)
                                                
                                                Row\(
                                                    verticalAlignment = Alignment\.CenterVertically,
                                                    modifier = Modifier
                                                        \.fillMaxWidth\(\)
                                                        \.clickable \{
                                                            if \(isAllSelectedOnFloor\) \{
                                                                selectedBulkFlatIds = selectedBulkFlatIds - floorFlatIds\.toSet\(\)
                                                            \} else \{
                                                                selectedBulkFlatIds = selectedBulkFlatIds \+ floorFlatIds\.toSet\(\)
                                                            \}
                                                        \}
                                                        \.padding\(vertical = 4\.dp\)
                                                \) \{
                                                    Checkbox\(
                                                        checked = isAllSelectedOnFloor,
                                                        onCheckedChange = \{ checked ->
                                                            if \(checked == true\) \{
                                                                selectedBulkFlatIds = selectedBulkFlatIds \+ floorFlatIds\.toSet\(\)
                                                            \} else \{
                                                                selectedBulkFlatIds = selectedBulkFlatIds - floorFlatIds\.toSet\(\)
                                                            \}
                                                        \},
                                                        colors = CheckboxDefaults\.colors\(checkedColor = Color\(0xFF1EA34C\)\)
                                                    \)
                                                    Spacer\(modifier = Modifier\.width\(8\.dp\)\)
                                                    Text\(
                                                        text = floor\.name,
                                                        fontWeight = FontWeight\.Bold,
                                                        fontSize = 14\.sp,
                                                        color = Color\(0xFF374151\)
                                                    \)
                                                \}
                                                
                                                floorFlats\.chunked\(2\)\.forEach \{ rowFlats ->
                                                    Row\(
                                                        modifier = Modifier\.fillMaxWidth\(\),
                                                        horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)
                                                    \) \{
                                                        rowFlats\.forEach \{ flat ->
                                                            val isSelected = selectedBulkFlatIds\.contains\(flat\.id\)
                                                            Row\(
                                                                verticalAlignment = Alignment\.CenterVertically,
                                                                modifier = Modifier
                                                                    \.weight\(1f\)
                                                                    \.clickable \{
                                                                        if \(isSelected\) \{
                                                                            selectedBulkFlatIds = selectedBulkFlatIds - flat\.id
                                                                        \} else \{
                                                                            selectedBulkFlatIds = selectedBulkFlatIds \+ flat\.id
                                                                        \}
                                                                    \}
                                                                    \.background\(
                                                                        color = if \(isSelected\) Color\(0xFFF0FDF4\) else Color\.Transparent,
                                                                        shape = RoundedCornerShape\(8\.dp\)
                                                                    \)
                                                                    \.padding\(horizontal = 4\.dp, vertical = 2\.dp\)
                                                            \) \{
                                                                Checkbox\(
                                                                    checked = isSelected,
                                                                    onCheckedChange = \{ checked ->
                                                                        if \(checked == true\) \{
                                                                            selectedBulkFlatIds = selectedBulkFlatIds \+ flat\.id
                                                                        \} else \{
                                                                            selectedBulkFlatIds = selectedBulkFlatIds - flat\.id
                                                                        \}
                                                                    \},
                                                                    colors = CheckboxDefaults\.colors\(checkedColor = Color\(0xFF1EA34C\)\)
                                                                \)
                                                                Spacer\(modifier = Modifier\.width\(4\.dp\)\)
                                                                Text\(
                                                                    text = flat\.name,
                                                                    fontSize = 13\.sp,
                                                                    color = if \(isSelected\) Color\(0xFF166534\) else Color\(0xFF4B5563\)
                                                                \)
                                                            \}
                                                        \}
                                                        if \(rowFlats\.size < 2\) \{
                                                            Spacer\(modifier = Modifier\.weight\(1f\)\)
                                                        \}
                                                    \}
                                                \}
                                            \}
                                            HorizontalDivider\(modifier = Modifier\.padding\(vertical = 4\.dp\), color = Color\(0xFFF3F4F6\)\)
                                        \}
                                    \}
                                \}
                            \}
                            
                            if \(selectedBulkFlatIds\.isNotEmpty\(\)\) \{
                                Text\(
                                    text = "Selected \$\{selectedBulkFlatIds\.size\} flats",
                                    fontSize = 12\.sp,
                                    fontWeight = FontWeight\.Medium,
                                    color = Color\(0xFF166534\)
                                \)
                            \}
                        \}
                    }'''

new_bulk_ui = '''                    } else {
                        // Bulk flat selection (Multiple Flats)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = bulkFlatNamesText,
                                onValueChange = { bulkFlatNamesText = it },
                                label = { Text("Enter Flat Nos (Comma Separated)") },
                                placeholder = { Text("e.g. 212, 213, 315, 516") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedBorderColor = Color(0xFF1EA34C),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            val parsedCount = bulkFlatNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct().size
                            if (parsedCount > 0) {
                                Text(
                                    text = "Entering work for $parsedCount flats",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }'''

text = re.sub(old_bulk_ui, new_bulk_ui, text)

# Then we replace the `isFormValid`
old_form_valid = r'''                    val isFormValid = if \(isBulkEntry\) \{
                        selectedBulkFlatIds\.isNotEmpty\(\) && selectedWorkColumnId != null && \(selectedMasonId != null \|\| isProblem\)
                    \} else \{
                        selectedFlatId != null && selectedWorkColumnId != null && \(selectedMasonId != null \|\| isProblem\)
                    \}'''

new_form_valid = '''                    val parsedBulkNames = bulkFlatNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                    val isFormValid = if (isBulkEntry) {
                        parsedBulkNames.isNotEmpty() && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)
                    } else {
                        selectedFlatId != null && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)
                    }'''

text = re.sub(old_form_valid, new_form_valid, text)

# Then we replace `onSave` logic
old_on_save = r'''                    val onSave = \{
                        if \(isBulkEntry\) \{
                            if \(selectedBulkFlatIds\.isNotEmpty\(\) && selectedWorkColumnId != null && \(selectedMasonId != null \|\| isProblem\)\) \{
                                selectedBulkFlatIds\.forEach \{ flatId ->
                                    val flatExistingEntry = allEntries\.find \{ it\.workEntry\.flatId == flatId && it\.workEntry\.workColumnId == selectedWorkColumnId!! \}
                                    if \(flatExistingEntry != null\) \{
                                        viewModel\.deleteWorkEntry\(flatExistingEntry\.workEntry\)
                                    \}
                                    viewModel\.insertWorkEntry\(
                                        flatId = flatId,
                                        workColumnId = selectedWorkColumnId!!,
                                        masonId = if \(isProblem && selectedMasonId == null\) null else selectedMasonId,
                                        helperId = selectedHelperId,
                                        date = selectedDateMillis,
                                        description = description\.takeIf \{ it\.isNotBlank\(\) \},
                                        isProblem = isProblem
                                    \)
                                \}
                                val activity = context as\? android\.app\.Activity
                                if \(activity != null\) \{
                                    val adManager = \(context\.applicationContext as com\.example\.MasonApplication\)\.adManager
                                    adManager\.incrementActionAndShowInterstitial\(activity\)
                                \}
                                onBack\(\)
                            \}
                        \} else \{
                            if \(selectedFlatId != null && selectedWorkColumnId != null && \(selectedMasonId != null \|\| isProblem\)\) \{
                                if \(existingEntry != null\) \{
                                    viewModel\.deleteWorkEntry\(existingEntry\.workEntry\)
                                \}
                                viewModel\.insertWorkEntry\(
                                    flatId = selectedFlatId!!,
                                    workColumnId = selectedWorkColumnId!!,
                                    masonId = if \(isProblem && selectedMasonId == null\) null else selectedMasonId,
                                    helperId = selectedHelperId,
                                    date = selectedDateMillis,
                                    description = description\.takeIf \{ it\.isNotBlank\(\) \},
                                    isProblem = isProblem
                                \)
                                val activity = context as\? android\.app\.Activity
                                if \(activity != null\) \{
                                    val adManager = \(context\.applicationContext as com\.example\.MasonApplication\)\.adManager
                                    adManager\.incrementActionAndShowInterstitial\(activity\)
                                \}
                                onBack\(\)
                            \}
                        \}
                    \}'''

new_on_save = '''                    val onSave = {
                        if (isBulkEntry) {
                            coroutineScope.launch {
                                val parsedNames = bulkFlatNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                                if (parsedNames.isNotEmpty() && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)) {
                                    parsedNames.forEach { flatName ->
                                        val existingFlat = flats.find { it.name.equals(flatName, ignoreCase = true) }
                                        val flatId = if (existingFlat != null) {
                                            existingFlat.id
                                        } else {
                                            val floorId = floors.firstOrNull()?.id ?: viewModel.insertFloorAndGetId("Floor 1")
                                            viewModel.insertFlatAndGetId(flatName, floorId)
                                        }

                                        val flatExistingEntry = allEntries.find { it.workEntry.flatId == flatId && it.workEntry.workColumnId == selectedWorkColumnId!! }
                                        if (flatExistingEntry != null) {
                                            viewModel.deleteWorkEntry(flatExistingEntry.workEntry)
                                        }
                                        viewModel.insertWorkEntry(
                                            flatId = flatId,
                                            workColumnId = selectedWorkColumnId!!,
                                            masonId = if (isProblem && selectedMasonId == null) null else selectedMasonId,
                                            helperId = selectedHelperId,
                                            date = selectedDateMillis,
                                            description = description.takeIf { it.isNotBlank() },
                                            isProblem = isProblem
                                        )
                                    }
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        val adManager = (context.applicationContext as com.example.MasonApplication).adManager
                                        adManager.incrementActionAndShowInterstitial(activity)
                                    }
                                    onBack()
                                }
                            }
                        } else {
                            if (selectedFlatId != null && selectedWorkColumnId != null && (selectedMasonId != null || isProblem)) {
                                if (existingEntry != null) {
                                    viewModel.deleteWorkEntry(existingEntry.workEntry)
                                }
                                viewModel.insertWorkEntry(
                                    flatId = selectedFlatId!!,
                                    workColumnId = selectedWorkColumnId!!,
                                    masonId = if (isProblem && selectedMasonId == null) null else selectedMasonId,
                                    helperId = selectedHelperId,
                                    date = selectedDateMillis,
                                    description = description.takeIf { it.isNotBlank() },
                                    isProblem = isProblem
                                )
                                val activity = context as? android.app.Activity
                                if (activity != null) {
                                    val adManager = (context.applicationContext as com.example.MasonApplication).adManager
                                    adManager.incrementActionAndShowInterstitial(activity)
                                }
                                onBack()
                            }
                        }
                    }'''

text = re.sub(old_on_save, new_on_save, text)

with open('app/src/main/java/com/example/ui/screens/AddWorkScreen.kt', 'w') as f:
    f.write(text)
