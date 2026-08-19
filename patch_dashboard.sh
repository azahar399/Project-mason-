sed -i '379,385c\
                            Row(\
                                verticalAlignment = Alignment.CenterVertically,\
                                modifier = Modifier.clickable { isSequential = !isSequential }\
                            ) {\
                                Checkbox(checked = isSequential, onCheckedChange = { isSequential = it })\
                                Spacer(Modifier.width(4.dp))\
                                Text("Strict Sequential (Blocks others)", fontSize = 14.sp)\
                            }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i '468,474c\
                            Row(\
                                verticalAlignment = Alignment.CenterVertically,\
                                modifier = Modifier.clickable { isSequential = !isSequential }\
                            ) {\
                                Checkbox(checked = isSequential, onCheckedChange = { isSequential = it })\
                                Spacer(Modifier.width(4.dp))\
                                Text("Strict Sequential (Blocks others)", fontSize = 14.sp)\
                            }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i '544,552c\
                    dismissButton = {\
                        Row {\
                            TextButton(onClick = {\
                                onNavigateToWorkColumn(editingWorkColumn!!.id)\
                                editingWorkColumn = null\
                            }) { Text("View History") }\
                            Spacer(modifier = Modifier.width(8.dp))\
                            TextButton(onClick = {\
                                viewModel.deleteWorkColumn(editingWorkColumn!!)\
                                editingWorkColumn = null\
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }\
                        }\
                    }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i '638,647c\
                    dismissButton = {\
                        Row {\
                            TextButton(onClick = {\
                                onNavigateToFlat(editingFlat!!.id)\
                                editingFlat = null\
                            }) { Text("View History") }\
                            Spacer(modifier = Modifier.width(8.dp))\
                            TextButton(onClick = {\
                                viewModel.deleteFlat(editingFlat!!)\
                                editingFlat = null\
                                triggerAd()\
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }\
                        }\
                    }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i '1041,1044c\
                                                .clickable { editingWorkColumn = col }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt

sed -i '1138,1141c\
                                                    .clickable { editingFlat = flat }' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt
