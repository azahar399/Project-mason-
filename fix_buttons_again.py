with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    }) {
                                    Text("Delete Entry")
                                    }
                            } else {""",
"""                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Delete Entry")
                                }
                            } else {""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
