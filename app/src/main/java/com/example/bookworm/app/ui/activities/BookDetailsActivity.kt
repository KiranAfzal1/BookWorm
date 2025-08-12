package com.example.bookworm.app.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bookworm.app.data.models.BookWithUserData
import com.example.bookworm.databinding.ActivityBookDetailsBinding
import com.example.bookworm.app.ad.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bookworm.R

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private var book: BookWithUserData? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?){
    window.statusBarColor = ContextCompat.getColor(this, R.color.snow)
    window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                )
    }

        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        MobileAds.initialize(this)


        binding.adView.loadAd(AdRequest.Builder().build())


        AdManager.preloadInterstitialAd(this)

        setupToolbar()
        getBookFromIntent()
        setupFavoriteButton()
        setupReadNowButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun getBookFromIntent() {
        val rawBook = intent.getParcelableExtra<BookWithUserData>("book")
        if (rawBook != null) {
            book = rawBook
            updateUI(book!!)

            checkIfBookInLibrary()
        } else {
            Toast.makeText(this, "Error: Book not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateUI(book: BookWithUserData) {
        binding.apply {
            tvTitle.text = book.title
            tvAuthor.text = book.author
            tvGenre.text = book.genre ?: "Unknown Genre"
            tvDescription.text = book.description ?: "No description available"

            Glide.with(this@BookDetailsActivity)
                .load(book.coverImageUrl)
                .into(ivBookCover)
        }

        updateFavoriteButtonUI()
    }

    private fun updateFavoriteButtonUI() {
        val isInLibrary = book?.isInLibrary ?: false
        binding.btnToggleFavorite.text = if (isInLibrary) "Unfavorite" else "Favorite"
        if (isInLibrary) {
            binding.btnToggleFavorite.setTextColor(Color.RED)
            binding.btnToggleFavorite.iconTint = ColorStateList.valueOf(Color.RED)
            binding.btnToggleFavorite.icon = getDrawable(com.example.bookworm.R.drawable.ic_favorite_filled)
        } else {
            binding.btnToggleFavorite.setTextColor(Color.GRAY)
            binding.btnToggleFavorite.iconTint = ColorStateList.valueOf(Color.GRAY)
            binding.btnToggleFavorite.icon = getDrawable(com.example.bookworm.R.drawable.ic_favorite_outline)
        }
    }

    private fun setupFavoriteButton() {
        binding.btnToggleFavorite.setOnClickListener {
            if (currentUserId == null) {
                Toast.makeText(this, "Please log in to manage your library", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            book?.let { currentBook ->
                val favDocRef = firestore.collection("users")
                    .document(currentUserId)
                    .collection("favorites")
                    .document(currentBook.id)

                if (currentBook.isInLibrary) {

                    favDocRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Unfavorited", Toast.LENGTH_SHORT).show()
                            currentBook.isInLibrary = false
                            updateFavoriteButtonUI()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to unfavorite", Toast.LENGTH_SHORT).show()
                        }
                } else {

                    favDocRef.set(currentBook)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Favorited", Toast.LENGTH_SHORT).show()
                            currentBook.isInLibrary = true
                            updateFavoriteButtonUI()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to favorite", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun checkIfBookInLibrary() {
        if (currentUserId == null || book == null) return

        firestore.collection("users")
            .document(currentUserId)
            .collection("favorites")
            .document(book!!.id)
            .get()
            .addOnSuccessListener { doc ->
                book!!.isInLibrary = doc.exists()
                updateFavoriteButtonUI()
            }
            .addOnFailureListener {

            }
    }

    private fun setupReadNowButton() {
        binding.btnReadNow.setOnClickListener {

            AdManager.showInterstitial(this) {
                openReadingActivity()
            }
        }
    }

    private fun openReadingActivity() {
        book?.let {
            val intent = Intent(this, ReadingActivity::class.java)
            intent.putExtra("bookId", it.id)
            startActivity(intent)
        } ?: Toast.makeText(this, "Book not available to read", Toast.LENGTH_SHORT).show()
    }
}
