with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "}) {" in line and 'Text("Log Entry")' in lines[i+1]:
        # remove this and the next 2 lines
        continue
    if 'Text("Log Entry")' in line and "}) {" in lines[i-1]:
        continue
    if "}" in line and 'Text("Log Entry")' in lines[i-2]:
        continue
    
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
