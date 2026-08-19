package com.example.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.OnUserEarnedRewardListener
import java.util.Date

class AdManager(private val context: Context) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var actionCount = 0
    private val ACTION_THRESHOLD = 2 // Show interstitial every 2 actions

    init {
        try {
            val app = context.applicationContext as Application
            app.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            loadAppOpenAd()
            loadInterstitialAd()
            loadRewardedAd()
        } catch (e: Throwable) {
            Log.e("AdManager", "Error initializing AdManager", e)
        }
    }

    private fun loadAppOpenAd() {
        try {
            if (isAdAvailable()) return
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                "ca-app-pub-3940256099942544/9257395921", // Test App Open Ad Unit ID
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        loadTime = Date().time
                        Log.d("AdManager", "App Open Ad loaded.")
                    }
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("AdManager", "App Open Ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AdManager", "Error loading app open ad", e)
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < (numMilliSecondsPerHour * numHours)
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    fun showAdIfAvailable(activity: Activity) {
        try {
            if (!isShowingAd && isAdAvailable()) {
                appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
                        loadAppOpenAd()
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        loadAppOpenAd()
                    }
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
                appOpenAd?.show(activity)
            } else {
                loadAppOpenAd()
            }
        } catch (e: Exception) {
            Log.e("AdManager", "Error showing app open ad", e)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            showAdIfAvailable(it)
        }
    }

    private fun loadInterstitialAd() {
        try {
            val request = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                "ca-app-pub-3940256099942544/1033173712", // Test Interstitial Ad Unit ID
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdManager", "Interstitial ad failed to load: ${adError.message}")
                        interstitialAd = null
                    }
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d("AdManager", "Interstitial ad loaded.")
                        interstitialAd = ad
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AdManager", "Error loading interstitial ad", e)
        }
    }

    fun showInterstitialAd(activity: Activity) {
        try {
            if (interstitialAd != null) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdManager", "Interstitial Ad was dismissed.")
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d("AdManager", "Interstitial Ad failed to show: ${adError.message}")
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdManager", "Interstitial Ad showed fullscreen content.")
                        interstitialAd = null
                    }
                }
                interstitialAd?.show(activity)
            } else {
                Log.d("AdManager", "The interstitial ad wasn't ready yet.")
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.e("AdManager", "Error showing interstitial ad", e)
        }
    }
    
    fun incrementActionAndShowInterstitial(activity: Activity) {
        actionCount++
        if (actionCount >= ACTION_THRESHOLD) {
            actionCount = 0
            showInterstitialAd(activity)
        }
    }

    private fun loadRewardedAd() {
        try {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                "ca-app-pub-3940256099942544/5224354917", // Test Rewarded Ad Unit ID
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdManager", "Rewarded ad failed to load: ${adError.message}")
                        rewardedAd = null
                    }
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d("AdManager", "Rewarded ad loaded.")
                        rewardedAd = ad
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AdManager", "Error loading rewarded ad", e)
        }
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit) {
        try {
            var rewardEarned = false
            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdManager", "Rewarded Ad was dismissed.")
                        rewardedAd = null
                        loadRewardedAd()
                        if (rewardEarned) {
                            onRewarded()
                        }
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d("AdManager", "Rewarded Ad failed to show: ${adError.message}")
                        rewardedAd = null
                        loadRewardedAd()
                        onRewarded() // Fallback
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdManager", "Rewarded Ad showed fullscreen content.")
                    }
                }
                rewardedAd?.show(activity) { rewardItem ->
                    Log.d("AdManager", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    rewardEarned = true
                }
            } else {
                Log.d("AdManager", "The rewarded ad wasn't ready yet.")
                loadRewardedAd()
                onRewarded() // Fallback
            }
        } catch (e: Exception) {
            Log.e("AdManager", "Error showing rewarded ad", e)
            onRewarded() // Fallback
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
