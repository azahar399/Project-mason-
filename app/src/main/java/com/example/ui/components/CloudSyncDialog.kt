package com.example.ui.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.theme.PrimaryColor
import com.example.ui.theme.Slate500
import com.example.utils.GoogleDriveSyncManager
import com.example.workers.AutoSyncWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun CloudSyncDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isSignedIn by remember { 
        mutableStateOf(GoogleSignIn.getLastSignedInAccount(context) != null) 
    }
    var accountEmail by remember { 
        mutableStateOf(GoogleSignIn.getLastSignedInAccount(context)?.email) 
    }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                isSignedIn = true
                accountEmail = account?.email
                resultMessage = "Signed in successfully!"
                
                // Schedule WorkManager for background sync
                scheduleAutoSync(context)
            } catch (e: Exception) {
                resultMessage = "Sign-in failed: ${e.message}"
            }
        } else {
            resultMessage = "Sign-in cancelled."
        }
        isLoading = false
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "☁️ Google Cloud Sync",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                if (!isSignedIn) {
                    Text(
                        text = "Sign in with Google to automatically backup your data securely to your hidden Google Drive AppFolder. It syncs whenever you are online.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = "Signed in as:\n$accountEmail",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (resultMessage != null) {
                    Text(
                        text = resultMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isSignedIn) {
                    Button(
                        onClick = {
                            isLoading = true
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "Signing in..." else "Sign in with Google")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                resultMessage = "Syncing..."
                                coroutineScope.launch {
                                    val res = GoogleDriveSyncManager.syncToDrive(context)
                                    resultMessage = if (res.isSuccess) "Sync Complete!" else "Sync Failed: ${res.exceptionOrNull()?.message}"
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Backup Now", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup Now", maxLines = 1)
                        }

                        FilledTonalButton(
                            onClick = {
                                isLoading = true
                                resultMessage = "Restoring..."
                                coroutineScope.launch {
                                    val res = GoogleDriveSyncManager.restoreFromDrive(context)
                                    resultMessage = if (res.isSuccess) "Restore Complete! Please restart app." else "Restore Failed: ${res.exceptionOrNull()?.message}"
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Restore", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", maxLines = 1)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            val client = GoogleSignIn.getClient(context, gso)
                            client.signOut().addOnCompleteListener {
                                isSignedIn = false
                                accountEmail = null
                                resultMessage = "Signed out"
                                WorkManager.getInstance(context).cancelUniqueWork("AutoSyncWork")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Out")
                    }
                }

                TextButton(onClick = onDismissRequest) {
                    Text("Close")
                }
            }
        }
    }
}

private fun scheduleAutoSync(context: android.content.Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Sync every 12 hours when online
    val syncRequest = PeriodicWorkRequestBuilder<AutoSyncWorker>(12, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "AutoSyncWork",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
}
