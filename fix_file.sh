#!/bin/bash
sed -i '1041d' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt
sed -i '1048,1051c\
                                                .clickable { editingWorkColumn = col }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt
