sed -i '/DropdownMenuItem(/,/Generate Excel Grid (Demo)/{
  /Generate Excel Grid (Demo)/,/onClick = {/d
  /viewModel.generateExcelGrid()/d
  /overflowMenuExpanded = false/d
  /})/d
}' app/src/main/java/com/example/ui/screens/DashboardScreen.kt
