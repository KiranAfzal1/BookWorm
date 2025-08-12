package com.example.bookworm.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImageUrl: String,
    val description: String
) : Parcelable
