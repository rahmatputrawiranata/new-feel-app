package com.feel.feel.data

data class Video(
    var author: Author,
    val description: String,
    val id: String,
    val price: Int,
    val status: String,
    val thumbnailUrl: String,
    val genre: String,
    val url: String
)