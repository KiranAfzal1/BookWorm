package com.example.bookworm.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookworm.app.data.models.BookWithUserData
import com.example.bookworm.app.ui.activities.BookDetailsActivity
import com.example.bookworm.app.ui.adapters.BookAdapter
import com.example.bookworm.databinding.FragmentLibraryBinding
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bookworm.R

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BookAdapter
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadUserLibrary()

        // load banner ad
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setupRecyclerView() {
        adapter = BookAdapter { book ->
            if (!isAdded) return@BookAdapter
            val intent = Intent(requireContext(), BookDetailsActivity::class.java)
            intent.putExtra("book", book)
            startActivity(intent)
        }

        binding.rvLibrary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLibrary.adapter = adapter
    }

    private fun loadUserLibrary() {
        val user = auth.currentUser
        if (user == null) {
            showEmptyState()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users")
            .document(user.uid)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val books = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BookWithUserData::class.java)
                }

                binding.progressBar.visibility = View.GONE

                if (books.isEmpty()) {
                    showEmptyState()
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvLibrary.visibility = View.VISIBLE
                    adapter.submitList(books)
                }
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                binding.progressBar.visibility = View.GONE
                showEmptyState()
            }
    }

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvLibrary.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
