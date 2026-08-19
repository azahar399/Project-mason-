with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    text = f.read()

import re
old_text = r'''                            // Table body
                            if \(selectedFloorId == null\) \{
                                Box\(modifier = Modifier\.fillMaxSize\(\)\.padding\(32\.dp\), contentAlignment = Alignment\.Center\) \{
                                    Column\(horizontalAlignment = Alignment\.CenterHorizontally\) \{
                                        Icon\(Icons\.Default\.Warning, contentDescription = "Info", modifier = Modifier\.size\(48\.dp\), tint = Slate400\)
                                        Spacer\(modifier = Modifier\.height\(16\.dp\)\)
                                        Text\("Please select a floor from the top dropdown to view its flats and work progress\.", textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center, color = Slate500, fontSize = 14\.sp\)
                                    \}
                                \}
                            \}'''

text = re.sub(old_text, '                            // Table body', text)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(text)
