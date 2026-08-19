package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.utils.AdManager
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MasonApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appDao()) }
    
    var adManager: AdManager? = null
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize the Google Mobile Ads SDK on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MasonApplication) {
                    // Initialize AdManager on the main thread after SDK init
                    CoroutineScope(Dispatchers.Main).launch {
                        adManager = AdManager(this@MasonApplication)
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("MasonApplication", "AdMob initialization failed", e)
            }
        }
    }
}
