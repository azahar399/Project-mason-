cat << 'INNER_EOF' > tmp_report_clicks.txt
                    onClick = {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val fileName = "WorkReport_${sdf.format(Date())}.pdf"
                        if (activity != null) {
                            adManager.showRewardedAd(activity) {
                                createDocumentLauncher.launch(fileName)
                            }
                        } else {
                            createDocumentLauncher.launch(fileName)
                        }
                    },
INNER_EOF

sed -i '/onClick = {/,/createDocumentLauncher.launch(fileName)/c\
                    onClick = {\
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())\
                        val fileName = "WorkReport_${sdf.format(Date())}.pdf"\
                        if (activity != null) {\
                            adManager.showRewardedAd(activity) {\
                                createDocumentLauncher.launch(fileName)\
                            }\
                        } else {\
                            createDocumentLauncher.launch(fileName)\
                        }\
                    },' app/src/main/java/com/example/ui/screens/ReportScreen.kt

cat << 'INNER_EOF' > tmp_share_clicks.txt
                    onClick = {
                        val action = {
                            coroutineScope.launch {
                                val filteredEntries = filterEntries(allEntries, selectedFlatId, selectedPersonId, selectedWorkColumnId, selectedStatus, selectedDateMillis)
                                sharePdf(context, filteredEntries, flats, persons, workColumns)
                            }
                        }
                        if (activity != null) {
                            adManager.showRewardedAd(activity) {
                                action()
                            }
                        } else {
                            action()
                        }
                    },
INNER_EOF

sed -i '/onClick = {/,/sharePdf(context, filteredEntries, flats, persons, workColumns)/c\
                    onClick = {\
                        val action = {\
                            coroutineScope.launch {\
                                val filteredEntries = filterEntries(allEntries, selectedFlatId, selectedPersonId, selectedWorkColumnId, selectedStatus, selectedDateMillis)\
                                sharePdf(context, filteredEntries, flats, persons, workColumns)\
                            }\
                        }\
                        if (activity != null) {\
                            adManager.showRewardedAd(activity) {\
                                action()\
                            }\
                        } else {\
                            action()\
                        }\
                    },' app/src/main/java/com/example/ui/screens/ReportScreen.kt
