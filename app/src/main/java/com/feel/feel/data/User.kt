package com.feel.feel.data

data class User(
    val balance: Int,
    val createdAt: String,
    val email: String,
    val id: String,
    val myVideos: List<MyVideoX>,
    val name: String,
    val ownVideos: List<OwnVideoXX>,
    val refreshToken: String,
    val updatedAt: String
)