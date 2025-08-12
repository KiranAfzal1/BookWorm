package com.example.bookworm.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookworm.R
import com.example.bookworm.app.ui.activities.LoginActivity
import com.example.bookworm.databinding.FragmentProfileBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database = FirebaseDatabase
        .getInstance("https://bookworm-ec1d7-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(requireContext()) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            if (!isAdded) return
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        val userId = currentUser.uid

        database.child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                if (!snapshot.exists()) {
                    Log.w("ProfileFragment", "No profile data found for $userId")
                    showDefaultProfile(currentUser.email)
                    return@addOnSuccessListener
                }

                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val profileKey = snapshot.child("profileImage").getValue(String::class.java)

                Log.d("ProfileFragment", "Profile loaded: name=$name, email=$email, key=$profileKey")

                binding.tvUserName.text = name ?: email?.substringBefore("@") ?: "Reader"
                binding.tvUserEmail.text = email ?: currentUser.email ?: ""

                val profileResId = getProfileDrawableId(profileKey)

                if (profileResId != R.drawable.ic_person) {
                    binding.ivProfileImage.setImageResource(profileResId)
                    Log.d("ProfileFragment", "Using drawable resource for profile image")
                } else if (currentUser.photoUrl != null) {
                    Glide.with(view.context)
                        .load(currentUser.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivProfileImage)
                    Log.d("ProfileFragment", "Using FirebaseAuth photoUrl for profile image")
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.ic_person)
                    Log.d("ProfileFragment", "Using default profile image")
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                Log.e("ProfileFragment", "Failed to load profile", e)
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                showDefaultProfile(currentUser.email)
            }

        binding.btnEditProfile.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            Toast.makeText(requireContext(), "Edit profile coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSignOut.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun showDefaultProfile(email: String?) {
        if (_binding != null) {
            binding.tvUserName.text = email?.substringBefore("@") ?: "Reader"
            binding.tvUserEmail.text = email ?: ""
            binding.ivProfileImage.setImageResource(R.drawable.ic_person)
        }
    }

    private fun getProfileDrawableId(key: String?): Int {
        return when (key) {
            "ic_person1" -> R.drawable.ic_person1
            "ic_person2" -> R.drawable.ic_person2
            "ic_person3" -> R.drawable.ic_person3
            else -> R.drawable.ic_person
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
