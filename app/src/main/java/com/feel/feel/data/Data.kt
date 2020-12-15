package com.feel.feel.data

data class Data(
    val accessToken: String,
    val balance: Int,
    val earning: Float,
    val createdAt: String,
    val email: String,
    val id: String,
    val myVideos: List<Any>,
    val name: String,
    val ownVideos: List<Any>,
    val password: String,
    val refreshToken: String,
    val updatedAt: String
)