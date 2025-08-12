package com.example.bookworm.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.bookworm.app.ad.AdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class MyApp : Application() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Initializing Mobile Ads SDK")

        MobileAds.initialize(this, OnInitializationCompleteListener { initializationStatus: InitializationStatus ->
            Log.d(TAG, "Mobile Ads SDK initialized: $initializationStatus")
            preloadAppOpenAdWithRetry()
        })
    }

    private fun preloadAppOpenAdWithRetry(retryCount: Int = 0) {
        Log.d(TAG, "Preloading App Open Ad, attempt: $retryCount")

        AdManager.preloadAppOpenAd(this, object : AdManager.AppOpenAdLoadCallback {
            override fun onAdLoaded() {
                Log.d(TAG, "App Open Ad loaded successfully")
            }

            override fun onAdFailedToLoad(errorMessage: String) {
                Log.e(TAG, "App Open Ad failed to load: $errorMessage")
                if (retryCount < 3) {
                    val delayMs = 3000L * (retryCount + 1)
                    handler.postDelayed({
                        preloadAppOpenAdWithRetry(retryCount + 1)
                    }, delayMs)
                }
            }
        })
    }

    companion object {
        private const val TAG = "MyApp"
    }
}
