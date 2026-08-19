awk '
/class AppViewModel/ {
    print $0
    print "    private val chatbotEngine = com.example.utils.ChatbotEngine(repository)"
    next
}
/fun deleteOtherWorkEntry/ {
    print $0
    next
}
/^}$/ {
    if (in_class) {
        print ""
        print "    suspend fun processChatCommand(command: String): String {"
        print "        return chatbotEngine.processMessage(command)"
        print "    }"
        print "}"
        in_class = 0
        next
    }
}
{
    if (/class AppViewModel/) {
        in_class = 1
    }
    if (/class AppViewModelFactory/) {
        in_class = 0
    }
    print $0
}
' app/src/main/java/com/example/ui/AppViewModel.kt > tmp_vm.kt
mv tmp_vm.kt app/src/main/java/com/example/ui/AppViewModel.kt
