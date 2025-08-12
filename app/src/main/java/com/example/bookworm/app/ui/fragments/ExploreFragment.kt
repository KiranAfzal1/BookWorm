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
import com.example.bookworm.databinding.FragmentExploreBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.bookworm.R

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter
    private var adView: AdView? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)


        MobileAds.initialize(requireContext()) {}

        adView = binding.adView
        adView?.loadAd(AdRequest.Builder().build())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchBooksFromFirestore()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter { bookWithUserData ->
            if (!isAdded) return@BookAdapter
            val intent = Intent(requireContext(), BookDetailsActivity::class.java)
            intent.putExtra("book", bookWithUserData)
            startActivity(intent)
        }

        binding.rvBooks.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchBooksFromFirestore() {
        if (!isAdded || _binding == null) return
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("books")
            .orderBy("title", Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val books = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BookWithUserData::class.java)?.copy(id = doc.id)
                }
                updateUI(books)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvBooks.visibility = View.GONE
            }
    }

    private fun updateUI(books: List<BookWithUserData>) {
        if (!isAdded || _binding == null) return

        binding.progressBar.visibility = View.GONE

        if (books.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvBooks.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvBooks.visibility = View.VISIBLE
            bookAdapter.submitList(books)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adView = null
        _binding = null
    }
}
