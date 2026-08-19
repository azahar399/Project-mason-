sed -i 's/import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback/import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback\nimport com.google.android.gms.ads.rewarded.RewardedAd\nimport com.google.android.gms.ads.rewarded.RewardedAdLoadCallback\nimport com.google.android.gms.ads.rewarded.RewardItem\nimport com.google.android.gms.ads.OnUserEarnedRewardListener/g' app/src/main/java/com/example/utils/AdManager.kt

sed -i '/private var interstitialAd/a\    private var rewardedAd: RewardedAd? = null' app/src/main/java/com/example/utils/AdManager.kt

sed -i '/loadInterstitialAd()/a\        loadRewardedAd()' app/src/main/java/com/example/utils/AdManager.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/utils/AdManager.kt

    private fun loadRewardedAd() {
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
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit) {
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
    }
INNER_EOF
