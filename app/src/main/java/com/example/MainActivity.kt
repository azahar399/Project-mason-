package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FlatDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.AddWorkScreen
import com.example.ui.screens.AddOtherWorkScreen
import com.example.ui.screens.PersonDetailScreen
import com.example.ui.screens.WorkColumnDetailScreen
import com.example.ui.screens.PersonsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.WageLedgerScreen
import com.example.ui.screens.DailyWageSheetScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.workers.AutoSyncWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

  private val viewModel: AppViewModel by viewModels {
    AppViewModelFactory((application as MasonApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Check if user is signed in, if so, ensure worker is running
    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
            if (account != null) {
                scheduleAutoSync()
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Google Sign In check failed on startup", e)
        }
    }
    
    // Set up Compose UI
    setContent {
      MyApplicationTheme {
        MasonApp(viewModel)
      }
    }
  }

  private fun scheduleAutoSync() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncRequest = PeriodicWorkRequestBuilder<AutoSyncWorker>(12, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "AutoSyncWork",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
  }
}

@Composable
fun MasonApp(viewModel: AppViewModel) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "splash") {
    composable("splash") {
      SplashScreen(
        onNavigateToDashboard = {
          navController.navigate("dashboard") {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }
    composable("dashboard") {
      DashboardScreen(
        viewModel = viewModel,
        onNavigateToFlat = { flatId -> navController.navigate("flat/$flatId") },
        onNavigateToPersons = { navController.navigate("persons") },
        onNavigateToCalendar = { navController.navigate("calendar") },
        onNavigateToAddWork = { navController.navigate("addWork") },
        onNavigateToAddOtherWork = { navController.navigate("addOtherWork") },
        onNavigateToWorkColumn = { colId -> navController.navigate("workColumn/$colId") },
        onNavigateToReport = { navController.navigate("report") },
        onNavigateToWageLedger = { navController.navigate("wage_ledger") },
        onNavigateToDailyWageSheet = { navController.navigate("daily_wage_sheet") },
        onNavigateToChatbot = { navController.navigate("chatbot") }
      )
    }
    composable("report") {
      ReportScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable(
      route = "flat/{flatId}",
      arguments = listOf(navArgument("flatId") { type = NavType.IntType })
    ) { backStackEntry ->
      val flatId = backStackEntry.arguments?.getInt("flatId") ?: 0
      FlatDetailScreen(
        flatId = flatId,
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
        onNavigateToAddWork = { fId -> navController.navigate("addWork?flatId=$fId") }
      )
    }
    composable(
      route = "addWork?flatId={flatId}",
      arguments = listOf(navArgument("flatId") { type = NavType.IntType; defaultValue = -1 })
    ) { backStackEntry ->
      val flatIdArg = backStackEntry.arguments?.getInt("flatId") ?: -1
      val initialFlatId = if (flatIdArg == -1) null else flatIdArg
      AddWorkScreen(
        initialFlatId = initialFlatId,
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("addOtherWork") {
      AddOtherWorkScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("persons") {
      PersonsScreen(
        viewModel = viewModel, 
        onBack = { navController.popBackStack() },
        onNavigateToPerson = { personId -> navController.navigate("person/$personId") }
      )
    }
    composable(
      route = "person/{personId}",
      arguments = listOf(navArgument("personId") { type = NavType.IntType })
    ) { backStackEntry ->
      val personId = backStackEntry.arguments?.getInt("personId") ?: 0
      PersonDetailScreen(
        personId = personId,
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable(
      route = "workColumn/{workColumnId}",
      arguments = listOf(navArgument("workColumnId") { type = NavType.IntType })
    ) { backStackEntry ->
      val workColumnId = backStackEntry.arguments?.getInt("workColumnId") ?: 0
      WorkColumnDetailScreen(
        workColumnId = workColumnId,
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("calendar") {
      CalendarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
    }
    composable("wage_ledger") {
      WageLedgerScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("chatbot") {
      ChatbotScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("daily_wage_sheet") {
      DailyWageSheetScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
  }
}
