with open('app/src/main/java/com/example/ui/components/TilesCalculatorDialog.kt', 'r') as f:
    text = f.read()

import re
old_text = r'''                            text = "📐 Area "📐 Tiles & Area Calculator" Material Calculator",'''
new_text = r'''                            text = "📐 Area & Material Calculator",'''

text = re.sub(old_text, new_text, text)

with open('app/src/main/java/com/example/ui/components/TilesCalculatorDialog.kt', 'w') as f:
    f.write(text)
