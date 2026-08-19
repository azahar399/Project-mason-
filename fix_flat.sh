#!/bin/bash
sed -i '1134,1148c\
                                            val flatBgColor = parseHexColor(flatStyle.bgColor) ?: Color(0xFF111111)\
                                            val flatTextColor = parseHexColor(flatStyle.textColor) ?: Color.White\
                                            val flatFontWeight = if (flatStyle.isBold) FontWeight.ExtraBold else FontWeight.Bold\
                                            val flatFontStyle = if (flatStyle.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal\
                                            Box(\
                                                modifier = Modifier\
                                                    .width(100.dp)\
                                                    .height(48.dp)\
                                                    .background(flatBgColor)\
                                                    .clickable { editingFlat = flat }\
                                                    .drawBehind {\
                                                        drawLine(\
                                                            color = Color(0xFF333333),\
                                                            start = Offset(size.width, 0f),\
                                                            end = Offset(size.width, size.height),\
                                                            strokeWidth = 2.dp.toPx()\
                                                        )\
                                                    },' /app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt
