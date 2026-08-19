with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

scope = 0
for i, line in enumerate(lines):
    if "fun DashboardScreen" in line:
        print(f"Function starts at {i+1}")
        scope = 0
    scope += line.count('{') - line.count('}')
    if scope <= 0 and i > 60:
        print(f"Scope ends at {i+1} with {line.strip()}")

