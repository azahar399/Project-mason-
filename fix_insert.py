import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'r') as f:
    text = f.read()

old_logic = """                                val entry = com.example.data.WorkEntry(
                                    flatId = matchedFlat.id,
                                    workColumnId = matchedCol.id,
                                    masonId = matchedMason?.id,
                                    helperId = null,
                                    date = System.currentTimeMillis()
                                )
                                viewModel.insertWorkEntry(entry)"""

new_logic = """                                viewModel.insertWorkEntry(
                                    flatId = matchedFlat.id,
                                    workColumnId = matchedCol.id,
                                    masonId = matchedMason?.id,
                                    helperId = null,
                                    date = System.currentTimeMillis()
                                )"""

text = text.replace(old_logic, new_logic)

with open('/app/applet/app/src/main/java/com/example/ui/screens/ChatbotScreen.kt', 'w') as f:
    f.write(text)
