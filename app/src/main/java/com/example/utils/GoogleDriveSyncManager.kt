package com.example.utils

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object GoogleDriveSyncManager {
    private const val TAG = "GoogleDriveSyncManager"
    private const val DB_MIME_TYPE = "application/x-sqlite3"
    private const val DB_FILE_NAME = "mason_database.db"

    suspend fun syncToDrive(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("User not signed in"))

            // Force database checkpoint to ensure all data is written
            val db = AppDatabase.getDatabase(context)
            val cursor = db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            cursor.moveToFirst()
            cursor.close()
            db.close()
            AppDatabase.resetInstance()

            val token = GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/drive.appdata")
            val dbFile = context.getDatabasePath("mason_database")
            
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            val client = OkHttpClient()
            
            // 1. Check if backup already exists
            val existingFileId = getExistingBackupFileId(client, token)

            // 2. Upload or Update
            if (existingFileId != null) {
                updateFile(client, token, existingFileId, dbFile)
                Result.success("Backup updated successfully")
            } else {
                uploadNewFile(client, token, dbFile)
                Result.success("New backup created successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun restoreFromDrive(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("User not signed in"))
            
            val token = GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/drive.appdata")
            val client = OkHttpClient()
            
            val existingFileId = getExistingBackupFileId(client, token)
                ?: return@withContext Result.failure(Exception("No backup found on Google Drive"))

            // Download file
            val request = Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files/$existingFileId?alt=media")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            // Close existing DB
            val db = AppDatabase.getDatabase(context)
            db.close()
            AppDatabase.resetInstance()
            
            val dbPath = context.getDatabasePath("mason_database")
            val dbWalPath = context.getDatabasePath("mason_database-wal")
            val dbShmPath = context.getDatabasePath("mason_database-shm")
            
            if (dbWalPath.exists()) dbWalPath.delete()
            if (dbShmPath.exists()) dbShmPath.delete()

            response.body?.byteStream()?.use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success("Data restored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            Result.failure(e)
        }
    }

    private fun getExistingBackupFileId(client: OkHttpClient, token: String): String? {
        val request = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$DB_FILE_NAME'")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val json = JSONObject(response.body?.string() ?: "{}")
            val files = json.optJSONArray("files")
            if (files != null && files.length() > 0) {
                return files.getJSONObject(0).getString("id")
            }
        }
        return null
    }

    private fun uploadNewFile(client: OkHttpClient, token: String, file: File) {
        val metadata = JSONObject()
            .put("name", DB_FILE_NAME)
            .put("parents", org.json.JSONArray().put("appDataFolder"))
            .toString()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("metadata", null, metadata.toRequestBody("application/json".toMediaType()))
            .addFormDataPart("file", DB_FILE_NAME, file.asRequestBody(DB_MIME_TYPE.toMediaType()))
            .build()

        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Upload failed: ${response.code} ${response.message}")
        }
    }

    private fun updateFile(client: OkHttpClient, token: String, fileId: String, file: File) {
        val requestBody = file.asRequestBody(DB_MIME_TYPE.toMediaType())
        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            .addHeader("Authorization", "Bearer $token")
            .patch(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Update failed: ${response.code} ${response.message}")
        }
    }
}
