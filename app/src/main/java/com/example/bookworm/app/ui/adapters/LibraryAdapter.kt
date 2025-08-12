package com.example.bookworm.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.app.data.models.UserLibrary
import com.example.bookworm.databinding.ItemLibraryBookBinding

class LibraryAdapter(
    private val onBookClick: (UserLibrary) -> Unit
) : ListAdapter<UserLibrary, LibraryAdapter.LibraryBookViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryBookViewHolder {
        val binding = ItemLibraryBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibraryBookViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: LibraryBookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LibraryBookViewHolder(
        private val binding: ItemLibraryBookBinding,
        private val onBookClick: (UserLibrary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserLibrary) {
            binding.tvTitle.text = book.title
            binding.tvAuthor.text = book.author

            Glide.with(binding.root.context)
                .load(book.coverUrl)
                .centerCrop()
                .into(binding.ivCover)

            binding.root.setOnClickListener { onBookClick(book) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UserLibrary>() {
        override fun areItemsTheSame(oldItem: UserLibrary, newItem: UserLibrary) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserLibrary, newItem: UserLibrary) =
            oldItem == newItem
    }
}
