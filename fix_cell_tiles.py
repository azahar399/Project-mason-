with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "}) {" in line and 'Text("Open Tiles Calculator")' in lines[i+1]:
        # skip this line
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
