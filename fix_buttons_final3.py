with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re

# More robust find/replace for rogue empty button blocks
text = re.sub(r'\}\) \{\s*\}\) \{', r'}) {', text)
text = re.sub(r'\}\) \{\s*\n\s*Text\("([^"]+)"\)\s*\n\s*\}', r'Text("\1")', text)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
