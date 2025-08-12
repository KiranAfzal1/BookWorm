package com.example.bookworm.app.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.bookworm.databinding.ActivityMainBinding
import com.example.bookworm.R
import com.example.bookworm.app.ui.fragments.ExploreFragment
import com.example.bookworm.app.ui.fragments.LibraryFragment
import com.example.bookworm.app.ui.fragments.ProfileFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.snow)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        MobileAds.initialize(this) {}

        AdManager.preloadInterstitialAd(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ExploreFragment())
            .commit()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ExploreFragment())
                        .commit()
                    true
                }
                R.id.nav_library -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, LibraryFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}

object AdManager {
    var interstitialAd: InterstitialAd? = null

    fun preloadInterstitialAd(context: MainActivity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            })
    }
}
