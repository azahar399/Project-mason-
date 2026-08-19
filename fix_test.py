with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

scope = 0
in_func = False
for i, line in enumerate(lines):
    if "fun DashboardScreen" in line:
        in_func = True
        
    if in_func:
        scope += line.count('{') - line.count('}')
        if scope <= 0 and i > 68:
            print(f"Scope ends at {i+1}: scope={scope} : {line.strip()}")

