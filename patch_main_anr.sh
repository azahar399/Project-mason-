awk '
/override fun onCreate\(savedInstanceState: Bundle\?\)/ {
    print $0
    print "    super.onCreate(savedInstanceState)"
    print "    enableEdgeToEdge()"
    print ""
    print "    // Check if user is signed in, if so, ensure worker is running"
    print "    androidx.lifecycle.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {"
    print "        try {"
    print "            val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)"
    print "            if (account != null) {"
    print "                scheduleAutoSync()"
    print "            }"
    print "        } catch (e: Throwable) {"
    print "            android.util.Log.e(\"MainActivity\", \"Google Sign In check failed on startup\", e)"
    print "        }"
    print "    }"
    in_block = 1
    next
}
/super\.onCreate/ { if (in_block) next }
/enableEdgeToEdge/ { if (in_block) next }
/try \{/ { if (in_block) next }
/val account = / { if (in_block) next }
/if \(account != null\) \{/ { if (in_block) next }
/scheduleAutoSync/ { if (in_block) next }
/\} catch \(e: Throwable\) \{/ { if (in_block) { in_catch = 1; next } }
/Log\.e/ { if (in_block && in_catch) next }
/\}/ { if (in_block && in_catch) { in_block = 0; in_catch = 0; next } }
{ print $0 }
' app/src/main/java/com/example/MainActivity.kt > tmp_main2.kt
mv tmp_main2.kt app/src/main/java/com/example/MainActivity.kt
