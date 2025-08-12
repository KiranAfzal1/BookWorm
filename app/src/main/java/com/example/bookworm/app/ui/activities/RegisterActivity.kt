package com.example.bookworm.app.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookworm.databinding.RegisterActivityBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.bookworm.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterActivityBinding
    private lateinit var auth: FirebaseAuth
    private var selectedProfileResId: Int = R.drawable.ic_person1
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.snow)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    )
        }
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        auth = FirebaseAuth.getInstance()

        highlightSelectedProfile(binding.ivProfile1)

        binding.ivProfile1.setOnClickListener { onProfileSelected(it as ImageView, R.drawable.ic_person1) }
        binding.ivProfile2.setOnClickListener { onProfileSelected(it as ImageView, R.drawable.ic_person2) }
        binding.ivProfile3.setOnClickListener { onProfileSelected(it as ImageView, R.drawable.ic_person3) }

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.inputType = if (isPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            binding.btnTogglePassword.setImageResource(
                if (isPasswordVisible) android.R.drawable.ic_menu_close_clear_cancel
                else android.R.drawable.ic_menu_view
            )
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Signing up..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Sign Up"

                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser == null) {
                            Toast.makeText(this, "User ID is null after registration", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterActivity", "FirebaseUser null after registration")
                            return@addOnCompleteListener
                        }

                        val userId = firebaseUser.uid
                        Log.d("RegisterActivity", "New user created with UID: $userId")

                        val profileKey = when (selectedProfileResId) {
                            R.drawable.ic_person1 -> "ic_person1"
                            R.drawable.ic_person2 -> "ic_person2"
                            R.drawable.ic_person3 -> "ic_person3"
                            else -> "ic_person1"
                        }

                        val userMap = mapOf(
                            "uid" to userId,
                            "name" to name,
                            "email" to email,
                            "profileImage" to profileKey
                        )

                        Log.d("RegisterActivity", "Saving user data: $userMap")

                        FirebaseDatabase.getInstance(
                            "https://bookworm-ec1d7-default-rtdb.asia-southeast1.firebasedatabase.app"
                        )
                            .getReference("users")
                            .child(userId)
                            .setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                Log.d("RegisterActivity", "User data saved successfully")
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("RegisterActivity", "Failed to save user data", e)
                            }
                    } else {
                        Toast.makeText(this, task.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        Log.e("RegisterActivity", "Registration failed", task.exception)
                    }
                }
        }

        binding.tvToggleToLogin.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun onProfileSelected(selectedImageView: ImageView, profileResId: Int) {
        selectedProfileResId = profileResId
        highlightSelectedProfile(selectedImageView)
    }

    private fun highlightSelectedProfile(selected: ImageView) {
        listOf(binding.ivProfile1, binding.ivProfile2, binding.ivProfile3).forEach {
            it.alpha = 0.5f
        }
        selected.alpha = 1.0f
    }
}
