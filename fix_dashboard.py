import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Fix Button(onClick = { ... } Text("Add") }
content = re.sub(
    r'(\s+)Text\("([^"]+)"\)\n\s*\}\n',
    r'\1}) {\n\1Text("\2")\n\1}\n',
    content
)

# Wait, Text("Cancel") is in dismissButton = { Text("Cancel") }
content = re.sub(
    r'dismissButton = \{\n\s+Text\("([^"]+)"\)\n\s*\}\n',
    r'dismissButton = {\nTextButton(onClick = { /* TODO dismiss */ }) {\nText("\1")\n}\n}\n',
    content
)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
