with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

# Just strip the 3 extra braces before @Composable fun SummaryCard
import re

text = re.sub(
    r'\s*\}\s*\}\s*\}\s*@Composable\nfun SummaryCard\(',
    r'\n@Composable\nfun SummaryCard(',
    text
)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
