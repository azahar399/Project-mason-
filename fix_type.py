with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "val allDisplayedFlats = if (selectedFloorId == null) flats else flats.filter { it.floorId == selectedFloorId }" in line:
        new_lines.append("            val allDisplayedFlats: List<com.example.data.Flat> = if (selectedFloorId == null) flats else flats.filter { it.floorId == selectedFloorId }\n")
    elif "val entriesForFlat = allEntries.filter { it.workEntry.flatId == flat.id }" in line:
        new_lines.append("                        val entriesForFlat: List<com.example.data.WorkEntryWithDetails> = allEntries.filter { it.workEntry.flatId == flat.id }\n")
    elif "val entryMatches = entriesForFlat.any { entry ->" in line:
        new_lines.append("                        val entryMatches = entriesForFlat.any { entry: com.example.data.WorkEntryWithDetails ->\n")
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
