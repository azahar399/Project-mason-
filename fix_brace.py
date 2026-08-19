with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re

text = re.sub(
r'''                            \}
                        \}
                        \}
                        if \(selectedFloorId != null\) \{''',
'''                            }
                        }
                        if (selectedFloorId != null) {''', text)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
