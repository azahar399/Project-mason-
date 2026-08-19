#!/bin/bash
sed -i '1040,1055c\
                                        val colBgColor = colStyle.bgColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Transparent\
                                        val colTextColor = colStyle.textColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White\
                                        val colFontWeight = if (colStyle.isBold) FontWeight.ExtraBold else FontWeight.Bold\
                                        val colFontStyle = if (colStyle.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal\
                                        Box(\
                                            modifier = Modifier\
                                                .width(100.dp)\
                                                .height(56.dp)\
                                                .background(colBgColor)\
                                                .padding(vertical = 4.dp, horizontal = 8.dp)\
                                                .clickable { editingWorkColumn = col }\
                                                .drawBehind {\
                                                    drawLine(\
                                                        color = Color(0xFF1E40AF),\
                                                        start = Offset(0f, 0f),\
                                                        end = Offset(0f, size.height),\
                                                        strokeWidth = 1.dp.toPx()\
                                                    )\
                                                },' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt
