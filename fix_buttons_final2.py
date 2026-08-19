with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""                                }) {
                                Text("Open Tiles Calculator")
                                }
                        }
                    },""",
"""                        }
                    },"""
)

text = text.replace(
"""                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    }) {""",
"""                                    shape = RoundedCornerShape(12.dp)
                                ) {""")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
