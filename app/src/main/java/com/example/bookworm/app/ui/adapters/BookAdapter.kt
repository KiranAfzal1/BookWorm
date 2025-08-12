package com.example.bookworm.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.databinding.ItemBookBinding
import com.example.bookworm.app.data.models.BookWithUserData

class BookAdapter(
    private val onBookClick: (BookWithUserData) -> Unit
) : ListAdapter<BookWithUserData, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        private val binding: ItemBookBinding,
        private val onBookClick: (BookWithUserData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: BookWithUserData) {
            binding.tvTitle.text = book.title
            binding.tvAuthor.text = book.author

            Glide.with(binding.root.context)
                .load(book.coverImageUrl)
                .centerCrop()
                .into(binding.ivCover)

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<BookWithUserData>() {
        override fun areItemsTheSame(oldItem: BookWithUserData, newItem: BookWithUserData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookWithUserData, newItem: BookWithUserData): Boolean {
            return oldItem == newItem
        }
    }
}
