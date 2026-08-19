with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "isSequential = isSequential," in line and "}" in lines[i+1] and "}) {" in lines[i+2]:
        new_lines.append(line)
        # We messed up the replace. We need to restore it properly
        new_lines.append("                                        requiresColumnId = requiresColumnId\n")
        new_lines.append("                                    )\n")
        new_lines.append("                                )\n")
        new_lines.append("                                editingWorkColumn = null\n")
        new_lines.append("                            }\n")
        new_lines.append("                        }) {\n")
        new_lines.append("                            Text(\"Save\")\n")
        new_lines.append("                        }\n")
        new_lines.append("                    },\n")
        skip = True
        continue
    if skip and "dismissButton = {" in line:
        skip = False
    if not skip:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
