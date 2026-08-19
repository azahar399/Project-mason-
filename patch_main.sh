sed -i 's/onNavigateToDailyWageSheet = { navController.navigate("daily_wage_sheet") }/onNavigateToDailyWageSheet = { navController.navigate("daily_wage_sheet") },\n        onNavigateToChatbot = { navController.navigate("chatbot") }/g' app/src/main/java/com/example/MainActivity.kt

sed -i '/import com.example.ui.screens.DailyWageSheetScreen/a import com.example.ui.screens.ChatbotScreen' app/src/main/java/com/example/MainActivity.kt

awk '
/composable\("daily_wage_sheet"\)/ {
    print "    composable(\"chatbot\") {"
    print "      ChatbotScreen("
    print "        viewModel = viewModel,"
    print "        onBack = { navController.popBackStack() }"
    print "      )"
    print "    }"
    print $0
    next
}
{ print $0 }
' app/src/main/java/com/example/MainActivity.kt > tmp_main.kt
mv tmp_main.kt app/src/main/java/com/example/MainActivity.kt
