import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'r') as f:
    text = f.read()

# Replace variables
text = text.replace("val workColumns by viewModel.allWorkColumns.collectAsState(initial = emptyList())", "val workColumns by viewModel.allWorkColumns.collectAsState(initial = emptyList())\n    val masons by viewModel.allMasons.collectAsState(initial = emptyList())")

# Replace parsing logic
old_logic = """                            val matchedFlat = flats.find { lowerText.contains(it.name.lowercase()) }
                            val matchedCol = workColumns.find { lowerText.contains(it.name.lowercase()) }

                            if (matchedFlat != null && matchedCol != null) {
                                // Insert work entry
                                val entry = com.example.data.WorkEntry(
                                    flatId = matchedFlat.id,
                                    workColumnId = matchedCol.id,
                                    masonId = null,
                                    helperId = null,
                                    date = System.currentTimeMillis()
                                )
                                viewModel.insertWorkEntry(entry)
                                chatHistory = chatHistory + ChatMessage("✅ Saved! Marked ${matchedCol.name} as done for Flat ${matchedFlat.name}.", false)
                            } else {"""

new_logic = """                            val matchedFlat = flats.find { lowerText.contains(it.name.lowercase()) }
                            val matchedCol = workColumns.find { lowerText.contains(it.name.lowercase()) }
                            val matchedMason = masons.find { lowerText.contains(it.name.lowercase()) }

                            if (matchedFlat != null && matchedCol != null) {
                                // Insert work entry
                                val entry = com.example.data.WorkEntry(
                                    flatId = matchedFlat.id,
                                    workColumnId = matchedCol.id,
                                    masonId = matchedMason?.id,
                                    helperId = null,
                                    date = System.currentTimeMillis()
                                )
                                viewModel.insertWorkEntry(entry)
                                val masonStr = if (matchedMason != null) " by ${matchedMason.name}" else ""
                                chatHistory = chatHistory + ChatMessage("✅ Saved! Marked ${matchedCol.name} as done for Flat ${matchedFlat.name}$masonStr.", false)
                            } else {"""

text = text.replace(old_logic, new_logic)

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'w') as f:
    f.write(text)

