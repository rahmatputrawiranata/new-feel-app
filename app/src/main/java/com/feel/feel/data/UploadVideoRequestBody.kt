package com.feel.feel.data

data class UploadVideoRequestBody(
    val authorId: String,
    val description: String,
    val genre: String,
    val thumbnailUrl: String,
    val url: String
)