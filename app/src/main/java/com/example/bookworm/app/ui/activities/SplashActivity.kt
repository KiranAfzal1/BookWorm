package com.example.bookworm.app.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookworm.app.ad.AdManager
import com.example.bookworm.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.blue2)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    )
        }

        AdManager.preloadAppOpenAd(this, object : AdManager.AppOpenAdLoadCallback {
            override fun onAdLoaded() {
                goToNextScreen(showAd = true)
            }

            override fun onAdFailedToLoad(errorMessage: String) {
                goToNextScreen(showAd = false)
            }
        })
    }

    private fun goToNextScreen(showAd: Boolean) {
        if (showAd) {
            AdManager.showAppOpenAd(this) {
                navigateBasedOnLogin()
            }
        } else {
            navigateBasedOnLogin()
        }
    }

    private fun navigateBasedOnLogin() {
        val loggedIn = isUserLoggedIn()
        val intent = if (loggedIn) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun isUserLoggedIn(): Boolean {

        return false
    }
}
