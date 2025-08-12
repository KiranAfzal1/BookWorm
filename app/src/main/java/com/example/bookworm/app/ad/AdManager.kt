package com.example.bookworm.app.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.bookworm.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdShowing = false

    private var interstitialAd: InterstitialAd? = null

    interface AppOpenAdLoadCallback {
        fun onAdLoaded()
        fun onAdFailedToLoad(errorMessage: String)
    }

    fun preloadAppOpenAd(context: Context, callback: AppOpenAdLoadCallback? = null) {
        if (appOpenAd != null) {
            callback?.onAdLoaded()
            return
        }

        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            context.getString(R.string.app_open_ad_unit_id),
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d("AdManager", "App Open Ad loaded successfully")
                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    Log.e("AdManager", "App Open Ad failed to load: ${error.message}")
                    callback?.onAdFailedToLoad(error.message ?: "Unknown error")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdClosed: () -> Unit) {
        if (appOpenAd == null || isAppOpenAdShowing) {
            onAdClosed()
            return
        }

        isAppOpenAdShowing = true

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAppOpenAdShowing = false
                appOpenAd = null
                preloadAppOpenAd(activity)
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isAppOpenAdShowing = false
                appOpenAd = null
                onAdClosed()
            }
        }

        appOpenAd?.show(activity)
    }

    fun preloadInterstitialAd(context: Context) {
        if (interstitialAd != null) return

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            context.getString(R.string.interstitial_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdManager", "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    Log.e("AdManager", "Interstitial Ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (interstitialAd == null) {
            onAdClosed()
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitialAd(activity)
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                onAdClosed()
            }
        }

        interstitialAd?.show(activity)
    }
}
