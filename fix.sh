sed -i -e '638,647c\
                                val subtitle = if (isFlatWork) {\
                                    val e = entry as com.example.data.WorkEntryWithDetails\
                                    val fName = floors.find { it.id == e.flat.floorId }?.name ?: "Unknown"\
                                    "${fName} Flat ${e.flat.name}"\
                                } else {\
                                    val e = entry as com.example.data.OtherWorkEntryWithDetails\
                                    "General"\
                                }' app/src/main/java/com/example/ui/screens/AddWorkScreen.kt
