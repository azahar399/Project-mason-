with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "}) {" in line and 'Text("Open Tiles/Area Calculator")' in lines[i+1]:
        # skip this line
        continue
    if "Text(\"Open Tiles/Area Calculator\")" in line:
        new_lines.append(line)
        continue
    
    if "confirmButton = {" in line and "Button(onClick = {" in lines[i+1]:
        if "if (newFlatName.isNotBlank()) {" in lines[i+2]:
            new_lines.append(line)
            new_lines.append(lines[i+1])
            new_lines.append(lines[i+2])
            new_lines.append(lines[i+3])
            new_lines.append(lines[i+4])
            new_lines.append(lines[i+5])
            new_lines.append(lines[i+6])
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

