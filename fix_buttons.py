with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

def fix_buttons():
    for i in range(len(lines)):
        # fix: dismissButton = { }) { Text("Cancel") }
        if "dismissButton = {" in lines[i]:
            if "}) {" in lines[i+1] and 'Text("Cancel")' in lines[i+2]:
                lines[i+1] = "                        TextButton(onClick = { showAddFlat = false }) {\n"
        
        # fix: confirmButton = { Button(onClick = { ... }, dismissButton = { } )
        # wait, the error said confirmButton has missing } for onClick
        if "confirmButton = {" in lines[i] and "Button(onClick = {" in lines[i+1]:
            # Let's find where onClick ends
            j = i + 2
            brace_count = 1
            while j < i + 30:
                if "}" in lines[j] and not "})" in lines[j] and not "}) {" in lines[j]:
                    brace_count -= lines[j].count("}")
                if "{" in lines[j]:
                    brace_count += lines[j].count("{")
                
                # Check for "Text("Add")" missing its Button closure
                if "}) {" in lines[j] and 'Text("Add")' in lines[j+1] and '}' in lines[j+2]:
                    # This is actually right if it's "Button(onClick = { ... }) { Text("Add") }"
                    pass
                j += 1

fix_buttons()
