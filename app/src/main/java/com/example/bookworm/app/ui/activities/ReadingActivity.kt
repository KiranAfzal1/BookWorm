package com.example.bookworm.app.ui.activities

import android.os.Bundle
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookworm.databinding.ActivityReadingBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.example.bookworm.R

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?){
    window.statusBarColor = ContextCompat.getColor(this, R.color.snow)
    window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                )
    }
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        bookId = intent.getStringExtra("bookId")

        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
