with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

scope = 0
for i, char in enumerate(text):
    if char == '{':
        scope += 1
    elif char == '}':
        scope -= 1

print(f"Final scope: {scope}")
