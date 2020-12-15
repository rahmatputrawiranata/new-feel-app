package com.feel.feel.data

data class RefreshTokenRequestBody(
    val refreshToken: String,
    val userId: String
)